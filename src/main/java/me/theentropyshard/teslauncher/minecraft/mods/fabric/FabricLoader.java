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

public class FabricLoader {
    private String separator;
    private int build;
    private String maven;
    private String version;
    private boolean stable;

    public FabricLoader() {

    }

    @Override
    public String toString() {
        return "FabricLoader{" +
            "separator='" + this.separator + '\'' +
            ", build=" + this.build +
            ", maven='" + this.maven + '\'' +
            ", version='" + this.version + '\'' +
            ", stable=" + this.stable +
            '}';
    }

    public String getSeparator() {
        return this.separator;
    }

    public int getBuild() {
        return this.build;
    }

    public String getMaven() {
        return this.maven;
    }

    public String getVersion() {
        return this.version;
    }

    public boolean isStable() {
        return this.stable;
    }
}
