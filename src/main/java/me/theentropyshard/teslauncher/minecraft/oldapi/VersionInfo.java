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

package me.theentropyshard.teslauncher.minecraft.oldapi;

import java.util.ArrayList;
import java.util.List;

public final class VersionInfo {
    public JavaVersion javaVersion;
    public boolean newFormat;
    public String id;
    public String mainClass;
    public String type;
    public String assets;
    public ClientDownloads downloads;
    public final List<Argument> jvmArgs = new ArrayList<>();
    public final List<Argument> gameArgs = new ArrayList<>();
    public final List<Library> libraries = new ArrayList<>();
    public String logArgument;
    public String logConfigUrl;
    public String logConfigId;
    public VersionAssetIndex assetIndex;
}
