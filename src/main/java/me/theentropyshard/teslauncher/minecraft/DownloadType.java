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

public enum DownloadType {
    CLIENT("client"),
    SERVER("server"),
    WINDOWS_SERVER("windows_server"),
    CLIENT_MAPPINGS("client_mappings"),
    SERVER_MAPPINGS("server_mappings");

    private static final Map<String, DownloadType> LOOKUP = new HashMap<>();

    private final String jsonName;

    DownloadType(String jsonName) {
        this.jsonName = jsonName;
        this.putInLookup();
    }

    private void putInLookup() {
        LOOKUP.put(this.jsonName, this);
    }

    public static DownloadType getByJsonName(String jsonName) {
        return LOOKUP.get(jsonName);
    }

    public String getJsonName() {
        return this.jsonName;
    }
}
