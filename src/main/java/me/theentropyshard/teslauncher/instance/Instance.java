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

package me.theentropyshard.teslauncher.instance;

public class Instance {
    private String name;
    private String groupName;
    private String minecraftVersion;
    private boolean wasEverPlayed;
    private String javaPath;
    private int minecraftWindowWidth;
    private int minecraftWindowHeight;
    private String customWindowString;
    private int minimumMemoryInMegabytes;
    private int maximumMemoryInMegabytes;

    public Instance() {

    }

    public Instance(String name, String groupName, String minecraftVersion) {
        this.name = name;
        this.groupName = groupName;
        this.minecraftVersion = minecraftVersion;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
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

    public boolean wasEverPlayed() {
        return this.wasEverPlayed;
    }

    public void setWasEverPlayed(boolean wasEverPlayed) {
        this.wasEverPlayed = wasEverPlayed;
    }

    public boolean isWasEverPlayed() {
        return this.wasEverPlayed;
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
}
