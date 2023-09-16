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

import me.theentropyshard.teslauncher.version.VersionType;
import me.theentropyshard.teslauncher.version.model.VersionInfo;

import java.io.IOException;
import java.util.List;

public interface VersionList {
    void load() throws IOException;

    void reload() throws IOException;

    VersionInfo getLatestVersion(VersionType type);

    void setLatestVersion(VersionInfo info);

    VersionInfo getVersionById(String id);

    List<VersionInfo> getVersions();
}
