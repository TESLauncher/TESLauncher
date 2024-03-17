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

package me.theentropyshard.teslauncher.utils;

import com.sun.jna.Platform;

import java.awt.*;
import java.awt.datatransfer.StringSelection;

public enum OperatingSystem {
    WINDOWS,
    LINUX,
    MACOS,
    UNKNOWN;

    public String getJavaExecutableName() {
        if (this == OperatingSystem.WINDOWS) {
            return "javaw.exe";
        }

        return "java";
    }

    public static OperatingSystem getCurrent() {
        if (OperatingSystem.isWindows()) {
            return OperatingSystem.WINDOWS;
        } else if (OperatingSystem.isLinux()) {
            return OperatingSystem.LINUX;
        } else if (OperatingSystem.isMacOS()) {
            return OperatingSystem.MACOS;
        } else {
            return OperatingSystem.UNKNOWN;
        }
    }

    public static boolean isWindows() {
        return Platform.isWindows();
    }

    public static boolean isLinux() {
        return Platform.isLinux();
    }

    public static boolean isMacOS() {
        return Platform.isMac();
    }

    public static boolean isArm() {
        return Platform.isARM();
    }

    public static String getName() {
        return System.getProperty("os.name");
    }

    public static String getVersion() {
        return System.getProperty("os.version");
    }

    public static String getArch() {
        if (OperatingSystem.is64Bit()) {
            return "x64";
        }

        return "x86";
    }

    public static String getBits() {
        if (OperatingSystem.is64Bit()) {
            return "64";
        }

        return "32";
    }

    public static void copyToClipboard(String text) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
    }

    public static boolean is64Bit() {
        return Platform.is64Bit();
    }
}
