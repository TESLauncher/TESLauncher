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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.gson.ActionTypeAdapter;
import me.theentropyshard.teslauncher.gson.DetailedVersionInfoDeserializer;
import me.theentropyshard.teslauncher.java.JavaManager;
import me.theentropyshard.teslauncher.network.HttpClients;
import me.theentropyshard.teslauncher.network.HttpRequest;
import me.theentropyshard.teslauncher.network.download.DownloadList;
import me.theentropyshard.teslauncher.network.download.HttpDownload;
import me.theentropyshard.teslauncher.utils.EnumOS;
import me.theentropyshard.teslauncher.utils.FileUtils;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MinecraftDownloader {
    private static final String VER_MAN_V2 = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
    private static final String RESOURCES = "https://resources.download.minecraft.net/";
    private final Gson gson;
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
        this.gson = new GsonBuilder()
                .registerTypeAdapter(VersionInfo.class, new DetailedVersionInfoDeserializer(TESLauncher.getInstance()))
                .registerTypeAdapter(Rule.Action.class, new ActionTypeAdapter())
                .create();
    }

    public void downloadMinecraft(String versionId) throws IOException {
        FileUtils.createDirectoryIfNotExists(this.versionsDir.resolve(versionId));

        //LOG.info("Downloading Minecraft " + versionId);

        VersionManifest manifest;
        try (HttpRequest request = new HttpRequest(TESLauncher.getInstance().getHttpClient(), this.gson)) {
            manifest = request.asObject(MinecraftDownloader.VER_MAN_V2, VersionManifest.class);
        }

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
            List<Library> nativeLibraries = this.downloadLibraries(versionInfo);
            System.out.println("Downloaded libraries");

            //LOG.info("Extracting natives...");
            System.out.println("Extracting natives...");
            this.extractNatives(nativeLibraries);
            System.out.println("Extracted natives");

            System.out.println("Downloading assets...");
            this.downloadAssets(versionInfo);
            System.out.println("Downloaded assets");

            System.out.println("Downloading Java...");
            this.downloadJava(versionInfo);
            System.out.println("Downloaded Java");

            //LOG.info("Done");
            break;
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

        this.minecraftDownloadListener.onStageChanged("Downloading Java Runtime");
        this.minecraftDownloadListener.onProgress(0, 0, 0);
        javaManager.downloadRuntime(javaKey, this.minecraftDownloadListener);
    }

    private void saveClientJson(VersionManifest.Version version) throws IOException {
        Path jsonFile = this.versionsDir.resolve(version.id).resolve(version.id + ".json");
        if (!Files.exists(jsonFile)) {
            try (HttpRequest request = new HttpRequest(TESLauncher.getInstance().getHttpClient())) {
                Files.write(jsonFile, request.asBytes(version.url));
            }
        }
    }

    private VersionInfo downloadClient(VersionManifest.Version version) throws IOException {
        VersionInfo versionInfo;
        try (HttpRequest request = new HttpRequest(TESLauncher.getInstance().getHttpClient(), this.gson)) {
            versionInfo = request.asObject(version.url, VersionInfo.class);
        }

        ClientDownload client = versionInfo.downloads.client;

        System.out.println("Downloading " + version.id + ".jar...");
        Path jarFile = this.versionsDir.resolve(version.id).resolve(version.id + ".jar");
        this.minecraftDownloadListener.onProgress(0, 0, 0);
        this.minecraftDownloadListener.onStageChanged("Downloading client");

        OkHttpClient httpClient = HttpClients.withProgress(TESLauncher.getInstance().getHttpClient(), (contentLength, bytesRead, done) -> {
            this.minecraftDownloadListener.onProgress(0, (int) contentLength, (int) bytesRead);
        });

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

                HttpDownload download = new HttpDownload.Builder()
                        .httpClient(TESLauncher.getInstance().getHttpClient())
                        .url(artifact.url)
                        .expectedSize(artifact.size)
                        .saveAs(jarFile)
                        .build();
                downloadList.add(download);
            }

            DownloadArtifact classifier = this.getClassifier(library);
            if (classifier != null) {
                nativeLibraries.add(library);
                Path filePath = this.librariesDir.resolve(classifier.path);

                HttpDownload download = new HttpDownload.Builder()
                        .httpClient(TESLauncher.getInstance().getHttpClient())
                        .url(classifier.url)
                        .expectedSize(classifier.size)
                        .saveAs(filePath)
                        .build();
                downloadList.add(download);
            }
        }

        downloadList.downloadAll();

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
                Files.write(assetsIndexFile, request.asBytes(vAssetIndex.url));
            }
        }

        AssetIndex assetIndex = this.gson.fromJson(Files.newBufferedReader(assetsIndexFile), AssetIndex.class);

        this.minecraftDownloadListener.onProgress(0, 0, 0);
        this.minecraftDownloadListener.onStageChanged("Downloading assets");

        DownloadList downloadList = new DownloadList(((total, completed) -> {
            this.minecraftDownloadListener.onProgress(0, total, completed);
        }));

        for (Map.Entry<String, AssetObject> entry : assetIndex.objects.entrySet()) {
            String fileName = entry.getKey();
            AssetObject assetObject = entry.getValue();

            if (assetIndex.mapToResources) {
                Path filePath = this.instanceResourcesDir.resolve(fileName);
                this.downloadAsset(downloadList, filePath, assetObject);
            } else if (assetIndex.virtual) {
                Path filePath = this.assetsDir.resolve("virtual").resolve("legacy").resolve(fileName);
                this.downloadAsset(downloadList, filePath, assetObject);
            } else {
                String prefix = assetObject.hash.substring(0, 2);
                Path filePath = this.assetsDir.resolve("objects").resolve(prefix).resolve(assetObject.hash);
                this.downloadAsset(downloadList, filePath, assetObject);
            }
        }

        downloadList.downloadAll();
    }

    private boolean excludeFromExtract(Library library, String fileName) {
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
