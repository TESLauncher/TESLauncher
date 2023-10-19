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
import me.theentropyshard.teslauncher.http.FileDownloader;
import me.theentropyshard.teslauncher.http.FileDownloaderIO;
import me.theentropyshard.teslauncher.http.ProgressListener;
import me.theentropyshard.teslauncher.utils.EnumOS;
import me.theentropyshard.teslauncher.utils.Http;
import me.theentropyshard.teslauncher.utils.PathUtils;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MinecraftDownloader {
    private static final String VER_MAN_V2 = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
    private static final String RESOURCES = "https://resources.download.minecraft.net/";
    private final Gson gson;
    private final Path versionsDir;
    private final Path assetsDir;
    private final Path librariesDir;
    private final Path nativesDir;
    private final Path instanceResourcesDir;
    private final ProgressListener progressListener;

    private final FileDownloader fileDownloader;

    public MinecraftDownloader(Path versionsDir, Path assetsDir, Path librariesDir, Path nativesDir,
                               Path instanceResourcesDir, ProgressListener progressListener) {
        this.versionsDir = versionsDir;
        this.assetsDir = assetsDir;
        this.librariesDir = librariesDir;
        this.nativesDir = nativesDir;
        this.instanceResourcesDir = instanceResourcesDir;
        this.progressListener = progressListener;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(VersionInfo.class, new DetailedVersionInfoDeserializer(TESLauncher.getInstance()))
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
        PathUtils.createDirectoryIfNotExists(this.versionsDir.resolve(versionId));

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
            List<Library> nativeLibraries = this.downloadLibraries(versionInfo);
            System.out.println("Downloaded libraries");

            //LOG.info("Extracting natives...");
            System.out.println("Extracting natives...");
            this.extractNatives(nativeLibraries);
            System.out.println("Extracted natives");

            System.out.println("Downloading assets...");
            this.downloadAssets(versionInfo);
            System.out.println("Downloaded assets");

            //LOG.info("Done");
            break;
        }
    }

    private void saveClientJson(VersionManifest.Version version) throws IOException {
        Path jsonFile = this.versionsDir.resolve(version.id).resolve(version.id + ".json");
        if (!Files.exists(jsonFile)) {
            Files.write(jsonFile, Http.get(version.url));
        }
    }

    private VersionInfo downloadClient(VersionManifest.Version version) throws IOException {
        Reader reader = MinecraftDownloader.getReader(Http.get(version.url));

        VersionInfo versionInfo = this.gson.fromJson(reader, VersionInfo.class);
        ClientDownload client = versionInfo.downloads.client;

        System.out.println("Downloading " + version.id + ".jar...");
        Path jarFile = this.versionsDir.resolve(version.id).resolve(version.id + ".jar");
        this.download(client.url, jarFile, client.size, this.progressListener);

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

        for (Library library : versionInfo.libraries) {
            if (!RuleMatcher.applyOnThisPlatform(library)) {
                continue;
            }

            LibraryDownloads downloads = library.downloads;
            DownloadArtifact artifact = downloads.artifact;

            if (artifact != null) {
                Path jarFile = this.librariesDir.resolve(artifact.path);
                this.download(artifact.url, jarFile, artifact.size, this.progressListener);
            }

            DownloadArtifact classifier = this.getClassifier(library);
            if (classifier != null) {
                nativeLibraries.add(library);
                Path filePath = this.librariesDir.resolve(classifier.path);
                this.download(classifier.url, filePath, classifier.size, this.progressListener);
            }
        }

        return nativeLibraries;
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
