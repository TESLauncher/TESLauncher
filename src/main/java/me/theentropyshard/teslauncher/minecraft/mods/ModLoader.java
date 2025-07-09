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

package me.theentropyshard.teslauncher.minecraft.mods;

import java.util.HashMap;
import java.util.Map;

public enum ModLoader {
    FABRIC("fabric", "Fabric"),

    // This is not a mod loader, but just a value indicating that it is a vanilla instance
    NONE("none", "None");

    private static final Map<String, ModLoader> lookup = new HashMap<>();

    static {
        for (ModLoader loader : ModLoader.values()) {
            ModLoader.lookup.put(loader.getName(), loader);
        }
    }

    public static ModLoader getByName(String name) {
        ModLoader loader = ModLoader.lookup.get(name);

        if (loader == null) {
            throw new IllegalArgumentException("Unknown mod loader: " + name);
        }

        return loader;
    }

    private final String name;
    private final String readableName;

    ModLoader(String name, String readableName) {
        this.name = name;
        this.readableName = readableName;
    }

    public String getName() {
        return this.name;
    }

    public String getReadableName() {
        return this.readableName;
    }
}
