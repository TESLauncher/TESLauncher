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

package me.theentropyshard.teslauncher.utils;

public class MavenArtifact {
    private final String groupId;
    private final String artifactId;
    private final String version;

    public MavenArtifact(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public static MavenArtifact parse(String s) {
        String[] parts = s.split(":");

        if (parts.length != 3) {
            throw new IllegalArgumentException("Unsupported maven notation: " + s);
        }

        return new MavenArtifact(parts[0], parts[1], parts[2]);
    }

    public String createPath(String extension) {
        return this.groupId + "/" + this.artifactId + "/" + this.version + "/" + this.artifactId + "-" + this.version + "." + extension;
    }

    public String createJarUrl(String repositoryBase) {
        return repositoryBase + "/" + this.createPath("jar");
    }

    @Override
    public String toString() {
        return this.groupId + ":" + this.artifactId + ":" + this.version;
    }

    public String getGroupId() {
        return this.groupId;
    }

    public String getArtifactId() {
        return this.artifactId;
    }

    public String getVersion() {
        return this.version;
    }
}
