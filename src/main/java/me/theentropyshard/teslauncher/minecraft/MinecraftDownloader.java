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

package me.theentropyshard.teslauncher.minecraft;

import com.google.gson.JsonObject;
import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.network.HttpRequest;
import me.theentropyshard.teslauncher.network.download.DownloadList;
import me.theentropyshard.teslauncher.network.download.HttpDownload;
import me.theentropyshard.teslauncher.network.progress.ProgressNetworkInterceptor;
import me.theentropyshard.teslauncher.utils.FileUtils;
import me.theentropyshard.teslauncher.utils.HashUtils;
import me.theentropyshard.teslauncher.utils.ListUtils;
import me.theentropyshard.teslauncher.utils.OperatingSystem;
import me.theentropyshard.teslauncher.utils.json.Json;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import okhttp3.OkHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MinecraftDownloader {
    private static final Logger LOG = LogManager.getLogger(MinecraftDownloader.class);

    private static final Duration MANIFEST_UPDATE_INTERVAL = Duration.ofHours(12);

    private final Path versionsDir;
    private final Path assetsDir;
    private final Path librariesDir;
    private final Path nativesDir;
    private final Path runtimesDir;
    private final Path instanceResourcesDir;
    private final MinecraftDownloadListener minecraftDownloadListener;

    public MinecraftDownloader(Path versionsDir, Path assetsDir, Path librariesDir, Path nativesDir, Path runtimesDir,
                               Path instanceResourcesDir, MinecraftDownloadListener minecraftDownloadListener) {
        this.versionsDir = versionsDir;
        this.assetsDir = assetsDir;
        this.librariesDir = librariesDir;
        this.nativesDir = nativesDir;
        this.runtimesDir = runtimesDir;
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

        VersionManifest.Version manifestVersion = ListUtils.search(manifest.getVersions(), v -> v.getId().equals(versionId));
        if (manifestVersion == null) {
            LOG.warn("Unable to find Minecraft " + versionId);
            return;
        }

        LOG.info("Found Minecraft " + versionId);

        if (TESLauncher.getInstance().getSettings().useDownloadDialog) {
            LOG.info("Downloading client...");
            this.minecraftDownloadListener.onProgress(0, 0);
            this.minecraftDownloadListener.onStageChanged("Downloading client");
            DownloadList clientList = new DownloadList(this.minecraftDownloadListener::onProgress);
            Version version = this.downloadClient(manifestVersion, clientList);
            clientList.downloadAll();

            LOG.info("Downloading libraries...");
            this.minecraftDownloadListener.onProgress(0, 0);
            this.minecraftDownloadListener.onStageChanged("Downloading libraries");
            DownloadList librariesList = new DownloadList(this.minecraftDownloadListener::onProgress);
            List<Library> nativeLibraries = this.downloadLibraries(version, librariesList);
            librariesList.downloadAll();

            LOG.info("Extracting natives...");
            this.minecraftDownloadListener.onProgress(0, 0);
            this.minecraftDownloadListener.onStageChanged("Extracting natives");
            this.extractNatives(nativeLibraries);

            LOG.info("Downloading assets...");
            this.minecraftDownloadListener.onProgress(0, 0);
            this.minecraftDownloadListener.onStageChanged("Downloading assets");
            DownloadList assetsList = new DownloadList(this.minecraftDownloadListener::onProgress);
            this.downloadAssets(version, assetsList);
            assetsList.downloadAll();

            LOG.info("Downloading Java...");
            this.minecraftDownloadListener.onProgress(0, 0);
            this.minecraftDownloadListener.onStageChanged("Downloading Java");
            DownloadList javaList = new DownloadList(this.minecraftDownloadListener::onProgress);
            this.downloadJava(version, javaList);
            javaList.downloadAll();

            this.minecraftDownloadListener.onFinish();
        } else {
            this.minecraftDownloadListener.onProgress(0, 0);
            DownloadList list = new DownloadList(this.minecraftDownloadListener::onProgress);

            LOG.info("Downloading client...");
            Version version = this.downloadClient(manifestVersion, list);

            LOG.info("Downloading libraries...");
            List<Library> nativeLibraries = this.downloadLibraries(version, list);

            LOG.info("Extracting natives...");
            this.extractNatives(nativeLibraries);

            LOG.info("Downloading assets...");
            this.downloadAssets(version, list);

            LOG.info("Downloading Java...");
            this.downloadJava(version, list);

            list.downloadAll();

            this.minecraftDownloadListener.onFinish();
        }
    }

    private static VersionManifest fetchAndSaveVersionManifest(Path manifestFile) throws IOException {
        try (HttpRequest request = new HttpRequest(TESLauncher.getInstance().getHttpClient())) {
            String string = request.asString(ApiUrls.VERSION_MANIFEST);
            FileUtils.writeUtf8(manifestFile, string);
            return Json.parse(string, VersionManifest.class);
        }
    }

    public static VersionManifest getVersionManifest(Path versionsDir) throws IOException {
        return MinecraftDownloader.getVersionManifest(versionsDir, false);
    }

    public static VersionManifest getVersionManifest(Path versionsDir, boolean forceNetwork) throws IOException {
        Path manifestFile = versionsDir.resolve("version_manifest_v2.json");

        if (forceNetwork) {
            return MinecraftDownloader.fetchAndSaveVersionManifest(manifestFile);
        }

        if (Files.exists(manifestFile)) {
            VersionManifest manifest = Json.parse(FileUtils.readUtf8(manifestFile), VersionManifest.class);

            BasicFileAttributes basicFileAttributes = Files.readAttributes(manifestFile, BasicFileAttributes.class);
            OffsetDateTime lastModified = OffsetDateTime.parse(basicFileAttributes.lastModifiedTime().toString());
            if (OffsetDateTime.now().minus(MinecraftDownloader.MANIFEST_UPDATE_INTERVAL).isAfter(lastModified)) {
                try {
                    return MinecraftDownloader.fetchAndSaveVersionManifest(manifestFile);
                } catch (IOException e) {
                    LOG.error("Could not fetch and save version manifest to {}", manifestFile, e);

                    return manifest;
                }
            } else {
                return manifest;
            }
        } else {
            return MinecraftDownloader.fetchAndSaveVersionManifest(manifestFile);
        }
    }

    private static String getJavaKey(Version version) {
        String javaKey;
        Version.JavaVersion javaVersion = version.getJavaVersion();
        if (javaVersion == null) {
            try {
                String[] split = version.getId().split("\\.");
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
            javaKey = javaVersion.getComponent();
        }

        return javaKey;
    }

    private void downloadJava(Version version, DownloadList javaList) throws IOException {
        String javaKey = MinecraftDownloader.getJavaKey(version);

        /*if (this.runtimeExists(javaKey)) { TODO: implement proper check
            return;
        }*/

        this.downloadRuntime(javaKey, javaList);
    }

    private void saveClientJson(VersionManifest.Version version, Path jsonFile) throws IOException {
        try (HttpRequest request = new HttpRequest(TESLauncher.getInstance().getHttpClient())) {
            FileUtils.writeUtf8(jsonFile, request.asString(version.getUrl()));
        }
    }

    private Version downloadClient(VersionManifest.Version manifestVersion, DownloadList clientList) throws IOException {
        Version version;

        Path jsonFile = this.versionsDir.resolve(manifestVersion.getId()).resolve(manifestVersion.getId() + ".json");
        if (!Files.exists(jsonFile)) {
            this.saveClientJson(manifestVersion, jsonFile);
        }

        version = Json.parse(FileUtils.readUtf8(jsonFile), Version.class);

        Version.Download client = version.getDownloads().get(DownloadType.CLIENT);

        Path jarFile = this.versionsDir.resolve(version.getId()).resolve(version.getId() + ".jar");

        if (Files.exists(jarFile) && Files.size(jarFile) == client.getSize()) {
            return version;
        }

        OkHttpClient httpClient = TESLauncher.getInstance().getHttpClient().newBuilder()
                .addNetworkInterceptor(new ProgressNetworkInterceptor((contentLength, bytesRead, bytesThisTime, done) -> {
                    this.minecraftDownloadListener.onProgress(contentLength, bytesRead);
                }))
                .build();

        HttpDownload download = new HttpDownload.Builder()
                .httpClient(httpClient)
                .url(client.getUrl())
                .expectedSize(client.getSize())
                .sha1(client.getSha1())
                .saveAs(jarFile)
                .build();

        clientList.add(download);

        return version;
    }

    public static String getMcName() {
        switch (OperatingSystem.getCurrent()) {
            case WINDOWS:
                return "windows";
            case LINUX:
                return "linux";
            case MACOS:
                return "osx";
            case UNKNOWN:
                throw new RuntimeException("Unsupported OS: " + OperatingSystem.getName());
            default:
                throw new RuntimeException("Unreachable");
        }
    }

    private static String getQualifierOS() {
        String qualifier;

        if (OperatingSystem.isArm()) {
            if (OperatingSystem.is64Bit()) {
                qualifier = "arm64";
            } else {
                qualifier = "arm32";
            }
        } else {
            qualifier = OperatingSystem.getArch();
        }

        return qualifier;
    }

    private Library.Artifact getClassifier(Library library) {
        Map<String, Library.Artifact> classifiers = library.getDownloads().getClassifiers();

        if (classifiers != null) {
            String key = "natives-" + MinecraftDownloader.getMcName();
            Library.Artifact classifier = classifiers.get(key);

            if (classifier == null) {
                classifier = classifiers.get(key + "-" + MinecraftDownloader.getQualifierOS());
            }

            return classifier;
        }

        return null;
    }

    private List<Library> downloadLibraries(Version version, DownloadList librariesList) throws IOException {
        List<Library> nativeLibraries = new ArrayList<>();

        for (Library library : version.getLibraries()) {
            if (!RuleMatcher.applyOnThisPlatform(library)) {
                continue;
            }

            if (!OperatingSystem.isArm()) {
                if (OperatingSystem.is64Bit() && library.getName().endsWith("arm64")) {
                    continue;
                } else if (library.getName().endsWith("x86")) {
                    continue;
                }
            }

            Library.DownloadList downloads = library.getDownloads();
            Library.Artifact artifact = downloads.getArtifact();

            if (artifact != null) {
                Path jarFile = this.librariesDir.resolve(artifact.getPath());

                if (!Files.exists(jarFile) || Files.size(jarFile) != artifact.getSize()) {
                    HttpDownload download = new HttpDownload.Builder()
                            .httpClient(TESLauncher.getInstance().getHttpClient())
                            .url(artifact.getUrl())
                            .sha1(artifact.getSha1())
                            .expectedSize(artifact.getSize())
                            .saveAs(jarFile)
                            .build();
                    librariesList.add(download);
                }
            }

            Library.Artifact classifier = this.getClassifier(library);
            if (classifier != null) {
                nativeLibraries.add(library);
                Path filePath = this.librariesDir.resolve(classifier.getPath());

                if (!Files.exists(filePath) || Files.size(filePath) != classifier.getSize()) {
                    HttpDownload download = new HttpDownload.Builder()
                            .httpClient(TESLauncher.getInstance().getHttpClient())
                            .url(classifier.getUrl())
                            .sha1(classifier.getSha1())
                            .expectedSize(classifier.getSize())
                            .saveAs(filePath)
                            .build();
                    librariesList.add(download);
                }
            }
        }

        return nativeLibraries;
    }

    private void downloadAssets(Version version, DownloadList assetsList) throws IOException {
        Version.AssetIndex vAssetIndex = version.getAssetIndex();

        if (vAssetIndex == null) {
            return;
        }

        Path assetsIndexFile = this.assetsDir.resolve("indexes").resolve(vAssetIndex.getId() + ".json");
        if (!Files.exists(assetsIndexFile)) {
            try (HttpRequest request = new HttpRequest(TESLauncher.getInstance().getHttpClient())) {
                FileUtils.writeUtf8(assetsIndexFile, request.asString(vAssetIndex.getUrl()));
            }
        }

        AssetIndex assetIndex = Json.parse(FileUtils.readUtf8(assetsIndexFile), AssetIndex.class);

        for (Map.Entry<String, AssetObject> entry : assetIndex.getObjects().entrySet()) {
            String fileName = entry.getKey();
            AssetObject assetObject = entry.getValue();

            HttpDownload.Builder builder = new HttpDownload.Builder()
                    .httpClient(TESLauncher.getInstance().getHttpClient())
                    .url(ApiUrls.RESOURCES + assetObject.getPrefix() + "/" + assetObject.getHash())
                    .expectedSize(assetObject.getSize())
                    .sha1(assetObject.getHash());

            Path saveAs;
            Path copyTo = null;

            if (assetIndex.isMapToResources()) {
                saveAs = this.instanceResourcesDir.resolve(fileName);

                Path resourcesFile = this.assetsDir.resolve("resources").resolve(fileName);

                if (!Files.exists(saveAs)) {
                    if (Files.exists(resourcesFile) &&
                            Files.size(resourcesFile) == assetObject.getSize() &&
                            HashUtils.sha1(resourcesFile).equals(assetObject.getHash())) {

                        FileUtils.createDirectoryIfNotExists(saveAs.getParent());
                        Files.copy(resourcesFile, saveAs, StandardCopyOption.REPLACE_EXISTING);

                        continue;
                    } else {
                        copyTo = saveAs;
                        saveAs = resourcesFile;
                    }
                } else {
                    if (Files.size(saveAs) == assetObject.getSize() && HashUtils.sha1(saveAs).equals(assetObject.getHash())) {
                        continue;
                    } else {
                        copyTo = saveAs;
                        saveAs = resourcesFile;
                    }
                }
            } else if (assetIndex.isVirtual()) {
                saveAs = this.assetsDir.resolve("virtual").resolve(vAssetIndex.getId()).resolve(fileName);
            } else {
                saveAs = this.assetsDir.resolve("objects").resolve(assetObject.getPrefix()).resolve(assetObject.getHash());
            }

            FileUtils.createDirectoryIfNotExists(saveAs.getParent());
            builder.saveAs(saveAs);
            if (copyTo != null) {
                FileUtils.createDirectoryIfNotExists(copyTo.getParent());
                builder.copyTo(copyTo);
            }
            assetsList.add(builder.build());
        }
    }

    private boolean excludeFromExtract(Library library, String fileName) {
        if (library.getExtract() == null) {
            return false;
        }

        for (String excludeName : library.getExtract().getExclude()) {
            if (fileName.startsWith(excludeName)) {
                return true;
            }
        }

        return false;
    }

    private void extractNatives(List<Library> nativeLibraries) throws IOException {
        for (Library library : nativeLibraries) {
            Library.Artifact classifier = this.getClassifier(library);
            if (classifier == null) {
                continue;
            }

            String extractPath = this.nativesDir.normalize().toAbsolutePath().toString();
            Path path = this.librariesDir.resolve(classifier.getPath()).toAbsolutePath();

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
                    return MinecraftDownloader.fetchAndSaveAllRuntimes(runtimesFile);
                } catch (IOException e) {
                    LOG.error("Could not fetch and save all runtimes to {}", runtimesFile, e);

                    return jsonObject;
                }
            } else {
                return jsonObject;
            }
        } else {
            return MinecraftDownloader.fetchAndSaveAllRuntimes(runtimesFile);
        }
    }

    public void downloadRuntime(String componentName, DownloadList javaList) throws IOException {
        Path componentDir = this.runtimesDir.resolve(componentName);

        JsonObject osObject = MinecraftDownloader.getAllRuntimesObject(this.runtimesDir.resolve("all_runtimes.json"));

        String jreOsName = MinecraftDownloader.getJreOsName();
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
        Path componentDir = this.runtimesDir.resolve(componentName);

        if (OperatingSystem.isMacOS()) {
            componentDir = componentDir.resolve("jre.bundle").resolve("Contents").resolve("Home");
        }

        return componentDir
                .resolve("bin")
                .resolve(OperatingSystem.getCurrent().getJavaExecutableName())
                .toString();
    }
}
