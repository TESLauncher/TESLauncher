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

package me.theentropyshard.teslauncher.minecraft.mods.fabric;

import com.google.gson.JsonElement;

import java.util.Map;

public class FabricLauncherMeta {
    private Map<String, FabricLibrary[]> libraries;
    private JsonElement mainClass;

    public FabricLauncherMeta() {

    }

    @Override
    public String toString() {
        return "FabricLauncherMeta{" +
            "libraries=" + this.libraries +
            ", mainClass=" + this.mainClass +
            '}';
    }

    public Map<String, FabricLibrary[]> getLibraries() {
        return this.libraries;
    }

    public JsonElement getMainClass() {
        return this.mainClass;
    }
}
