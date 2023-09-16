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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.theentropyshard.teslauncher.utils.EnumOS;
import me.theentropyshard.teslauncher.utils.PathUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InstanceManagerImpl implements InstanceManager {
    private final Path workDir;
    private final Gson gson;
    private final List<Instance> instances;
    private final Map<String, Instance> instancesByName;

    private final String mcDirName;

    public InstanceManagerImpl(Path workDir) {
        this.workDir = workDir;
        this.gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        this.instances = new ArrayList<>();
        this.instancesByName = new HashMap<>();

        String mcDirName = "minecraft";
        if (EnumOS.getOS() != EnumOS.MACOS) {
            mcDirName = "." + mcDirName;
        }
        this.mcDirName = mcDirName;
    }

    @Override
    public void load() throws IOException {
        List<Path> paths;
        try (Stream<Path> stream = Files.list(this.workDir)) {
            paths = stream.collect(Collectors.toList());
        }

        for (Path path : paths) {
            if (!Files.isDirectory(path)) {
                continue;
            }

            Path instanceFile = path.resolve("instance.json");
            if (!Files.exists(instanceFile)) {
                continue;
            }

            try (InputStream inputStream = Files.newInputStream(instanceFile)) {
                InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                Instance instance = this.gson.fromJson(reader, Instance.class);
                this.instances.add(instance);
                this.instancesByName.put(instance.getName(), instance);
            }
        }
    }

    @Override
    public void reload() throws IOException {
        this.instances.clear();
        this.instancesByName.clear();
        this.load();
    }

    @Override
    public void save(Instance instance) throws IOException {
        Path instanceDir = this.getInstanceDir(instance);
        if (!Files.exists(instanceDir)) {
            return;
        }

        Path instanceFile = instanceDir.resolve("instance.json");
        Files.write(instanceFile, this.gson.toJson(instance).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void createInstance(String name, String groupName, String minecraftVersion) throws IOException {
        Instance instance = new Instance(name, groupName, minecraftVersion);
        this.instances.add(instance);
        this.instancesByName.put(name, instance);

        Path instanceDir = this.getInstanceDir(instance);
        if (Files.exists(instanceDir)) {
            return;
        }

        PathUtils.createDirectories(instanceDir);
        Path minecraftDir = instanceDir.resolve(this.mcDirName);
        PathUtils.createDirectories(minecraftDir);
        Path instanceFile = instanceDir.resolve("instance.json");
        Files.write(instanceFile, this.gson.toJson(instance).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void removeInstance(String name) throws IOException {
        Instance instance = this.getInstanceByName(name);
        if (instance == null) {
            return;
        }

        Path instanceDir = this.getInstanceDir(instance);
        if (Files.exists(instanceDir)) {
            PathUtils.deleteDirectoryRecursively(instanceDir);
        }

        this.instances.remove(instance);
        this.instancesByName.remove(name);
    }

    @Override
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

    @Override
    public Path getInstanceDir(Instance instance) {
        return this.workDir.resolve(instance.getName());
    }

    @Override
    public Path getMinecraftDir(Instance instance) {
        return this.workDir.resolve(instance.getName()).resolve(this.mcDirName);
    }

    @Override
    public Instance getInstanceByName(String name) {
        return this.instancesByName.get(name);
    }

    @Override
    public List<Instance> getInstances() {
        return this.instances;
    }
}
