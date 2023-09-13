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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InstanceManager {
    private final Path workDir;
    private final List<Instance> instances;
    private final Gson gson;

    public InstanceManager(Path workDir) {
        this.workDir = workDir;
        this.instances = new ArrayList<>();
        this.gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    }

    public void loadInstances() {
        this.instances.clear();

        List<Path> paths = new ArrayList<>();
        try (Stream<Path> stream = Files.list(this.workDir)) {
            paths.addAll(stream.collect(Collectors.toList()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Path path : paths) {
            if (!Files.isDirectory(path)) {
                continue;
            }

            Path instanceFile = path.resolve("instance.json");
            if (!Files.exists(instanceFile)) {
                continue;
            }

            InputStream inputStream = null;
            try {
                inputStream = Files.newInputStream(instanceFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            Instance instance = this.gson.fromJson(reader, Instance.class);
            this.instances.add(instance);
        }
    }

    public void createInstance(String name, String groupName, String minecraftVersion) {
        Instance instance = new Instance(name, groupName, minecraftVersion);
        this.instances.add(instance);

        Path instanceDir = this.workDir.resolve(name);
        if (Files.exists(instanceDir)) {
            return;
        }
        try {
            PathUtils.createDirectories(instanceDir);
            String mcDirName = "minecraft";
            if (EnumOS.getOS() != EnumOS.MACOS) {
                mcDirName = "." + mcDirName;
            }
            Path minecraftDir = instanceDir.resolve(mcDirName);
            PathUtils.createDirectories(minecraftDir);
            Path instanceFile = instanceDir.resolve("instance.json");
            Files.write(instanceFile, this.gson.toJson(instance).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveInstance(Instance instance) {
        Path instanceDir = this.workDir.resolve(instance.getName());
        Path instanceFile = instanceDir.resolve("instance.json");
        try {
            Files.write(instanceFile, this.gson.toJson(instance).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Path getMcDirOfInstance(String name) {
        Instance instance = this.findInstanceByName(name);
        if (instance == null) {
            return null;
        }
        return this.workDir.resolve(instance.getName()).resolve(".minecraft");
    }

    public void deleteInstance(String name) {
        Instance instance = this.findInstanceByName(name);

        if (instance == null) {
            return;
        }

        this.instances.remove(instance);

        Path instanceDir = this.workDir.resolve(instance.getName());
        if (Files.exists(instanceDir)) {
            try {
                PathUtils.deleteDirectoryRecursively(instanceDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void runInstance(String name) {
        Instance instance = this.findInstanceByName(name);

        if (instance == null) {
            throw new InstanceNotFoundException(name);
        }

        new InstanceRunner(instance).start();
    }

    public Instance findInstanceByName(String name) {
        Instance instance = null;
        for (Instance inst : this.instances) {
            if (inst.getName().equals(name)) {
                instance = inst;
            }
        }

        return instance;
    }

    public List<Instance> getInstances() {
        return new ArrayList<>(this.instances);
    }
}
