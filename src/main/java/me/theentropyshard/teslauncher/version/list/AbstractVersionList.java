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

package me.theentropyshard.teslauncher.version.list;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.theentropyshard.teslauncher.gson.VersionManifestDeserializer;
import me.theentropyshard.teslauncher.version.VersionType;
import me.theentropyshard.teslauncher.version.model.VersionInfo;
import me.theentropyshard.teslauncher.version.model.VersionManifest;

import java.io.IOException;
import java.util.*;

public abstract class AbstractVersionList implements VersionList {
    private static final String LATEST_BETA_ID = "b1.8.1";
    private static final String LATEST_ALPHA_ID = "a1.2.6";

    private final List<VersionInfo> versions;
    private final Map<String, VersionInfo> versionsById;
    private final Map<VersionType, VersionInfo> latestVersions;
    private final Gson gson;

    public AbstractVersionList() {
        this.versions = new ArrayList<>();
        this.versionsById = new HashMap<>();
        this.latestVersions = new EnumMap<>(VersionType.class);
        this.gson = new GsonBuilder()
                .registerTypeAdapter(VersionManifest.class, new VersionManifestDeserializer())
                .create();
    }

    public abstract String getJson() throws IOException;

    @Override
    public void load() throws IOException {
        String json = this.getJson();
        VersionManifest versionManifest = this.gson.fromJson(json, VersionManifest.class);
        VersionInfo[] versions = versionManifest.getVersions();

        for (VersionInfo version : versions) {
            this.versions.add(version);
            this.versionsById.put(version.getId(), version);
        }

        this.setLatestVersion(this.getVersionById(versionManifest.getLatestReleaseId()));
        this.setLatestVersion(this.getVersionById(versionManifest.getLatestSnapshotId()));
        this.setLatestVersion(this.getVersionById(AbstractVersionList.LATEST_BETA_ID));
        this.setLatestVersion(this.getVersionById(AbstractVersionList.LATEST_ALPHA_ID));
    }

    @Override
    public void reload() throws IOException {
        this.versions.clear();
        this.versionsById.clear();
        this.latestVersions.clear();
        this.load();
    }

    @Override
    public VersionInfo getVersionById(String id) {
        return this.versionsById.get(id);
    }

    @Override
    public VersionInfo getLatestVersion(VersionType type) {
        return this.latestVersions.get(type);
    }

    @Override
    public void setLatestVersion(VersionInfo info) {
        this.latestVersions.put(info.getType(), info);
    }

    @Override
    public List<VersionInfo> getVersions() {
        return new ArrayList<>(this.versions);
    }
}
