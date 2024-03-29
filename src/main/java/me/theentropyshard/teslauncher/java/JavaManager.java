/*
 * TESLauncher - https://github.com/TESLauncher/TESLauncher
 * Copyright (C) 2023-2024 TESLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package me.theentropyshard.teslauncher.java;

import com.google.gson.JsonObject;
import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.minecraft.ApiUrls;
import me.theentropyshard.teslauncher.network.HttpRequest;
import me.theentropyshard.teslauncher.network.download.DownloadList;
import me.theentropyshard.teslauncher.network.download.HttpDownload;
import me.theentropyshard.teslauncher.utils.FileUtils;
import me.theentropyshard.teslauncher.utils.json.Json;
import me.theentropyshard.teslauncher.utils.OperatingSystem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class JavaManager {
    private static final Logger LOG = LogManager.getLogger(JavaManager.class);

    private final Path workDir;

    public JavaManager(Path workDir) {
        this.workDir = workDir;
    }

    private static JsonObject fetchAndSaveAllRuntimes(Path runtimesFile) throws IOException {
        try (HttpRequest request = new HttpRequest(TESLauncher.getInstance().getHttpClient())) {
            String string = request.asString(ApiUrls.ALL_RUNTIMES);
            FileUtils.writeUtf8(runtimesFile, string);

            return Json.parse(string, JsonObject.class);
        }
    }

    private static JsonObject getAllRuntimesObject(Path runtimesFile) throws IOException {
        if (Files.exists(runtimesFile)) {
            JsonObject jsonObject = Json.parse(FileUtils.readUtf8(runtimesFile), JsonObject.class);

            BasicFileAttributes basicFileAttributes = Files.readAttributes(runtimesFile, BasicFileAttributes.class);
            String string = basicFileAttributes.lastModifiedTime().toString();
            if (OffsetDateTime.now().minus(Duration.ofHours(24)).isAfter(OffsetDateTime.parse(string))) {
                try {
                    return JavaManager.fetchAndSaveAllRuntimes(runtimesFile);
                } catch (IOException e) {
                    LOG.error(e);

                    return jsonObject;
                }
            } else {
                return jsonObject;
            }
        } else {
            return JavaManager.fetchAndSaveAllRuntimes(runtimesFile);
        }
    }

    public void downloadRuntime(String componentName, DownloadList javaList) throws IOException {
        Path componentDir = this.workDir.resolve(componentName);

        JsonObject osObject = JavaManager.getAllRuntimesObject(this.workDir.resolve("all_runtimes.json"));

        String jreOsName = JavaManager.getJreOsName();
        if (osObject.has(jreOsName)) {
            JsonObject runtimesObject = Json.parse(osObject.get(jreOsName), JsonObject.class);
            if (runtimesObject.has(componentName)) {
                List<JavaRuntime> javaRuntimes = Arrays.asList(Json.parse(runtimesObject.get(componentName), JavaRuntime[].class));
                JavaRuntime javaRuntime = javaRuntimes.get(0);

                JavaRuntimeManifest manifest;

                Path componentInfoFile = componentDir.resolve("component.json");
                if (Files.exists(componentInfoFile)) {
                    manifest = Json.parse(FileUtils.readUtf8(componentInfoFile), JavaRuntimeManifest.class);
                } else {
                    try (HttpRequest request = new HttpRequest(TESLauncher.getInstance().getHttpClient())) {
                        String string = request.asString(javaRuntime.manifest.url);
                        FileUtils.writeUtf8(componentInfoFile, string);
                        manifest = Json.parse(string, JavaRuntimeManifest.class);
                    }
                }

                for (Map.Entry<String, JreFile> entry : manifest.files.entrySet()) {
                    JreFile jreFile = entry.getValue();
                    Path savePath = componentDir.resolve(entry.getKey());

                    if (jreFile.type.equals("directory")) {
                        FileUtils.createDirectoryIfNotExists(savePath);
                    } else if (jreFile.type.equals("file")) {
                        // TODO: there is also 'lzma' available. maybe use it and decompress?
                        JreFile.Download raw = jreFile.downloads.get("raw");

                        HttpDownload download = new HttpDownload.Builder()
                                .httpClient(TESLauncher.getInstance().getHttpClient())
                                .url(raw.url)
                                .expectedSize(raw.size)
                                .sha1(raw.sha1)
                                .executable(jreFile.executable)
                                .saveAs(savePath)
                                .build();

                        javaList.add(download);
                    }
                }
            } else {
                throw new IOException("Unable to find JRE for component '" + componentName + "'");
            }
        } else {
            throw new IOException("Runtime for os '" + jreOsName + "' not found");
        }
    }

    private static String getJreOsName() {
        switch (OperatingSystem.getCurrent()) {
            case LINUX:
                if (OperatingSystem.getArch().equals("x86")) {
                    return "linux-i386";
                } else if (OperatingSystem.getArch().equals("x64")) {
                    return "linux";
                }

            case WINDOWS:
                if (OperatingSystem.getArch().equals("x64")) {
                    return "windows-x64";
                } else if (OperatingSystem.getArch().equals("x86")) {
                    return "windows-x86";
                }

                if (OperatingSystem.isArm()) {
                    return "windows-arm64";
                }

            case MACOS:
                if (OperatingSystem.getArch().equals("x64")) {
                    return "mac-os";
                } else {
                    return "mac-os-arm64";
                }

            default:
                throw new RuntimeException("Unreachable");
        }
    }

    public boolean runtimeExists(String componentName) {
        return Files.exists(Paths.get(this.getJavaExecutable(componentName)));
    }

    public String getJavaExecutable(String componentName) {
        Path componentDir = this.workDir.resolve(componentName);

        if (OperatingSystem.isMacOS()) {
            componentDir = componentDir.resolve("jre.bundle").resolve("Contents").resolve("Home");
        }

        return componentDir
                .resolve("bin")
                .resolve(OperatingSystem.getCurrent().getJavaExecutableName())
                .toString();
    }
}
