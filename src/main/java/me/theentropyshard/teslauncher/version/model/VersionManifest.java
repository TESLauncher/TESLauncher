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

package me.theentropyshard.teslauncher.version.model;

import java.util.List;

public class VersionManifest {
    private final String latestReleaseId;
    private final String latestSnapshotId;
    private final List<VersionInfo> versions;

    public VersionManifest(String latestReleaseId, String latestSnapshotId, List<VersionInfo> versions) {
        this.latestReleaseId = latestReleaseId;
        this.latestSnapshotId = latestSnapshotId;
        this.versions = versions;
    }

    public String getLatestReleaseId() {
        return this.latestReleaseId;
    }

    public String getLatestSnapshotId() {
        return this.latestSnapshotId;
    }

    public List<VersionInfo> getVersions() {
        return this.versions;
    }
}
