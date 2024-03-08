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

import java.util.HashMap;
import java.util.Map;

public enum VersionType {
    RELEASE("release"),
    SNAPSHOT("snapshot"),
    OLD_BETA("old_beta"),
    OLD_ALPHA("old_alpha");

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

    private final String jsonName;

    VersionType(String jsonName) {
        this.jsonName = jsonName;
    }

    public String getJsonName() {
        return this.jsonName;
    }
}
