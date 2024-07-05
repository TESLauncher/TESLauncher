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

package me.theentropyshard.teslauncher.minecraft.data;

import java.time.OffsetDateTime;
import java.util.List;

public class VersionManifest {
    private Latest latest;
    private List<Version> versions;

    public VersionManifest() {

    }

    public Latest getLatest() {
        return this.latest;
    }

    public List<Version> getVersions() {
        return this.versions;
    }

    public static final class Latest {
        private String release;
        private String snapshot;

        public Latest() {

        }

        public String getRelease() {
            return this.release;
        }

        public String getSnapshot() {
            return this.snapshot;
        }
    }

    public static final class Version {
        private String id;
        private VersionType type;
        private String url;
        private OffsetDateTime releaseTime;
        private String sha1;
        private int complianceLevel;

        public Version() {

        }

        public String getId() {
            return this.id;
        }

        public VersionType getType() {
            return this.type;
        }

        public String getUrl() {
            return this.url;
        }

        public OffsetDateTime getReleaseTime() {
            return this.releaseTime;
        }

        public String getSha1() {
            return this.sha1;
        }

        public int getComplianceLevel() {
            return this.complianceLevel;
        }
    }
}
