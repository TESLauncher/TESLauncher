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

import me.theentropyshard.teslauncher.logging.Log;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapLanguageSection implements LanguageSection {
    private final Map<String, String> strings;
    private final Map<String, LanguageSection> sections;

    private final Map<String, LanguageSection> dummySections;

    public MapLanguageSection() {
        this(new LinkedHashMap<>(), new LinkedHashMap<>());
    }

    public MapLanguageSection(Map<String, String> strings, Map<String, LanguageSection> sections) {
        this.strings = strings;
        this.sections = sections;

        this.dummySections = new LinkedHashMap<>();
    }

    public void addString(String key, String value) {
        this.strings.put(key, value);
    }

    public void addSection(String key, LanguageSection section) {
        this.sections.put(key, section);
    }

    public String getString(String key) {
        int lastDotIndex = key.lastIndexOf(".");

        if (lastDotIndex == -1) {
            String s = this.strings.get(key);

            if (s == null) {
                Log.warn("Section does not contain string for key: " + key);

                return key;
            }

            return s;
        }

        return this.getSection(key.substring(0, lastDotIndex)).getString(key.substring(lastDotIndex + 1));
    }

    public LanguageSection getSection(String key) {
        String[] parts = key.split("\\.");

        LanguageSection section = this.sections.get(parts[0]);

        for (int i = 1; i < parts.length; i++) {
            section = section.getSection(parts[i]);
        }

        if (section == null) {
            Log.warn("Could not find section by key: " + key);

            return this.dummySections.computeIfAbsent(key, k -> new DummySection());
        }

        return section;
    }
}
