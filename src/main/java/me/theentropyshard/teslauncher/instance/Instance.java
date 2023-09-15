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
    private boolean wasEverPlayed;
    private final String minecraftVersion;

    public Instance(String minecraftVersion) {
        this.minecraftVersion = minecraftVersion;
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

    public boolean wasEverPlayed() {
        return this.wasEverPlayed;
    }

    public void setWasEverPlayed(boolean wasEverPlayed) {
        this.wasEverPlayed = wasEverPlayed;
    }
}