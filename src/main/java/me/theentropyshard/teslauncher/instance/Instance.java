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

package me.theentropyshard.teslauncher.instance;

import java.time.LocalDateTime;

public abstract class Instance {
    private String name;
    private String group;
    private String iconPath = "/assets/icons/grass_icon.png";
    private LocalDateTime lastTimePlayed = LocalDateTime.MIN;
    private long lastPlaytime;
    private long totalPlaytime;

    public Instance() {

    }

    public void updatePlaytime(long seconds) {
        this.lastPlaytime = seconds;
        this.totalPlaytime += seconds;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return this.group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getIconPath() {
        return this.iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public LocalDateTime getLastTimePlayed() {
        return this.lastTimePlayed;
    }

    public void setLastTimePlayed(LocalDateTime lastTimePlayed) {
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
