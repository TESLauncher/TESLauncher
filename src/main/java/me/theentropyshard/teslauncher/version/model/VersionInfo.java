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

import me.theentropyshard.teslauncher.version.VersionType;

import java.time.OffsetDateTime;

public class VersionInfo {
    private final String id;
    private final VersionType type;
    private final OffsetDateTime releaseTime;
    private final String url;
    private final String sha1;
    private final boolean secure;

    public VersionInfo(String id, VersionType type, OffsetDateTime releaseTime, String url, String sha1, boolean secure) {
        this.id = id;
        this.type = type;
        this.releaseTime = releaseTime;
        this.url = url;
        this.sha1 = sha1;
        this.secure = secure;
    }

    @Override
    public String toString() {
        return "VersionInfo[id = " + this.id + ", type = " + this.type + ", releaseTime = " + this.releaseTime +
                ", secure = " + this.secure + ", sha1 = " + this.sha1 + ", url = " + this.url;
    }

    public String getId() {
        return this.id;
    }

    public VersionType getType() {
        return this.type;
    }

    public OffsetDateTime getReleaseTime() {
        return this.releaseTime;
    }

    public String getUrl() {
        return this.url;
    }

    public String getSha1() {
        return this.sha1;
    }

    public boolean isSecure() {
        return this.secure;
    }
}
