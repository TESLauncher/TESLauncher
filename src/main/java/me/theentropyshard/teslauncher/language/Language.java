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

package me.theentropyshard.teslauncher.language;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.theentropyshard.teslauncher.utils.json.Json;

import java.util.Map;

public class Language {
    private final String name;
    private final String displayName;
    private final LanguageSection rootSection;

    public Language(String json) {
        JsonObject rootObject = Json.parse(json, JsonObject.class);

        JsonObject metaInfo = rootObject.get("metaInfo").getAsJsonObject();
        this.name = metaInfo.get("name").getAsString();
        this.displayName = metaInfo.get("displayName").getAsString();

        this.rootSection = this.parseSection(rootObject);
    }

    private LanguageSection parseSection(JsonObject jsonObject) {
        LanguageSection section = new MapLanguageSection();

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            if (value.isJsonObject()) {
                section.addSection(key, this.parseSection(value.getAsJsonObject()));
            } else if (value.isJsonPrimitive()) {
                section.addString(key, value.getAsString());
            }
        }

        return section;
    }

    public String getString(String key) {
        return this.rootSection.getString(key);
    }

    public LanguageSection getSection(String key) {
        return this.rootSection.getSection(key);
    }

    @Override
    public String toString() {
        return this.displayName;
    }

    public String getName() {
        return this.name;
    }

    public String getDisplayName() {
        return this.displayName;
    }
}
