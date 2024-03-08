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

package me.theentropyshard.teslauncher.minecraft.rule;

import me.theentropyshard.teslauncher.utils.OperatingSystem;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OperatingSystemFilter {
    private String name;
    private String version;
    private String arch;

    public boolean applies() {
        if (this.name != null) {
            return this.nameMatches();
        }

        if (this.arch != null) {
            return this.archMatches();
        }

        if (this.version != null) {
            return this.versionMatches();
        }

        return true;
    }

    private boolean nameMatches() {
        if (this.name.equalsIgnoreCase("windows") && !OperatingSystem.isWindows()) {
            return false;
        }

        if (this.name.equalsIgnoreCase("linux") && !OperatingSystem.isLinux()) {
            return false;
        }

        if (this.name.equalsIgnoreCase("osx") && !OperatingSystem.isOSX()) {
            return false;
        }

        return true;
    }

    private boolean archMatches() {
        boolean is64Bit = OperatingSystem.is64Bit();

        if (OperatingSystem.isArm()) {
            return (!this.arch.equalsIgnoreCase("arm32") || !is64Bit) &&
                    (!this.arch.equalsIgnoreCase("arm64") || is64Bit);
        } else {
            return (!this.arch.equalsIgnoreCase("x86") || !is64Bit) &&
                    (!this.arch.equalsIgnoreCase("x64") || is64Bit);
        }
    }

    private boolean versionMatches() {
        Pattern pattern = Pattern.compile(this.version);
        Matcher matcher = pattern.matcher(OperatingSystem.getVersion());

        return matcher.find();
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
