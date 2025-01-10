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

import java.util.HashMap;
import java.util.Map;

public enum VersionType {
    RELEASE("Release", "release"),
    SNAPSHOT("Snapshot", "snapshot"),
    OLD_BETA("Beta", "old_beta"),
    OLD_ALPHA("Alpha", "old_alpha");

    private static final Map<String, VersionType> lookup = new HashMap<>();

    static {
        for (VersionType type : VersionType.values()) {
            lookup.put(type.getJsonName(), type);
        }
    }

    public static VersionType getByName(String jsonName) {
        VersionType type = lookup.get(jsonName);

        if (type == null) {
            throw new IllegalArgumentException("jsonName: " + jsonName);
        }

        return type;
    }

    private final String readableName;
    private final String jsonName;

    VersionType(String readableName, String jsonName) {
        this.readableName = readableName;
        this.jsonName = jsonName;
    }

    @Override
    public String toString() {
        return this.getReadableName();
    }

    public String getReadableName() {
        return this.readableName;
    }

    public String getJsonName() {
        return this.jsonName;
    }
}
