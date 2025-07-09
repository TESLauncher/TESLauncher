/*
 * TESLauncher - https://github.com/TESLauncher/TESLauncher
 * Copyright (C) 2023-2025 TESLauncher
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

package me.theentropyshard.teslauncher.minecraft.data;

import me.theentropyshard.teslauncher.minecraft.data.argument.Argument;
import me.theentropyshard.teslauncher.minecraft.data.argument.ArgumentType;
import me.theentropyshard.teslauncher.utils.ListUtils;
import me.theentropyshard.teslauncher.utils.MavenArtifact;

import java.time.OffsetDateTime;
import java.util.EnumMap;
import java.util.List;

public class Version {
    private AssetIndex assetIndex;
    private JavaVersion javaVersion;
    private String id;
    private String mainClass;
    private VersionType type;
    private OffsetDateTime releaseTime;
    private OffsetDateTime time;
    private String minecraftArguments;
    private String assets;
    private int complianceLevel;
    private EnumMap<DownloadType, Download> downloads;
    private List<Library> libraries;
    private EnumMap<ArgumentType, List<Argument>> arguments;

    public Version() {

    }

    public void addLibrary(Library library) {
        MavenArtifact artifact = MavenArtifact.parse(library.getName());

        this.libraries.removeIf(l -> {
            MavenArtifact a = MavenArtifact.parse(l.getName());

            return a.getGroupId().equals(artifact.getGroupId()) && a.getArtifactId().equals(artifact.getArtifactId());
        });

        this.libraries.add(library);
    }

    public AssetIndex getAssetIndex() {
        return this.assetIndex;
    }

    public void setAssetIndex(AssetIndex assetIndex) {
        this.assetIndex = assetIndex;
    }

    public JavaVersion getJavaVersion() {
        return this.javaVersion;
    }

    public void setJavaVersion(JavaVersion javaVersion) {
        this.javaVersion = javaVersion;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMainClass() {
        return this.mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public VersionType getType() {
        return this.type;
    }

    public void setType(VersionType type) {
        this.type = type;
    }

    public OffsetDateTime getReleaseTime() {
        return this.releaseTime;
    }

    public void setReleaseTime(OffsetDateTime releaseTime) {
        this.releaseTime = releaseTime;
    }

    public OffsetDateTime getTime() {
        return this.time;
    }

    public void setTime(OffsetDateTime time) {
        this.time = time;
    }

    public String getMinecraftArguments() {
        return this.minecraftArguments;
    }

    public void setMinecraftArguments(String minecraftArguments) {
        this.minecraftArguments = minecraftArguments;
    }

    public String getAssets() {
        return this.assets;
    }

    public void setAssets(String assets) {
        this.assets = assets;
    }

    public int getComplianceLevel() {
        return this.complianceLevel;
    }

    public void setComplianceLevel(int complianceLevel) {
        this.complianceLevel = complianceLevel;
    }

    public EnumMap<DownloadType, Download> getDownloads() {
        return this.downloads;
    }

    public void setDownloads(EnumMap<DownloadType, Download> downloads) {
        this.downloads = downloads;
    }

    public List<Library> getLibraries() {
        return this.libraries;
    }

    public void setLibraries(List<Library> libraries) {
        this.libraries = libraries;
    }

    public EnumMap<ArgumentType, List<Argument>> getArguments() {
        return this.arguments;
    }

    public void setArguments(EnumMap<ArgumentType, List<Argument>> arguments) {
        this.arguments = arguments;
    }

    public static final class AssetIndex {
        private String id;
        private String sha1;
        private long size;
        private long totalSize;
        private String url;

        public AssetIndex() {

        }

        public String getId() {
            return this.id;
        }

        public String getSha1() {
            return this.sha1;
        }

        public long getSize() {
            return this.size;
        }

        public long getTotalSize() {
            return this.totalSize;
        }

        public String getUrl() {
            return this.url;
        }
    }

    public static final class JavaVersion {
        private String component;
        private int majorVersion;

        public JavaVersion() {

        }

        public String getComponent() {
            return this.component;
        }

        public int getMajorVersion() {
            return this.majorVersion;
        }
    }

    public static final class Download {
        private String sha1;
        private long size;
        private String url;

        public Download() {

        }

        public String getSha1() {
            return this.sha1;
        }

        public long getSize() {
            return this.size;
        }

        public String getUrl() {
            return this.url;
        }
    }
}
