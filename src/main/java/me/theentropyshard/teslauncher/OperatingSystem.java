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

package me.theentropyshard.teslauncher;

public enum OperatingSystem {
    WINDOWS("windows", "windows", "win"),
    LINUX("linux", "linux", "unix"),
    OSX("oxs", "mac"),
    UNKNOWN("unknown");

    private final String name;
    private final String[] aliases;

    OperatingSystem(String name, String... aliases) {
        this.name = name;
        this.aliases = aliases;
    }

    public static OperatingSystem getCurrent() {
        String osName = System.getProperty("os.name");
        for (OperatingSystem os : OperatingSystem.values()) {
            for (String alias : os.getAliases()) {
                if (osName.contains(alias)) {
                    return os;
                }
            }
        }

        return OperatingSystem.UNKNOWN;
    }

    public String getName() {
        return this.name;
    }

    public String[] getAliases() {
        System.out.println("Called getAliases");
        return this.aliases;
    }
}
