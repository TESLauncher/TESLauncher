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

package me.theentropyshard.teslauncher.minecraft;

import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.java.JavaManager;
import me.theentropyshard.teslauncher.network.HttpRequest;
import me.theentropyshard.teslauncher.network.download.DownloadList;
import me.theentropyshard.teslauncher.network.download.HttpDownload;
import me.theentropyshard.teslauncher.network.progress.ProgressNetworkInterceptor;
import me.theentropyshard.teslauncher.utils.EnumOS;
import me.theentropyshard.teslauncher.utils.FileUtils;
import me.theentropyshard.teslauncher.utils.Json;
import me.theentropyshard.teslauncher.utils.ListUtils;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import okhttp3.OkHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MinecraftDownloader {
    private static final Logger LOG = LogManager.getLogger(MinecraftDownloader.class);

    private static final String VER_MAN_V2 = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
    private static final String RESOURCES = "https://resources.download.minecraft.net/";

    private final Path versionsDir;
    private final Path assetsDir;
    private final Path librariesDir;
    private final Path nativesDir;
    private final Path instanceResourcesDir;
    private final MinecraftDownloadListener minecraftDownloadListener;

    public MinecraftDownloader(Path versionsDir, Path assetsDir, Path librariesDir, Path nativesDir,
                               Path instanceResourcesDir, MinecraftDownloadListener minecraftDownloadListener) {
        this.versionsDir = versionsDir;
        this.assetsDir = assetsDir;
        this.librariesDir = librariesDir;
        this.nativesDir = nativesDir;
        this.instanceResourcesDir = instanceResourcesDir;
        this.minecraftDownloadListener = minecraftDownloadListener;
    }

    public void downloadMinecraft(String versionId) throws IOException {
        FileUtils.createDirectoryIfNotExists(this.versionsDir.resolve(versionId));

        LOG.info("Getting Manifest...");

        VersionManifest manifest = MinecraftDownloader.getVersionManifest(this.versionsDir);
        if (manifest == null) {
            throw new IOException("Unable to deserialize version manifest");
        }

        VersionManifest.Version version = ListUtils.search(manifest.versions, v -> v.id.equals(versionId));
        if (version == null) {
            LOG.warn("Unable to find Minecraft " + versionId);
            return;
        }

        LOG.info("Found Minecraft " + versionId);

        LOG.info("Downloading client...");
        VersionInfo versionInfo = this.downloadClient(version);

        LOG.info("Downloading libraries...");
        List<Library> nativeLibraries = this.downloadLibraries(versionInfo);

        LOG.info("Extracting natives...");
        this.extractNatives(nativeLibraries);

        LOG.info("Downloading assets...");
        this.downloadAssets(versionInfo);

        LOG.info("Downloading Java...");
        this.downloadJava(versionInfo);
    }

    private static VersionManifest fetchAndSaveVersionManifest(Path manifestFile) throws IOException {
        try (HttpRequest request = new HttpRequest(TESLauncher.getInstance().getHttpClient())) {
            String string = request.asString(MinecraftDownloader.VER_MAN_V2);
            FileUtils.writeUtf8(manifestFile, string);
            return Json.parse(string, VersionManifest.class);
        }
    }

    public static VersionManifest getVersionManifest(Path versionsDir) throws IOException {
        Path manifestFile = versionsDir.resolve("version_manifest_v2.json");
        if (Files.exists(manifestFile)) {
            BasicFileAttributes basicFileAttributes = Files.readAttributes(manifestFile, BasicFileAttributes.class);
            String string = basicFileAttributes.lastModifiedTime().toString();
            if (OffsetDateTime.now().minus(Duration.ofHours(3)).isAfter(OffsetDateTime.parse(string))) {
                return MinecraftDownloader.fetchAndSaveVersionManifest(manifestFile);
            } else {
                return Json.parse(FileUtils.readUtf8(manifestFile), VersionManifest.class);
            }
        } else {
            return MinecraftDownloader.fetchAndSaveVersionManifest(manifestFile);
        }
    }

    private static String getJavaKey(VersionInfo versionInfo) {
        String javaKey;
        JavaVersion javaVersion = versionInfo.javaVersion;
        if (javaVersion == null) {
            try {
                String[] split = versionInfo.id.split("\\.");
                int minorVersion = Integer.parseInt(split[1]);

                if (minorVersion >= 17) {
                    javaKey = "java-runtime-gamma";
                } else {
                    javaKey = "jre-legacy";
                }
            } catch (Exception ignored) {
                javaKey = "jre-legacy";
            }
        } else {
            javaKey = javaVersion.component;
        }

        return javaKey;
    }

    private void downloadJava(VersionInfo versionInfo) throws IOException {
        String javaKey = MinecraftDownloader.getJavaKey(versionInfo);

        JavaManager javaManager = TESLauncher.getInstance().getJavaManager();

        if (javaManager.runtimeExists(javaKey)) {
            return;
        }

        this.minecraftDownloadListener.onStageChanged("Downloading Java Runtime");
        this.minecraftDownloadListener.onProgress(0, 0, 0);
        javaManager.downloadRuntime(javaKey, this.minecraftDownloadListener);
    }

    private void saveClientJson(VersionManifest.Version version, Path jsonFile) throws IOException {
        try (HttpRequest request = new HttpRequest(TESLauncher.getInstance().getHttpClient())) {
            FileUtils.writeUtf8(jsonFile, request.asString(version.url));
        }
    }

    private VersionInfo downloadClient(VersionManifest.Version version) throws IOException {
        VersionInfo versionInfo;

        Path jsonFile = this.versionsDir.resolve(version.id).resolve(version.id + ".json");
        if (!Files.exists(jsonFile)) {
            this.saveClientJson(version, jsonFile);
        }

        versionInfo = Json.parse(FileUtils.readUtf8(jsonFile), VersionInfo.class);

        ClientDownload client = versionInfo.downloads.client;

        Path jarFile = this.versionsDir.resolve(version.id).resolve(version.id + ".jar");

        if (Files.exists(jarFile) && Files.size(jarFile) == client.size) {
            return versionInfo;
        }

        this.minecraftDownloadListener.onProgress(0, 0, 0);
        this.minecraftDownloadListener.onStageChanged("Downloading client");

        OkHttpClient httpClient = TESLauncher.getInstance().getHttpClient().newBuilder()
                .addNetworkInterceptor(new ProgressNetworkInterceptor((contentLength, bytesRead, done) -> {
                    this.minecraftDownloadListener.onProgress(0, (int) contentLength, (int) bytesRead);
                }))
                .build();

        HttpDownload download = new HttpDownload.Builder()
                .httpClient(httpClient)
                .url(client.url)
                .expectedSize(client.size)
                .saveAs(jarFile)
                .build();

        download.execute();

        return versionInfo;
    }

    private DownloadArtifact getClassifier(Library library) {
        if (library.downloads.classifiers != null) {
            String key = "natives-" + EnumOS.getOsName();
            DownloadArtifact classifier = library.downloads.classifiers.get(key);

            if (classifier == null) {
                key = key + "-" + EnumOS.getBits();
                classifier = library.downloads.classifiers.get(key);
            }

            return classifier;
        }

        return null;
    }

    private List<Library> downloadLibraries(VersionInfo versionInfo) throws IOException {
        List<Library> nativeLibraries = new ArrayList<>();

        this.minecraftDownloadListener.onProgress(0, 0, 0);
        this.minecraftDownloadListener.onStageChanged("Downloading libraries");

        DownloadList downloadList = new DownloadList((total, completed) -> {
            this.minecraftDownloadListener.onProgress(0, total, completed);
        });

        for (Library library : versionInfo.libraries) {
            if (!RuleMatcher.applyOnThisPlatform(library)) {
                continue;
            }

            LibraryDownloads downloads = library.downloads;
            DownloadArtifact artifact = downloads.artifact;

            if (artifact != null) {
                Path jarFile = this.librariesDir.resolve(artifact.path);

                if (!Files.exists(jarFile) || Files.size(jarFile) != artifact.size) {
                    HttpDownload download = new HttpDownload.Builder()
                            .httpClient(TESLauncher.getInstance().getHttpClient())
                            .url(artifact.url)
                            .expectedSize(artifact.size)
                            .saveAs(jarFile)
                            .build();
                    downloadList.add(download);
                }
            }

            DownloadArtifact classifier = this.getClassifier(library);
            if (classifier != null) {
                nativeLibraries.add(library);
                Path filePath = this.librariesDir.resolve(classifier.path);

                if (!Files.exists(filePath) || Files.size(filePath) != classifier.size) {
                    HttpDownload download = new HttpDownload.Builder()
                            .httpClient(TESLauncher.getInstance().getHttpClient())
                            .url(classifier.url)
                            .expectedSize(classifier.size)
                            .saveAs(filePath)
                            .build();
                    downloadList.add(download);
                }
            }
        }

        if (downloadList.size() > 0) {
            downloadList.downloadAll();
        }

        return nativeLibraries;
    }

    private void downloadAsset(DownloadList downloadList, Path filePath, AssetObject assetObject) {
        String prefix = assetObject.hash.substring(0, 2);
        String url = MinecraftDownloader.RESOURCES + prefix + "/" + assetObject.hash;

        HttpDownload download = new HttpDownload.Builder()
                .httpClient(TESLauncher.getInstance().getHttpClient())
                .url(url)
                .expectedSize(assetObject.size)
                .saveAs(filePath)
                .build();
        downloadList.add(download);
    }

    private void downloadAssets(VersionInfo versionInfo) throws IOException {
        VersionAssetIndex vAssetIndex = versionInfo.assetIndex;

        if (vAssetIndex == null) {
            return;
        }

        Path assetsIndexFile = this.assetsDir.resolve("indexes").resolve(vAssetIndex.id + ".json");
        if (!Files.exists(assetsIndexFile)) {
            FileUtils.createDirectoryIfNotExists(assetsIndexFile.getParent());

            try (HttpRequest request = new HttpRequest(TESLauncher.getInstance().getHttpClient())) {
                FileUtils.writeUtf8(assetsIndexFile, request.asString(vAssetIndex.url));
            }
        }

        AssetIndex assetIndex = Json.parse(FileUtils.readUtf8(assetsIndexFile), AssetIndex.class);

        this.minecraftDownloadListener.onProgress(0, 0, 0);
        this.minecraftDownloadListener.onStageChanged("Downloading assets");

        DownloadList downloadList = new DownloadList(((total, completed) -> {
            this.minecraftDownloadListener.onProgress(0, total, completed);
        }));

        for (Map.Entry<String, AssetObject> entry : assetIndex.objects.entrySet()) {
            String fileName = entry.getKey();
            AssetObject assetObject = entry.getValue();

            Path filePath;

            if (assetIndex.mapToResources) {
                filePath = this.instanceResourcesDir.resolve(fileName);
            } else if (assetIndex.virtual) {
                filePath = this.assetsDir.resolve("virtual").resolve("legacy").resolve(fileName);
            } else {
                String prefix = assetObject.hash.substring(0, 2);
                filePath = this.assetsDir.resolve("objects").resolve(prefix).resolve(assetObject.hash);
            }

            if (!Files.exists(filePath) || Files.size(filePath) != assetObject.size) {
                this.downloadAsset(downloadList, filePath, assetObject);
            }
        }

        if (downloadList.size() > 0) {
            downloadList.downloadAll();
        }
    }

    private boolean excludeFromExtract(Library library, String fileName) {
        if (library.extract == null) {
            return false;
        }

        for (String excludeName : library.extract.exclude) {
            if (fileName.startsWith(excludeName)) {
                return true;
            }
        }

        return false;
    }

    private void extractNatives(List<Library> nativeLibraries) throws IOException {
        for (Library library : nativeLibraries) {
            DownloadArtifact classifier = this.getClassifier(library);
            if (classifier == null) {
                continue;
            }

            String extractPath = this.nativesDir.normalize().toAbsolutePath().toString();
            Path path = this.librariesDir.resolve(classifier.path).toAbsolutePath();

            try (ZipFile zipFile = new ZipFile(path.toFile())) {
                List<FileHeader> fileHeaders = zipFile.getFileHeaders();
                for (FileHeader fileHeader : fileHeaders) {
                    if (this.excludeFromExtract(library, fileHeader.getFileName())) {
                        continue;
                    }

                    if (Files.exists(this.nativesDir.resolve(fileHeader.getFileName()))) {
                        continue;
                    }

                    zipFile.extractFile(fileHeader, extractPath);
                }
            }
        }
    }
}
