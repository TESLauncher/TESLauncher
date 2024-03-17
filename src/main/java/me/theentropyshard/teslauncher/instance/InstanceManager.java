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

import me.theentropyshard.teslauncher.utils.FileUtils;
import me.theentropyshard.teslauncher.utils.Json;
import me.theentropyshard.teslauncher.utils.OperatingSystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstanceManager {
    private final Path workDir;
    private final List<Instance> instances;
    private final Map<String, Instance> instancesByName;

    private final String mcDirName;

    public InstanceManager(Path workDir) {
        this.workDir = workDir;
        this.instances = new ArrayList<>();
        this.instancesByName = new HashMap<>();

        String mcDirName = "minecraft";
        if (!OperatingSystem.isMacOS()) {
            mcDirName = "." + mcDirName;
        }
        this.mcDirName = mcDirName;
    }

    public void load() throws IOException {
        List<Path> paths = FileUtils.list(this.workDir);

        for (Path path : paths) {
            if (!Files.isDirectory(path)) {
                continue;
            }

            Path instanceFile = path.resolve("instance.json");
            if (!Files.exists(instanceFile)) {
                continue;
            }

            Instance instance = Json.parse(FileUtils.readUtf8(instanceFile), Instance.class);
            if (instance.getDirName() == null) {
                instance.setDirName(instance.getName());
            }

            this.instances.add(instance);
            this.instancesByName.put(instance.getName(), instance);
        }
    }

    private void createDirName(Instance instance) {
        if (Files.exists(this.getInstanceDir(instance))) {
            instance.setDirName(instance.getDirName() + "_");
            if (!Files.exists(this.getInstanceDir(instance))) {
                return;
            }

            this.createDirName(instance);
        }
    }

    public void reload() throws IOException {
        this.instances.clear();
        this.instancesByName.clear();
        this.load();
    }

    public void save(Instance instance) throws IOException {
        Path instanceDir = this.getInstanceDir(instance);
        FileUtils.createDirectoryIfNotExists(instanceDir);

        Path instanceFile = instanceDir.resolve("instance.json");
        FileUtils.writeUtf8(instanceFile, Json.write(instance));
    }

    public void createInstance(String name, String groupName, String minecraftVersion) throws IOException {
        Instance instance = new Instance(name, groupName, minecraftVersion);

        instance.setDirName(instance.getName());
        this.createDirName(instance);

        Path instanceDir = this.getInstanceDir(instance);
        if (Files.exists(instanceDir)) {
            throw new IOException("Instance dir '" + instanceDir + "' already exists");
        }

        this.instances.add(instance);
        this.instancesByName.put(name, instance);

        FileUtils.createDirectoryIfNotExists(instanceDir);
        Path minecraftDir = instanceDir.resolve(this.mcDirName);
        FileUtils.createDirectoryIfNotExists(minecraftDir);
        Path jarModsDir = this.getInstanceJarModsDir(instance);
        FileUtils.createDirectoryIfNotExists(jarModsDir);
        Path instanceFile = instanceDir.resolve("instance.json");
        FileUtils.writeUtf8(instanceFile, Json.write(instance));
    }

    public void removeInstance(String name) throws IOException {
        Instance instance = this.getInstanceByName(name);
        if (instance == null) {
            return;
        }

        Path instanceDir = this.getInstanceDir(instance);
        if (Files.exists(instanceDir)) {
            FileUtils.delete(instanceDir);
        }

        this.instances.remove(instance);
        this.instancesByName.remove(name);
    }

    public boolean instanceExists(String name) {
        Instance instance = this.getInstanceByName(name);
        if (instance == null) {
            return false;
        }

        Path instanceDir = this.getInstanceDir(instance);
        if (Files.exists(instanceDir)) {
            return true;
        } else {
            this.instances.remove(instance);
            this.instancesByName.remove(name);
            return false;
        }
    }

    public Path getInstanceDir(Instance instance) {
        return this.workDir.resolve(instance.getDirName());
    }

    public Path getMinecraftDir(Instance instance) {
        return this.workDir.resolve(instance.getDirName()).resolve(this.mcDirName);
    }

    public Path getInstanceJarModsDir(Instance instance) {
        return this.workDir.resolve(instance.getDirName()).resolve("jarmods");
    }

    public Instance getInstanceByName(String name) {
        return this.instancesByName.get(name);
    }

    public List<Instance> getInstances() {
        return this.instances;
    }
}
