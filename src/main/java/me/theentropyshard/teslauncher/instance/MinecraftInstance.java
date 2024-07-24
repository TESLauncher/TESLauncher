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

import me.theentropyshard.teslauncher.Settings;
import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.utils.FileUtils;
import me.theentropyshard.teslauncher.utils.OperatingSystem;
import me.theentropyshard.teslauncher.utils.json.Json;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MinecraftInstance extends JavaInstance {
    private static final String INSTANCE_FILE_NAME = "instance.json";
    private static final String MINECRAFT_DIR_NAME = OperatingSystem.isMacOS() ? "minecraft" : ".minecraft";
    private static final String JARMODS_DIR_NAME = "jarmods";

    private transient Path workDir;

    private String minecraftVersion;
    private int minecraftWindowWidth;
    private int minecraftWindowHeight;
    private String customWindowString;
    private final List<JarMod> jarMods;
    private transient volatile boolean running;

    public MinecraftInstance() {
        this(null, null, null);
    }

    public MinecraftInstance(String name, String group, String minecraftVersion) {
        this.setName(name);
        this.setGroup(group);
        this.minecraftVersion = minecraftVersion;

        this.jarMods = new ArrayList<>();
    }

    public void save() throws IOException {
        Settings settings = TESLauncher.getInstance().getSettings();
        String content = settings.writePrettyJson ? Json.writePretty(this) : Json.write(this);
        FileUtils.writeUtf8(this.getWorkDir().resolve(MinecraftInstance.INSTANCE_FILE_NAME), content);
    }

    public boolean isRunning() {
        return this.running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public Path getWorkDir() {
        return this.workDir;
    }

    public void setWorkDir(Path workDir) {
        this.workDir = workDir;
    }

    public Path getMinecraftDir() {
        return this.workDir.resolve(MinecraftInstance.MINECRAFT_DIR_NAME);
    }

    public Path getJarModsDir() {
        return this.workDir.resolve(MinecraftInstance.JARMODS_DIR_NAME);
    }

    public List<JarMod> getJarMods() {
        return this.jarMods;
    }

    public String getMinecraftVersion() {
        return this.minecraftVersion;
    }

    public void setMinecraftVersion(String minecraftVersion) {
        this.minecraftVersion = minecraftVersion;
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
}
