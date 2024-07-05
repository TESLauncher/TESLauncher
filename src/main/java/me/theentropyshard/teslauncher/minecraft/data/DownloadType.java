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

import java.util.HashMap;
import java.util.Map;

public enum DownloadType {
    CLIENT("client"),
    CLIENT_MAPPINGS("client_mappings"),
    SERVER("server"),
    SERVER_MAPPINGS("server_mappings"),
    WINDOWS_SERVER("windows_server");

    private static final Map<String, DownloadType> lookup = new HashMap<>();

    static {
        for (DownloadType type : DownloadType.values()) {
            lookup.put(type.getJsonName(), type);
        }
    }

    public static DownloadType getByName(String jsonName) {
        DownloadType type = lookup.get(jsonName);

        if (type == null) {
            throw new IllegalArgumentException("jsonName: " + jsonName);
        }

        return type;
    }

    private final String jsonName;

    DownloadType(String jsonName) {
        this.jsonName = jsonName;
    }

    public String getJsonName() {
        return this.jsonName;
    }
}
