/*
 * TESLauncher - https://github.com/TESLauncher/TESLauncher
 * Copyright (C) 2023 TESLauncher
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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.minecraft.MinecraftDownloadListener;
import me.theentropyshard.teslauncher.network.HttpRequest;
import me.theentropyshard.teslauncher.network.download.DownloadList;
import me.theentropyshard.teslauncher.network.download.HttpDownload;
import me.theentropyshard.teslauncher.utils.EnumOS;
import me.theentropyshard.teslauncher.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class JavaManager {
    private static final String ALL_RUNTIMES = "https://launchermeta.mojang.com/v1/products/java-runtime/2ec0cc96c44e5a76b9c8b7c39df7210883d12871/all.json";

    private final Path workDir;
    private final Gson gson;
    private final String executableName;

    public JavaManager(Path workDir) {
        this.workDir = workDir;
        this.gson = new Gson();
        if (EnumOS.getOS() == EnumOS.WINDOWS) {
            this.executableName = "javaw.exe";
        } else {
            this.executableName = "java";
        }
    }

    public void downloadRuntime(String componentName, MinecraftDownloadListener listener) throws IOException {
        Path componentDir = this.workDir.resolve(componentName);
        FileUtils.createDirectoryIfNotExists(componentDir);

        String jreOsName = JavaManager.getJreOsName();

        JsonObject osObject;
        try (HttpRequest request = new HttpRequest(TESLauncher.getInstance().getHttpClient(), this.gson)) {
            osObject = request.asObject(JavaManager.ALL_RUNTIMES, JsonObject.class);
        }

        if (osObject.has(jreOsName)) {
            JsonObject runtimesObject = this.gson.fromJson(osObject.get(jreOsName), JsonObject.class);
            if (runtimesObject.has(componentName)) {
                List<JavaRuntime> javaRuntimes = Arrays.asList(this.gson.fromJson(runtimesObject.get(componentName), JavaRuntime[].class));
                JavaRuntime javaRuntime = javaRuntimes.get(0);

                JavaRuntimeManifest manifest;
                try (HttpRequest request = new HttpRequest(TESLauncher.getInstance().getHttpClient(), this.gson)) {
                    manifest = request.asObject(javaRuntime.manifest.url, JavaRuntimeManifest.class);
                }

                DownloadList downloadList = new DownloadList((total, completed) -> {
                    listener.onProgress(0, total, completed);
                });

                for (Map.Entry<String, JreFile> entry : manifest.files.entrySet()) {
                    JreFile jreFile = entry.getValue();
                    Path savePath = componentDir.resolve(entry.getKey());
                    // TODO: there is also 'lzma' available. maybe use it and decompress?
                    if (jreFile.type.equals("directory")) {
                        FileUtils.createDirectoryIfNotExists(savePath);
                    } else if (jreFile.type.equals("file")) {
                        JreFile.Download raw = jreFile.downloads.get("raw");
                        //this.download(raw.url, savePath, raw.size, progressListener);

                        HttpDownload download = new HttpDownload.Builder()
                                .httpClient(TESLauncher.getInstance().getHttpClient())
                                .url(raw.url)
                                .expectedSize(raw.size)
                                .saveAs(savePath)
                                .build();
                        downloadList.add(download);
                    }
                }

                downloadList.downloadAll();

                String javaExecutable = this.getJavaExecutable(componentName);
                if (!new File(javaExecutable).setExecutable(true, true)) {
                    System.err.println("Unable to make '" + javaExecutable + "' executable. Operating system: " + EnumOS.getOS());
                }
            } else {
                throw new IOException("Unable to find JRE for component '" + componentName + "'");
            }
        } else {
            throw new IOException("Runtime for os '" + jreOsName + "' not found");
        }
    }

    private static String getJreOsName() {
        switch (EnumOS.getOS()) {
            case SOLARIS:
            case LINUX:
                if (EnumOS.getArch().equals("x86")) {
                    return "linux-i386";
                } else if (EnumOS.getArch().equals("x64")) {
                    return "linux";
                } else {
                    throw new RuntimeException("unknown linux architecture");
                }

            case WINDOWS:
                if (EnumOS.getArch().equals("x64")) {
                    return "windows-x64";
                } else if (EnumOS.getArch().equals("x32")) {
                    return "windows-x86";
                } else {
                    return "windows-arm64";
                }

            case MACOS:
                if (EnumOS.getArch().equals("x64")) {
                    return "mac-os";
                } else {
                    return "mac-os-arm64";
                }

            case UNKNOWN:
            default:
                throw new RuntimeException("Unknown os");
        }
    }

    public String getJavaExecutable(String componentName) {
        Path componentDir = this.workDir.resolve(componentName);
        if (EnumOS.getOS() == EnumOS.MACOS) {
            componentDir = componentDir.resolve("jre.bundle").resolve("Contents").resolve("Home");
        }
        return componentDir.resolve("bin").resolve(this.executableName).toString();
    }
}
