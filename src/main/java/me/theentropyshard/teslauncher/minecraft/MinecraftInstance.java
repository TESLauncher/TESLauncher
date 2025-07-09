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

package me.theentropyshard.teslauncher.minecraft;

import me.theentropyshard.teslauncher.instance.JarMod;
import me.theentropyshard.teslauncher.instance.JavaInstance;
import me.theentropyshard.teslauncher.minecraft.mods.ModLoader;
import me.theentropyshard.teslauncher.minecraft.mods.ModLoaderInfo;
import me.theentropyshard.teslauncher.utils.FileUtils;
import me.theentropyshard.teslauncher.utils.ListUtils;
import me.theentropyshard.teslauncher.utils.OperatingSystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MinecraftInstance extends JavaInstance {
    private static final String MINECRAFT_DIR_NAME = OperatingSystem.isMacOS() ? "minecraft" : ".minecraft";
    private static final String JARMODS_DIR_NAME = "jarmods";

    private static final String FABRIC_MODS_DIR = "fabric-mods";
    private static final String DISABLED_FABRIC_MODS_DIR = "disabled-fabric-mods";

    private String minecraftVersion;
    private int minecraftWindowWidth;
    private int minecraftWindowHeight;
    private String customWindowString;
    private boolean autoUpdateToLatest;

    private final List<JarMod> jarMods;

    private ModLoader modLoader = ModLoader.NONE;

    private String fabricVersion = "0.16.14";
    private final List<Mod> fabricMods;

    private transient volatile boolean running;

    public MinecraftInstance() {
        this(null, null, null);
    }

    public MinecraftInstance(String name, String group, String minecraftVersion) {
        this.setName(name);
        this.setGroup(group);
        this.minecraftVersion = minecraftVersion;

        this.setMinimumMemoryMegabytes(512);
        this.setMaximumMemoryMegabytes(2048);

        this.jarMods = new ArrayList<>();
        this.fabricMods = new ArrayList<>();
    }

    public ModLoaderInfo createLoaderInfo() {
        return switch (this.modLoader) {
            case FABRIC -> new ModLoaderInfo(this.modLoader, this.fabricVersion);

            default -> null;
        };
    }

    public ModLoader getModLoader() {
        return this.modLoader;
    }

    public void setModLoader(ModLoader modLoader) {
        this.modLoader = modLoader;
    }

    public String getFabricVersion() {
        return this.fabricVersion;
    }

    public void setFabricVersion(String fabricVersion) {
        this.fabricVersion = fabricVersion;
    }

    public List<Mod> getFabricMods() {
        return this.fabricMods;
    }

    public Path getCurrentModPath(Mod mod) {
        return this.getModPath(mod, this.modLoader);
    }

    public Path getModPath(Mod mod, ModLoader loader) {
        if (mod.isActive()) {
            return this.getModsDir(loader).resolve(mod.getFileName());
        } else {
            return this.getDisabledModsDir(loader).resolve(mod.getFileName());
        }
    }

    public List<Mod> getCurrentMods() {
        return this.getMods(this.modLoader);
    }

    public List<Mod> getMods(ModLoader loader) {
        return switch (this.modLoader) {
            case FABRIC -> this.fabricMods;
            case NONE -> throw new RuntimeException("Cannot get mods for vanilla instance");
        };
    }

    public Path getCurrentModsDir() {
        return this.getModsDir(this.modLoader);
    }

    public Path getModsDir(ModLoader loader) {
        return switch (loader) {
            case FABRIC -> this.getFabricModsDir();
            case NONE -> throw new RuntimeException("Cannot get mods dir for vanilla instance");
        };
    }

    public Path getCurrentDisabledModsDir() {
        return this.getDisabledModsDir(this.modLoader);
    }

    public Path getDisabledModsDir(ModLoader loader) {
        return switch (loader) {
            case FABRIC -> this.getDisabledFabricModsDir();
            case NONE -> throw new RuntimeException("Cannot get disabled mods dir for vanilla instance");
        };
    }

    public Path getFabricModsDir() {
        return this.getWorkDir().resolve(MinecraftInstance.FABRIC_MODS_DIR);
    }

    public Path getDisabledFabricModsDir() {
        return this.getWorkDir().resolve(MinecraftInstance.DISABLED_FABRIC_MODS_DIR);
    }

    public JarMod addJarMod(Path file) throws IOException {
        UUID uuid = UUID.randomUUID();

        Path fullPath = Files.copy(file, this.getJarModsDir().resolve(uuid + ".jar"), StandardCopyOption.REPLACE_EXISTING);

        JarMod jarMod = new JarMod(
            true, fullPath.toString(), uuid, file.getFileName().toString()
        );

        this.jarMods.add(jarMod);

        return jarMod;
    }

    public void removeJarMod(String id) throws IOException {
        JarMod jarMod = ListUtils.search(this.jarMods, mod -> mod.getUuid().toString().equals(id));

        if (jarMod == null) {
            return;
        }

        this.jarMods.remove(jarMod);

        FileUtils.delete(Paths.get(jarMod.getFullPath()));
    }

    public boolean isRunning() {
        return this.running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public Path getMinecraftDir() {
        return this.getWorkDir().resolve(MinecraftInstance.MINECRAFT_DIR_NAME);
    }

    public Path getJarModsDir() {
        return this.getWorkDir().resolve(MinecraftInstance.JARMODS_DIR_NAME);
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

    public boolean isAutoUpdateToLatest() {
        return this.autoUpdateToLatest;
    }

    public void setAutoUpdateToLatest(boolean autoUpdateToLatest) {
        this.autoUpdateToLatest = autoUpdateToLatest;
    }
}
