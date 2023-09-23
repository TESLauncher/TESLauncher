/*
 *  Copyright 2023 TheEntropyShard (https://github.com/TheEntropyShard)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package me.theentropyshard.teslauncher;

import com.beust.jcommander.JCommander;
import me.theentropyshard.teslauncher.instance.InstanceManager;
import me.theentropyshard.teslauncher.instance.InstanceManagerImpl;
import me.theentropyshard.teslauncher.utils.PathUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TESLauncher {
    private final Args args;
    private final Logger logger;
    private final Path workDir;

    private final Path minecraftDir;
    private final Path assetsDir;
    private final Path librariesDir;
    private final Path instancesDir;
    private final Path versionsDir;
    private final Path log4jConfigsDir;

    private final InstanceManager instanceManager;

    private TESLauncher(Args args, Logger logger, Path workDir) {
        this.args = args;
        this.logger = logger;
        this.workDir = workDir;

        TESLauncher.setInstance(this);

        this.minecraftDir = this.workDir.resolve("minecraft");
        this.assetsDir = this.minecraftDir.resolve("assets");
        this.librariesDir = this.minecraftDir.resolve("libraries");
        this.instancesDir = this.minecraftDir.resolve("instances");
        this.versionsDir = this.minecraftDir.resolve("versions");
        this.log4jConfigsDir = this.minecraftDir.resolve("log4j");
        this.createDirectories();

        this.instanceManager = new InstanceManagerImpl(this.instancesDir);
    }

    public static void start(String[] rawArgs) {
        Args args = new Args();
        JCommander.newBuilder().addObject(args).build().parse(rawArgs);

        String workDirPath = args.getWorkDirPath();
        Path workDir = (workDirPath == null || workDirPath.isEmpty() ?
                Paths.get(System.getProperty("user.dir", ".")) :
                Paths.get(workDirPath)).normalize().toAbsolutePath();

        System.setProperty("teslauncher.workDir", workDir.toString());
        Logger logger = LogManager.getLogger(TESLauncher.class);

        new TESLauncher(args, logger, workDir);
    }

    private void createDirectories() {
        try {
            PathUtils.createDirectoryIfNotExists(this.workDir);
            PathUtils.createDirectoryIfNotExists(this.minecraftDir);
            PathUtils.createDirectoryIfNotExists(this.assetsDir);
            PathUtils.createDirectoryIfNotExists(this.librariesDir);
            PathUtils.createDirectoryIfNotExists(this.instancesDir);
            PathUtils.createDirectoryIfNotExists(this.versionsDir);
            PathUtils.createDirectoryIfNotExists(this.log4jConfigsDir);
        } catch (IOException e) {
            this.logger.error("Unable to create launcher directories", e);
        }
    }

    private static TESLauncher instance;

    public static TESLauncher getInstance() {
        return TESLauncher.instance;
    }

    private static void setInstance(TESLauncher instance) {
        TESLauncher.instance = instance;
    }

    public Args getArgs() {
        return this.args;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public Path getWorkDir() {
        return this.workDir;
    }

    public Path getMinecraftDir() {
        return this.minecraftDir;
    }

    public Path getAssetsDir() {
        return this.assetsDir;
    }

    public Path getLibrariesDir() {
        return this.librariesDir;
    }

    public Path getInstancesDir() {
        return this.instancesDir;
    }

    public Path getVersionsDir() {
        return this.versionsDir;
    }

    public Path getLog4jConfigsDir() {
        return this.log4jConfigsDir;
    }

    public InstanceManager getInstanceManager() {
        return this.instanceManager;
    }
}
