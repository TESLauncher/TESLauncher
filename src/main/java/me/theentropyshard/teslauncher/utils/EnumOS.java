/*
 * Copyright 2023 TheEntropyShard (https://github.com/TheEntropyShard)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.theentropyshard.teslauncher.utils;

import com.sun.jna.Platform;

public enum EnumOS {
    WINDOWS,
    LINUX,
    MACOS,
    SOLARIS,
    UNKNOWN;

    public static EnumOS getOS() {
        String osName = System.getProperty("os.name").toLowerCase();
        if(osName.contains("win")) return EnumOS.WINDOWS;
        if(osName.contains("mac")) return EnumOS.MACOS;
        if(osName.contains("solaris") || osName.contains("sunos")) return EnumOS.SOLARIS;
        if(osName.contains("linux") || osName.contains("unix")) return EnumOS.LINUX;
        return EnumOS.UNKNOWN;
    }

    public static String getOsName() {
        EnumOS os = EnumOS.getOS();
        switch (os) {
            case WINDOWS:
                return "windows";
            case LINUX:
            case SOLARIS:
                return "linux";
            case MACOS:
                return "osx";
            case UNKNOWN:
            default:
                return "unknown";
        }
    }

    public static String getArch() {
        if (Platform.is64Bit()) {
            return "64";
        }

        return "32";
    }

    public static String getVersion() {
        return System.getProperty("os.version");
    }
}
