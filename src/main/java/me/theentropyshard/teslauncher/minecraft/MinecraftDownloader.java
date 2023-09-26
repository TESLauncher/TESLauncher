/*
 *  Copyright 2023 TheEntropyShard (https://github.com/TheEntropyShard)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package me.theentropyshard.teslauncher.minecraft;

import com.google.gson.Gson;
import me.theentropyshard.teslauncher.minecraft.models.VersionManifest;
import me.theentropyshard.teslauncher.utils.EnumOS;
import me.theentropyshard.teslauncher.utils.Http;
import me.theentropyshard.teslauncher.utils.PathUtils;
import net.lingala.zip4j.ZipFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class MinecraftDownloader {
    private static final String VER_MAN_V2 = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
    private static final String RESOURCES = "https://resources.download.minecraft.net/";
    private final Gson gson;
    private final Path clientsDir;
    private final Path assetsDir;
    private final Path librariesDir;
    private final Path nativesDir;
    private final Path instanceResourcesDir;

    public MinecraftDownloader(Path clientsDir, Path assetsDir, Path librariesDir, Path nativesDir,
                               Path instanceResourcesDir) {
        this.clientsDir = clientsDir;
        this.assetsDir = assetsDir;
        this.librariesDir = librariesDir;
        this.nativesDir = nativesDir;
        this.instanceResourcesDir = instanceResourcesDir;
        this.gson = new Gson();
    }

    public void downloadMinecraft(String versionId) throws IOException {
        PathUtils.createDirectoryIfNotExists(this.clientsDir.resolve(versionId));

        //LOG.info("Downloading Minecraft " + versionId);

        byte[] bytes = Http.get(MinecraftDownloader.VER_MAN_V2);
        VersionManifest manifest = this.gson.fromJson(MinecraftDownloader.getReader(bytes), VersionManifest.class);
        if (manifest == null) {
            throw new IOException("Unable to deserialize version manifest");
        }

        VersionManifest.Version[] versions = manifest.versions;
        if (versions == null) {
            throw new IOException("Unable to deserialize version manifest");
        }

        for (VersionManifest.Version version : versions) {
            if (version.id == null) {
                continue;
            }

            if (!version.id.equals(versionId)) {
                continue;
            }

            //LOG.info("Found Minecraft " + versionId);

            if (version.url == null) {
                throw new IOException("Version url is null");
            }

            //LOG.info("Downloading client...");
            System.out.println("Downloading client...");
            this.downloadClient(versionId, version.url);
            System.out.println("Downloaded client");

            //LOG.info("Downloading libraries...");
            System.out.println("Downloading libraries...");
            this.downloadLibraries(versionId, version.url);
            System.out.println("Downloaded libraries");

            //LOG.info("Extracting natives...");
            System.out.println("Extracting natives...");
            this.extractNatives(versionId);
            System.out.println("Extracted natives");

            System.out.println("Downloading assets...");
            this.downloadAssets(versionId);
            System.out.println("Downloaded assets");

            //LOG.info("Done");
        }
    }

    private void downloadClient(String versionId, String url) throws IOException {
        File jsonFile = this.clientsDir.resolve(versionId).resolve(versionId + ".json").toFile();
        if (!jsonFile.exists()) {
            byte[] bytes = Http.get(url);
            try (FileOutputStream fos = new FileOutputStream(jsonFile)) {
                fos.write(bytes);
            }
        }

        FileInputStream fileInputStream = new FileInputStream(jsonFile);
        Map<String, Object> map = this.gson.fromJson(new InputStreamReader(fileInputStream, StandardCharsets.UTF_8), Map.class);
        Map<String, Object> downloads = (Map<String, Object>) map.get("downloads");
        Map<String, Object> client = (Map<String, Object>) downloads.get("client");
        String clientUrl = (String) client.get("url");
        File jarFile = this.clientsDir.resolve(versionId).resolve(versionId + ".jar").toFile();

        if (!jarFile.exists()) {
            System.out.println("Downloading " + versionId + ".jar...");
            /*byte[] clientBytes = Http.get(clientUrl, ((totalBytes, currentBytes, done) -> {
                if (done) {
                    System.out.println("Downloaded " + (versionId + ".jar") + ", writing to disk...");
                } else {
                    System.out.println("Progress (" + (versionId + ".jar") + "): " + (currentBytes / 1024 / 1024) + " / " + (totalBytes / 1024 / 1024));
                }
            }));
            try (FileOutputStream fos = new FileOutputStream(jarFile)) {
                fos.write(clientBytes);
            }*/
            Http.downloadFile(clientUrl, jarFile.toPath(), ((totalBytes, currentBytes, done) -> {
                if (done) {
                    System.out.println("Downloaded " + (versionId + ".jar") + ", writing to disk...");
                } else {
                    System.out.println("Progress (" + (versionId + ".jar") + "): " + (currentBytes / 1024 / 1024) + " / " + (totalBytes / 1024 / 1024));
                }
            }));
        }
    }

    private void downloadLibraries(String versionId, String url) throws IOException {
        byte[] bytes = Http.get(url);
        Map<String, Object> map = this.gson.fromJson(MinecraftDownloader.getReader(bytes), Map.class);
        List<Map<String, Object>> libraries = (List<Map<String, Object>>) map.get("libraries");
        for (Map<String, Object> library : libraries) {
            Map<String, Object> downloads = (Map<String, Object>) library.get("downloads");
            Map<String, Object> artifact = (Map<String, Object>) downloads.get("artifact");
            Map<String, Object> classifiers = (Map<String, Object>) downloads.get("classifiers");
            if (artifact != null) {
                String path = (String) artifact.get("path");
                String libraryUrl = (String) artifact.get("url");

                Path resolvedPath = this.librariesDir.resolve(path);

                if (!Files.exists(resolvedPath)) {
                    System.out.println("Downloading " + resolvedPath.getFileName().toString());
                    byte[] libraryBytes = Http.get(libraryUrl, ((totalBytes, currentBytes, done) -> {
                        if (done) {
                            System.out.println("Downloaded " + resolvedPath.getFileName().toString());
                        } else {
                            System.out.println("Progress (" + resolvedPath.getFileName().toString() + "): " + (currentBytes / 1024 / 1024) + " / " + (totalBytes / 1024 / 1024));
                        }
                    }));
                    PathUtils.createDirectoryIfNotExists(resolvedPath.getParent());
                    Files.write(resolvedPath, libraryBytes);
                }
            }

            if (classifiers != null) {
                String key = "natives-" + MinecraftDownloader.getOsName();
                Map<String, Object> classifier = (Map<String, Object>) classifiers.get(key);
                if (classifier != null) {
                    String path = (String) classifier.get("path");
                    String libraryUrl = (String) classifier.get("url");
                    Path resolvedPath = this.nativesDir.resolve(versionId).resolve(path);
                    PathUtils.createDirectoryIfNotExists(resolvedPath.getParent());
                    File file = resolvedPath.toFile();
                    if (!file.exists()) {
                        System.out.println("Downloading " + resolvedPath.getFileName().toString());
                        byte[] libraryBytes = Http.get(libraryUrl, ((totalBytes, currentBytes, done) -> {
                            if (done) {
                                System.out.println("Downloaded " + resolvedPath.getFileName().toString());
                            } else {
                                System.out.println("Progress (" + resolvedPath.getFileName().toString() + "): " + (currentBytes / 1024 / 1024) + " / " + (totalBytes / 1024 / 1024));
                            }
                        }));
                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            fos.write(libraryBytes);
                        }
                    }
                }
            }
        }
    }

    private void downloadAssets(String versionId) throws IOException {
        File jsonFile = this.clientsDir.resolve(versionId).resolve(versionId + ".json").toFile();
        Map<String, Object> map = this.gson.fromJson(new FileReader(jsonFile), Map.class);
        Map<String, Object> assetIndex = (Map<String, Object>) map.get("assetIndex");
        if (assetIndex == null) {
            return;
        }

        Path assetsIndexFile = this.assetsDir.resolve("indexes").resolve(assetIndex.get("id") + ".json");
        if (!Files.exists(assetsIndexFile)) {
            PathUtils.createDirectoryIfNotExists(assetsIndexFile.getParent());
            String assetIndexUrl = (String) assetIndex.get("url");
            byte[] assetIndexJsonBytes = Http.get(assetIndexUrl);
            Files.write(assetsIndexFile, assetIndexJsonBytes);
        }

        Map<String, Object> assetIndexInfo = this.gson.fromJson(Files.newBufferedReader(assetsIndexFile), Map.class);
        Object virtualObj = assetIndexInfo.get("virtual");
        boolean assetsVirtual;
        if (virtualObj != null) {
            assetsVirtual = Boolean.parseBoolean(String.valueOf(virtualObj));
        } else {
            assetsVirtual = false;
        }

        Object mapToResourcesObj = assetIndexInfo.get("map_to_resources");
        boolean mapToResources;
        if (mapToResourcesObj != null) {
            mapToResources = Boolean.parseBoolean(String.valueOf(mapToResourcesObj));
        } else {
            mapToResources = false;
        }

        Map<String, Object> objects = (Map<String, Object>) assetIndexInfo.get("objects");
        objects.forEach((fileName, obj) -> {
            if (mapToResources) {
                Path filePath = this.instanceResourcesDir.resolve(fileName);
                if (!Files.exists(filePath)) {
                    Map<String, Object> assetItem = (Map<String, Object>) obj;
                    String assetItemHash = (String) assetItem.get("hash");
                    String prefix = assetItemHash.substring(0, 2);
                    try {
                        PathUtils.createDirectoryIfNotExists(filePath.getParent());
                        byte[] bytes = Http.get(MinecraftDownloader.RESOURCES + prefix + "/" + assetItemHash);
                        Files.write(filePath, bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (assetsVirtual) {
                Path filePath = this.assetsDir.resolve("virtual").resolve("legacy").resolve(fileName);
                if (!Files.exists(filePath)) {
                    Map<String, Object> assetItem = (Map<String, Object>) obj;
                    String assetItemHash = (String) assetItem.get("hash");
                    String prefix = assetItemHash.substring(0, 2);
                    try {
                        PathUtils.createDirectoryIfNotExists(filePath.getParent());
                        byte[] bytes = Http.get(MinecraftDownloader.RESOURCES + prefix + "/" + assetItemHash);
                        Files.write(filePath, bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Map<String, Object> assetItem = (Map<String, Object>) obj;
                String assetItemHash = (String) assetItem.get("hash");
                String prefix = assetItemHash.substring(0, 2);

                Path filePath = this.assetsDir.resolve("objects").resolve(prefix).resolve(assetItemHash);
                if (!Files.exists(filePath)) {
                    try {
                        PathUtils.createDirectoryIfNotExists(filePath.getParent());
                        byte[] bytes = Http.get(MinecraftDownloader.RESOURCES + prefix + "/" + assetItemHash);
                        Files.write(filePath, bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void downloadResources() throws IOException {

    }

    private static Reader getReader(byte[] bytes) throws IOException {
        return new InputStreamReader(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8);
    }

    private void extractNatives(String versionId) throws IOException {
        Path nativesDir = this.nativesDir;
        try (Stream<Path> files = Files.walk(nativesDir)) {
            files.forEach(file -> {
                if (Files.isRegularFile(file)) {
                    try (ZipFile zipFile = new ZipFile(file.toFile())) {
                        zipFile.extractAll(nativesDir.normalize().toAbsolutePath().toString());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

            });
        }
    }

    private static String getOsName() {
        EnumOS os = EnumOS.getOS();
        switch (os) {
            case WINDOWS:
                return "windows";
            case LINUX:
            case SOLARIS:
                return "linux";
            case MACOS:
                return "osx";
            case UNKNOWN:
                throw new RuntimeException("Unsupported OS");
        }
        return null;
    }
}
