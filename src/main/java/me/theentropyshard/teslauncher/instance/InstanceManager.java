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

package me.theentropyshard.teslauncher.instance;

import me.theentropyshard.teslauncher.minecraft.MinecraftInstance;
import me.theentropyshard.teslauncher.utils.FileUtils;
import me.theentropyshard.teslauncher.utils.StringUtils;
import me.theentropyshard.teslauncher.utils.ZipUtils;
import me.theentropyshard.teslauncher.utils.json.Json;
import me.theentropyshard.teslauncher.logging.Log;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstanceManager {
    private final Path workDir;
    private final List<MinecraftInstance> instances;
    private final Map<String, MinecraftInstance> instancesByName;

    private long totalPlaytime;

    public InstanceManager(Path workDir) {
        this.workDir = workDir;
        this.instances = new ArrayList<>();
        this.instancesByName = new HashMap<>();
    }

    public void load() throws IOException {
        List<Path> paths = FileUtils.list(this.workDir);

        for (Path path : paths) {
            if (!Files.isDirectory(path)) {
                continue;
            }

            if (this.loadInstance(path) == null) {
                Log.warn("Found instance directory without instance.json: " + path);
            }
        }
    }

    public MinecraftInstance loadInstance(Path instanceDir) throws IOException {
        Path instanceFile = instanceDir.resolve("instance.json");

        if (!Files.exists(instanceFile)) {
            return null;
        }

        MinecraftInstance instance = Json.parse(FileUtils.readUtf8(instanceFile), MinecraftInstance.class);
        instance.setWorkDir(instanceDir);

        this.cacheInstance(instance);
        this.increaseTotalPlaytime(instance.getTotalPlaytime());

        return instance;
    }

    public void reload() throws IOException {
        this.uncacheAll();
        this.load();
    }

    private void cacheInstance(MinecraftInstance instance) {
        if (this.instancesByName.containsKey(instance.getName())) {
            return;
        }

        this.instances.add(instance);
        this.instancesByName.put(instance.getName(), instance);
    }

    private void uncacheInstance(MinecraftInstance instance) {
        if (!this.instancesByName.containsKey(instance.getName())) {
            return;
        }

        this.instances.remove(instance);
        this.instancesByName.remove(instance.getName());
    }

    private void uncacheAll() {
        this.instances.clear();
        this.instancesByName.clear();
    }

    private Path findFreeName(String suggestion) {
        Path path = this.workDir.resolve(suggestion);

        if (Files.exists(path)) {
            suggestion = suggestion + "_";
            path = this.workDir.resolve(suggestion);

            if (!Files.exists(path)) {
                return path;
            }

            return this.findFreeName(suggestion);
        }

        return path;
    }

    private Path getInstanceWorkDir(String suggestedName, String minecraftVersion) {
        String cleanName = FileUtils.sanitizeFileName(suggestedName);

        if (cleanName.isEmpty()) {
            cleanName = "instance" + minecraftVersion;
        }

        Path freeName;

        try {
            freeName = this.findFreeName(cleanName);
        } catch (StackOverflowError | Exception e) {
            Log.warn("Unable to find free name for instance");

            freeName = this.workDir.resolve(StringUtils.getRandomString(10));
        }

        return freeName;
    }

    public Instance createInstance(String name, String group, String minecraftVersion, boolean autoUpdate) throws
            IOException,
            InstanceAlreadyExistsException {

        if (this.instancesByName.containsKey(name)) {
            throw new InstanceAlreadyExistsException(name);
        }

        MinecraftInstance instance = new MinecraftInstance(name, group, minecraftVersion);
        instance.setWorkDir(this.getInstanceWorkDir(name, minecraftVersion));
        instance.setAutoUpdateToLatest(autoUpdate);

        this.cacheInstance(instance);

        FileUtils.createDirectoryIfNotExists(instance.getWorkDir());
        FileUtils.createDirectoryIfNotExists(instance.getMinecraftDir());
        FileUtils.createDirectoryIfNotExists(instance.getJarModsDir());

        instance.save();

        return instance;
    }

    public void removeInstance(String name) throws IOException {
        MinecraftInstance instance = this.getInstanceByName(name);

        if (instance == null) {
            return;
        }

        FileUtils.delete(instance.getWorkDir());

        this.uncacheInstance(instance);
        this.decreaseTotalPlaytime(instance.getTotalPlaytime());
    }

    public boolean renameInstance(MinecraftInstance instance, String newName) throws IOException {
        this.uncacheInstance(instance);

        Path newInstanceDir = this.getInstanceWorkDir(newName, instance.getMinecraftVersion());

        Files.move(instance.getWorkDir(), newInstanceDir, StandardCopyOption.REPLACE_EXISTING);

        instance.setWorkDir(newInstanceDir);

        boolean invalidName = !newInstanceDir.endsWith(newName);

        if (invalidName) {
            instance.setName(newInstanceDir.getFileName().toString());
        } else {
            instance.setName(newName);
        }

        this.cacheInstance(instance);

        return invalidName;
    }

    public void increaseTotalPlaytime(long seconds) {
        if (seconds < 0) {
            return;
        }

        this.totalPlaytime += seconds;
    }

    public void decreaseTotalPlaytime(long seconds) {
        if (seconds < 0) {
            return;
        }

        this.totalPlaytime = Math.max(0, Math.min(this.totalPlaytime, this.totalPlaytime - seconds));
    }

    public InstanceImportResult importInstance(Path file) throws IOException {
        try (ZipFile zipFile = new ZipFile(file.toFile())) {
            List<FileHeader> fileHeaders = zipFile.getFileHeaders();
            if (fileHeaders.isEmpty()) {
                return new InstanceImportResult(
                    InstanceImportStatus.BAD_FILE,
                    "empty zip"
                );
            }

            String fileName = ZipUtils.findTopLevelDirectory(fileHeaders);
            if (fileName == null) {
                return new InstanceImportResult(
                    InstanceImportStatus.BAD_FILE,
                    "cannot find top level directory in zip"
                );
            }

            if (Files.exists(this.workDir.resolve(fileName))) {
                return new InstanceImportResult(
                    InstanceImportStatus.INSTANCE_EXISTS,
                    fileName
                );
            }

            zipFile.extractAll(this.workDir.toString());

            Path instanceDir = this.workDir.resolve(fileName);
            MinecraftInstance instance = this.loadInstance(instanceDir);
            if (instance == null) {
                FileUtils.delete(instanceDir);

                return new InstanceImportResult(
                    InstanceImportStatus.BAD_FILE,
                    "no instance.json in zip"
                );
            }

            return new InstanceImportResult(InstanceImportStatus.SUCCESS, instance);
        }
    }

    public static final class InstanceImportResult {
        private final InstanceImportStatus status;
        private final Object message;

        public InstanceImportResult(InstanceImportStatus status, Object message) {
            this.status = status;
            this.message = message;
        }

        public InstanceImportStatus getStatus() {
            return this.status;
        }

        public Object getMessage() {
            return this.message;
        }
    }

    public enum InstanceImportStatus {
        SUCCESS,
        BAD_FILE,
        INSTANCE_EXISTS
    }

    public long getTotalPlaytime() {
        return this.totalPlaytime;
    }

    public int getInstancesCount() {
        return this.instances.size();
    }

    public MinecraftInstance getInstanceByName(String name) {
        return this.instancesByName.get(name);
    }

    public List<MinecraftInstance> getInstances() {
        return this.instances;
    }
}
