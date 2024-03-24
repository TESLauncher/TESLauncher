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

package me.theentropyshard.teslauncher.instance;

import me.theentropyshard.teslauncher.TESLauncher;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

public class Instance {
    private String name;
    private String dirName;
    private String groupName;
    private String minecraftVersion;
    private String javaPath;
    private int minecraftWindowWidth;
    private int minecraftWindowHeight;
    private String customWindowString;
    private int minimumMemoryInMegabytes = 512;
    private int maximumMemoryInMegabytes = 2048;
    private Instant lastTimePlayed = Instant.EPOCH;
    private long lastPlaytime;
    private long totalPlaytime;
    private List<JarMod> jarMods;

    public Instance() {

    }

    public Instance(String name, String groupName, String minecraftVersion) {
        this.name = name;
        this.groupName = groupName;
        this.minecraftVersion = minecraftVersion;
    }

    public void save() throws IOException {
        TESLauncher.getInstance().getInstanceManager().save(this);
    }

    public void updatePlaytime(long seconds) {
        this.lastPlaytime = seconds;
        this.totalPlaytime += seconds;
    }

    public List<JarMod> getJarMods() {
        return this.jarMods;
    }

    public void setJarMods(List<JarMod> jarMods) {
        this.jarMods = jarMods;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDirName() {
        return this.dirName;
    }

    public void setDirName(String dirName) {
        this.dirName = dirName;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getMinecraftVersion() {
        return this.minecraftVersion;
    }

    public void setMinecraftVersion(String minecraftVersion) {
        this.minecraftVersion = minecraftVersion;
    }

    public String getJavaPath() {
        return this.javaPath;
    }

    public void setJavaPath(String javaPath) {
        this.javaPath = javaPath;
    }

    public int getMinecraftWindowWidth() {
        return this.minecraftWindowWidth;
    }

    public void setMinecraftWindowWidth(int minecraftWindowWidth) {
        this.minecraftWindowWidth = minecraftWindowWidth;
    }

    public int getMinecraftWindowHeight() {
        return this.minecraftWindowHeight;
    }

    public void setMinecraftWindowHeight(int minecraftWindowHeight) {
        this.minecraftWindowHeight = minecraftWindowHeight;
    }

    public String getCustomWindowString() {
        return this.customWindowString;
    }

    public void setCustomWindowString(String customWindowString) {
        this.customWindowString = customWindowString;
    }

    public int getMinimumMemoryInMegabytes() {
        return this.minimumMemoryInMegabytes;
    }

    public void setMinimumMemoryInMegabytes(int minimumMemoryInMegabytes) {
        this.minimumMemoryInMegabytes = minimumMemoryInMegabytes;
    }

    public int getMaximumMemoryInMegabytes() {
        return this.maximumMemoryInMegabytes;
    }

    public void setMaximumMemoryInMegabytes(int maximumMemoryInMegabytes) {
        this.maximumMemoryInMegabytes = maximumMemoryInMegabytes;
    }

    public Instant getLastTimePlayed() {
        return this.lastTimePlayed;
    }

    public void setLastTimePlayed(Instant lastTimePlayed) {
        this.lastTimePlayed = lastTimePlayed;
    }

    public long getLastPlaytime() {
        return this.lastPlaytime;
    }

    public void setLastPlaytime(long lastPlaytime) {
        this.lastPlaytime = lastPlaytime;
    }

    public long getTotalPlaytime() {
        return this.totalPlaytime;
    }

    public void setTotalPlaytime(long totalPlaytime) {
        this.totalPlaytime = totalPlaytime;
    }
}
