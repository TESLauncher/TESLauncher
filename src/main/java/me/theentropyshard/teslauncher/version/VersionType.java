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

package me.theentropyshard.teslauncher.version;

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
            VersionType.lookup.put(type.getJsonName(), type);
        }
    }

    private final String jsonName;

    VersionType(String jsonName) {
        this.jsonName = jsonName;
    }

    public String getJsonName() {
        return this.jsonName;
    }

    public static VersionType byJsonName(String jsonName) {
        return VersionType.lookup.get(jsonName);
    }
}
