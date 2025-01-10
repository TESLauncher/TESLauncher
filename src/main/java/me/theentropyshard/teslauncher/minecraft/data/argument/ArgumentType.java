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

package me.theentropyshard.teslauncher.minecraft.data.argument;

import java.util.HashMap;
import java.util.Map;

public enum ArgumentType {
    JVM("jvm"),
    GAME("game");

    private static final Map<String, ArgumentType> lookup = new HashMap<>();

    static {
        for (ArgumentType type : ArgumentType.values()) {
            lookup.put(type.getJsonName(), type);
        }
    }

    public static ArgumentType getByName(String jsonName) {
        ArgumentType type = lookup.get(jsonName);

        if (type == null) {
            throw new IllegalArgumentException("jsonName: " + jsonName);
        }

        return type;
    }

    private final String jsonName;

    ArgumentType(String jsonName) {
        this.jsonName = jsonName;
    }

    public String getJsonName() {
        return this.jsonName;
    }
}
