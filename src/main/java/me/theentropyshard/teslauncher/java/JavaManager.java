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
import me.theentropyshard.teslauncher.http.FileDownloader;
import me.theentropyshard.teslauncher.http.ProgressListener;
import me.theentropyshard.teslauncher.utils.EnumOS;
import me.theentropyshard.teslauncher.utils.Http;
import me.theentropyshard.teslauncher.utils.PathUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JavaManager {
    private static final String ALL_RUNTIMES = "https://launchermeta.mojang.com/v1/products/java-runtime/2ec0cc96c44e5a76b9c8b7c39df7210883d12871/all.json";

    private final Path workDir;
    private final FileDownloader fileDownloader;
    private final Gson gson;
    private final String executableName;

    public JavaManager(Path workDir, FileDownloader fileDownloader) {
        this.workDir = workDir;
        this.fileDownloader = fileDownloader;
        this.gson = new Gson();
        if (EnumOS.getOS() == EnumOS.WINDOWS) {
            this.executableName = "javaw.exe";
        } else {
            this.executableName = "java";
        }
    }

    // TODO: implement a proper check for this
    public boolean runtimeExists(String componentName) {
        return Files.exists(this.workDir.resolve(componentName));
    }

    public void downloadRuntime(String componentName, ProgressListener progressListener) throws IOException {
        Path componentDir = this.workDir.resolve(componentName);
        PathUtils.createDirectoryIfNotExists(componentDir);

        String jreOsName = JavaManager.getJreOsName();

        JsonObject osObject = this.gson.fromJson(JavaManager.getReader(Http.get(JavaManager.ALL_RUNTIMES)), JsonObject.class);
        if (osObject.has(jreOsName)) {
            JsonObject runtimesObject = this.gson.fromJson(osObject.get(jreOsName), JsonObject.class);
            if (runtimesObject.has(componentName)) {
                List<JavaRuntime> javaRuntimes = Arrays.asList(this.gson.fromJson(runtimesObject.get(componentName), JavaRuntime[].class));
                JavaRuntime javaRuntime = javaRuntimes.get(0);
                JavaRuntimeManifest manifest = this.gson.fromJson(
                        JavaManager.getReader(Http.get(javaRuntime.manifest.url)), JavaRuntimeManifest.class);
                for (Map.Entry<String, JreFile> entry : manifest.files.entrySet()) {
                    JreFile jreFile = entry.getValue();
                    Path savePath = componentDir.resolve(entry.getKey());
                    // TODO: there is also 'lzma' available. maybe use it and decompress?
                    if (jreFile.type.equals("directory")) {
                        PathUtils.createDirectoryIfNotExists(savePath);
                    } else if (jreFile.type.equals("file")) {
                        JreFile.Download raw = jreFile.downloads.get("raw");
                        this.download(raw.url, savePath, raw.size, progressListener);
                    }
                }

                if (EnumOS.getOS() == EnumOS.LINUX || EnumOS.getOS() == EnumOS.MACOS) {
                    Path javaExecutable = Paths.get(this.getJavaExecutable(componentName));
                    Set<PosixFilePermission> posixFilePermissions = Files.getPosixFilePermissions(javaExecutable);
                    posixFilePermissions.add(PosixFilePermission.OWNER_EXECUTE);
                    Files.setPosixFilePermissions(javaExecutable, posixFilePermissions);
                }
            } else {
                throw new IOException("Unable to find JRE for component '" + componentName + "'");
            }
        } else {
            throw new IOException("Runtime for os '" + jreOsName + "' not found");
        }
    }

    private void download(String url, Path savePath, long expectedSize, ProgressListener progressListener) throws IOException {
        PathUtils.createDirectoryIfNotExists(savePath.getParent());

        if (!Files.exists(savePath)) {
            this.fileDownloader.download(url, savePath, 0, progressListener);
        } else {
            long size = Files.size(savePath);
            if (size < expectedSize) {
                this.fileDownloader.download(url, savePath, size, progressListener);
            }
        }
    }

    private static Reader getReader(byte[] bytes) {
        return new InputStreamReader(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8);
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
