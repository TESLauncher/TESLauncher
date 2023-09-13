/*
 * Copyright 2023 TheEntropyShard (https://github.com/TheEntropyShard)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
