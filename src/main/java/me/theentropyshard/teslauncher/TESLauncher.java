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

package me.theentropyshard.teslauncher;

import me.theentropyshard.teslauncher.accounts.AccountsManager;
import me.theentropyshard.teslauncher.gui.AppWindow;
import me.theentropyshard.teslauncher.gui.Gui;
import me.theentropyshard.teslauncher.instance.InstanceManager;
import me.theentropyshard.teslauncher.java.JavaManager;
import me.theentropyshard.teslauncher.network.UserAgentInterceptor;
import me.theentropyshard.teslauncher.utils.FileUtils;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TESLauncher {
    private static final Logger LOG = LogManager.getLogger(TESLauncher.class);

    public static final String USER_AGENT = "TESLauncher/0.7.3";
    public static final String TITLE = "TESLauncher";
    public static final int WIDTH = 960;
    public static final int HEIGHT = 540;

    private final Args args;
    private final Path workDir;

    private final Path runtimesDir;
    private final Path assetsDir;
    private final Path librariesDir;
    private final Path instancesDir;
    private final Path versionsDir;
    private final Path log4jConfigsDir;

    private final Path settingsFile;
    private final Settings settings;

    private final OkHttpClient httpClient;

    private final AccountsManager accountsManager;
    private final InstanceManager instanceManager;
    private final JavaManager javaManager;

    private final ExecutorService taskPool;

    private final Gui gui;

    private volatile boolean shutdown;

    public static AppWindow window;

    public TESLauncher(Args args, Path workDir) {
        this.args = args;
        this.workDir = workDir;

        TESLauncher.setInstance(this);

        Path minecraftDir = this.workDir.resolve("minecraft");
        this.runtimesDir = minecraftDir.resolve("runtimes");
        this.assetsDir = minecraftDir.resolve("assets");
        this.librariesDir = minecraftDir.resolve("libraries");
        this.instancesDir = minecraftDir.resolve("instances");
        this.versionsDir = minecraftDir.resolve("versions");
        this.log4jConfigsDir = minecraftDir.resolve("log4j");
        this.createDirectories();

        this.settingsFile = this.workDir.resolve("settings.json");

        Settings settings = new Settings();
        try {
            settings = settings.load(this.settingsFile);
        } catch (IOException e) {
            LOG.error("Unable to load settings, using defaults", e);
        }
        this.settings = settings;

        this.httpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(new UserAgentInterceptor(TESLauncher.USER_AGENT))
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.MINUTES)
                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                .build();

        this.accountsManager = new AccountsManager(this.workDir);
        try {
            this.accountsManager.loadAccounts();
        } catch (IOException e) {
            LOG.error("Unable to load accounts", e);
        }

        this.instanceManager = new InstanceManager(this.instancesDir);
        try {
            this.instanceManager.load();
        } catch (IOException e) {
            LOG.error("Unable to load instances", e);
        }

        this.javaManager = new JavaManager(this.runtimesDir);

        this.taskPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        this.gui = new Gui(this.settings.darkTheme);
        this.gui.getAppWindow().addWindowClosingListener(this::shutdown);

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        this.gui.showGui();
    }

    private void createDirectories() {
        try {
            FileUtils.createDirectoryIfNotExists(this.workDir);
            FileUtils.createDirectoryIfNotExists(this.runtimesDir);
            FileUtils.createDirectoryIfNotExists(this.assetsDir);
            FileUtils.createDirectoryIfNotExists(this.librariesDir);
            FileUtils.createDirectoryIfNotExists(this.instancesDir);
            FileUtils.createDirectoryIfNotExists(this.versionsDir);
            FileUtils.createDirectoryIfNotExists(this.log4jConfigsDir);
        } catch (IOException e) {
            LOG.error("Unable to create launcher directories", e);
        }
    }

    public void doTask(Runnable r) {
        this.taskPool.submit(r);
    }

    public void shutdown() {
        if (this.shutdown) {
            return;
        }

        this.shutdown = true;

        this.taskPool.shutdown();

        try {
            this.accountsManager.save();
        } catch (IOException e) {
            LOG.error("Exception while saving accounts", e);
        }

        this.instanceManager.getInstances().forEach(i -> {
            try {
                this.instanceManager.save(i);
            } catch (IOException e) {
                LOG.error("Exception while saving instance '" + i + "'", e);
            }
        });

        try {
            this.settings.save(this.settingsFile);
        } catch (IOException e) {
            LOG.error("Exception while saving settings", e);
        }
    }

    private static TESLauncher instance;

    public static TESLauncher getInstance() {
        return TESLauncher.instance;
    }

    private static void setInstance(TESLauncher instance) {
        TESLauncher.instance = instance;
    }

    public OkHttpClient getHttpClient() {
        return this.httpClient;
    }

    public Settings getSettings() {
        return this.settings;
    }

    public Args getArgs() {
        return this.args;
    }

    public Path getWorkDir() {
        return this.workDir;
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

    public AccountsManager getAccountsManager() {
        return this.accountsManager;
    }

    public InstanceManager getInstanceManager() {
        return this.instanceManager;
    }

    public JavaManager getJavaManager() {
        return this.javaManager;
    }

    public Gui getGui() {
        return this.gui;
    }
}
