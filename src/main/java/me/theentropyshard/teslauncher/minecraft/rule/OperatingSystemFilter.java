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

package me.theentropyshard.teslauncher.minecraft.rule;

import me.theentropyshard.teslauncher.utils.OperatingSystem;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OperatingSystemFilter {
    private String name;
    private String version;
    private String arch;

    public OperatingSystemFilter() {

    }

    public String getName() {
        return this.name;
    }

    public String getVersion() {
        return this.version;
    }

    public String getArch() {
        return this.arch;
    }

    @Override
    public String toString() {
        return "OperatingSystemFilter{" +
                "name='" + this.name + '\'' +
                ", version='" + this.version + '\'' +
                ", arch='" + this.arch + '\'' +
                '}';
    }
}
