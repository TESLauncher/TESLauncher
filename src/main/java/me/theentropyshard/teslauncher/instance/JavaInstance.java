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

package me.theentropyshard.teslauncher.instance;

import java.util.Set;

public abstract class JavaInstance extends Instance {
    private String javaPath;
    private int minimumMemoryMegabytes;
    private int maximumMemoryMegabytes;
    private Set<String> customJvmFlags;

    public JavaInstance() {

    }

    public String getJavaPath() {
        return this.javaPath;
    }

    public void setJavaPath(String javaPath) {
        this.javaPath = javaPath;
    }

    public int getMinimumMemoryMegabytes() {
        return this.minimumMemoryMegabytes;
    }

    public void setMinimumMemoryMegabytes(int minimumMemoryMegabytes) {
        this.minimumMemoryMegabytes = minimumMemoryMegabytes;
    }

    public int getMaximumMemoryMegabytes() {
        return this.maximumMemoryMegabytes;
    }

    public void setMaximumMemoryMegabytes(int maximumMemoryMegabytes) {
        this.maximumMemoryMegabytes = maximumMemoryMegabytes;
    }

    public Set<String> getCustomJvmFlags() {
        return this.customJvmFlags;
    }

    public void setCustomJvmFlags(Set<String> customJvmFlags) {
        this.customJvmFlags = customJvmFlags;
    }
}
