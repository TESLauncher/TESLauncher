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
import com.google.gson.GsonBuilder;
import me.theentropyshard.teslauncher.gson.ActionTypeAdapter;
import me.theentropyshard.teslauncher.gson.DetailedVersionInfoDeserializerOld;
import me.theentropyshard.teslauncher.http.FileDownloader;
import me.theentropyshard.teslauncher.http.FileDownloaderIO;
import me.theentropyshard.teslauncher.http.ProgressListener;
import me.theentropyshard.teslauncher.minecraft.models.VersionManifest;
import me.theentropyshard.teslauncher.minecraft.models.*;
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
import java.util.stream.Collectors;
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
    private final ProgressListener progressListener;

    private final FileDownloader fileDownloader;

    public MinecraftDownloader(Path clientsDir, Path assetsDir, Path librariesDir, Path nativesDir,
                               Path instanceResourcesDir, ProgressListener progressListener) {
        this.clientsDir = clientsDir;
        this.assetsDir = assetsDir;
        this.librariesDir = librariesDir;
        this.nativesDir = nativesDir;
        this.instanceResourcesDir = instanceResourcesDir;
        this.progressListener = progressListener;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(VersionInfo.class, new DetailedVersionInfoDeserializerOld())
                .registerTypeAdapter(Rule.Action.class, new ActionTypeAdapter())
                .create();

        this.fileDownloader = new FileDownloaderIO("TESLauncher/1.0.0");
    }

    public void download(String url, Path savePath, long expectedSize, ProgressListener progressListener) throws IOException {
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

            this.saveClientJson(version);

            //LOG.info("Downloading client...");
            System.out.println("Downloading client...");
            VersionInfo versionInfo = this.downloadClient(version);
            System.out.println("Downloaded client");

            //LOG.info("Downloading libraries...");
            System.out.println("Downloading libraries...");
            this.downloadLibraries(versionInfo);
            System.out.println("Downloaded libraries");

            //LOG.info("Extracting natives...");
            System.out.println("Extracting natives...");
            this.extractNatives();
            System.out.println("Extracted natives");

            System.out.println("Downloading assets...");
            this.downloadAssets(versionInfo);
            System.out.println("Downloaded assets");

            //LOG.info("Done");
            break;
        }
    }

    private void saveClientJson(VersionManifest.Version version) throws IOException {
        Path jsonFile = this.clientsDir.resolve(version.id).resolve(version.id + ".json");
        if (!Files.exists(jsonFile)) {
            Files.write(jsonFile, Http.get(version.url));
        }
    }

    private VersionInfo downloadClient(VersionManifest.Version version) throws IOException {
        Reader reader = MinecraftDownloader.getReader(Http.get(version.url));

        VersionInfo versionInfo = this.gson.fromJson(reader, VersionInfo.class);
        ClientDownload client = versionInfo.downloads.client;

        System.out.println("Downloading " + version.id + ".jar...");
        Path jarFile = this.clientsDir.resolve(version.id).resolve(version.id + ".jar");
        this.download(client.url, jarFile, client.size, this.progressListener);

        return versionInfo;
    }

    private void downloadLibraries(VersionInfo versionInfo) throws IOException {
        for (Library library : versionInfo.libraries) {
            LibraryDownloads downloads = library.downloads;
            DownloadArtifact artifact = downloads.artifact;
            Map<String, DownloadArtifact> classifiers = downloads.classifiers;

            if (artifact != null) {
                Path jarFile = this.librariesDir.resolve(artifact.path);
                this.download(artifact.url, jarFile, artifact.size, this.progressListener);
            }

            if (classifiers != null) {
                String key = "natives-" + EnumOS.getOsName();
                DownloadArtifact classifier = classifiers.get(key);

                if (classifier == null) {
                    key = key + "-" + EnumOS.getBits();
                    classifier = classifiers.get(key);
                }

                if (classifier != null) {
                    Path filePath = this.nativesDir.resolve(classifier.path);
                    this.download(classifier.url, filePath, classifier.size, this.progressListener);
                }
            }
        }
    }

    private void downloadAsset(Path filePath, AssetObject assetObject) throws IOException {
        String prefix = assetObject.hash.substring(0, 2);
        String url = MinecraftDownloader.RESOURCES + prefix + "/" + assetObject.hash;
        this.download(url, filePath, assetObject.size, this.progressListener);
    }

    private void downloadAssets(VersionInfo versionInfo) throws IOException {
        VersionAssetIndex vAssetIndex = versionInfo.assetIndex;

        if (vAssetIndex == null) {
            return;
        }

        Path assetsIndexFile = this.assetsDir.resolve("indexes").resolve(vAssetIndex.id + ".json");
        if (!Files.exists(assetsIndexFile)) {
            PathUtils.createDirectoryIfNotExists(assetsIndexFile.getParent());
            Files.write(assetsIndexFile, Http.get(vAssetIndex.url));
        }

        AssetIndex assetIndex = this.gson.fromJson(Files.newBufferedReader(assetsIndexFile), AssetIndex.class);

        assetIndex.objects.forEach((fileName, assetObject) -> {
            try {
                if (assetIndex.mapToResources) {
                    Path filePath = this.instanceResourcesDir.resolve(fileName);
                    this.downloadAsset(filePath, assetObject);
                } else if (assetIndex.virtual) {
                    Path filePath = this.assetsDir.resolve("virtual").resolve("legacy").resolve(fileName);
                    this.downloadAsset(filePath, assetObject);
                } else {
                    String prefix = assetObject.hash.substring(0, 2);
                    Path filePath = this.assetsDir.resolve("objects").resolve(prefix).resolve(assetObject.hash);
                    this.downloadAsset(filePath, assetObject);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static Reader getReader(byte[] bytes) {
        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8));
    }

    private void extractNatives() throws IOException {
        Path nativesDir = this.nativesDir;

        List<Path> paths;
        try (Stream<Path> files = Files.walk(nativesDir)) {
            paths = files.collect(Collectors.toList());
        }

        for (Path file : paths) {
            if (!file.endsWith(".jar") || !file.endsWith(".zip")) {
                continue;
            }

            if (Files.isRegularFile(file)) {
                try (ZipFile zipFile = new ZipFile(file.toFile())) {
                    zipFile.extractAll(nativesDir.normalize().toAbsolutePath().toString());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
