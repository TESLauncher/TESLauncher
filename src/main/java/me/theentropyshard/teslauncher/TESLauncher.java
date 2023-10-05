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

package me.theentropyshard.teslauncher;

import com.beust.jcommander.JCommander;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import me.theentropyshard.teslauncher.accounts.AccountsManager;
import me.theentropyshard.teslauncher.gui.AboutView;
import me.theentropyshard.teslauncher.gui.AccountsView;
import me.theentropyshard.teslauncher.gui.AppWindow;
import me.theentropyshard.teslauncher.gui.SettingsView;
import me.theentropyshard.teslauncher.gui.playview.PlayView;
import me.theentropyshard.teslauncher.instance.InstanceManager;
import me.theentropyshard.teslauncher.instance.InstanceManagerImpl;
import me.theentropyshard.teslauncher.utils.PathUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TESLauncher {
    public static final String TITLE = "TESLauncher";
    public static final int WIDTH = 960;
    public static final int HEIGHT = 540;

    private final Args args;
    private final Logger logger;
    private final Path workDir;

    private final Path runtimesDir;
    private final Path minecraftDir;
    private final Path assetsDir;
    private final Path librariesDir;
    private final Path instancesDir;
    private final Path versionsDir;
    private final Path log4jConfigsDir;

    private final AccountsManager accountsManager;
    private final InstanceManager instanceManager;

    private final ExecutorService taskPool;

    private boolean darkTheme;

    public static AppWindow window;
    private PlayView playView;

    private TESLauncher(Args args, Logger logger, Path workDir) {
        this.args = args;
        this.logger = logger;
        this.workDir = workDir;

        TESLauncher.setInstance(this);

        this.runtimesDir = this.workDir.resolve("runtimes");
        this.minecraftDir = this.workDir.resolve("minecraft");
        this.assetsDir = this.minecraftDir.resolve("assets");
        this.librariesDir = this.minecraftDir.resolve("libraries");
        this.instancesDir = this.minecraftDir.resolve("instances");
        this.versionsDir = this.minecraftDir.resolve("versions");
        this.log4jConfigsDir = this.minecraftDir.resolve("log4j");
        this.createDirectories();

        this.accountsManager = new AccountsManager(this.workDir);
        try {
            this.accountsManager.loadAccounts();
        } catch (IOException e) {
            this.logger.error("Unable to load accounts", e);
        }

        this.instanceManager = new InstanceManagerImpl(this.instancesDir);
        try {
            this.instanceManager.load();
        } catch (IOException e) {
            this.logger.error("Unable to load instances", e);
        }

        this.taskPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        this.darkTheme = false;

        this.showGui();
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
            PathUtils.createDirectoryIfNotExists(this.runtimesDir);
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

    private void showGui() {
        SwingUtilities.invokeLater(() -> {
            if (this.darkTheme) {
                UIManager.put("InstanceItem.defaultColor", new ColorUIResource(64, 75, 93));
                UIManager.put("InstanceItem.hoveredColor", new ColorUIResource(70, 80, 100));
                UIManager.put("InstanceItem.pressedColor", new ColorUIResource(60, 70, 86));

                UIManager.put("ProgressBar.selectionBackground", Color.WHITE);
                UIManager.put("ProgressBar.selectionForeground", Color.WHITE);

                FlatDarculaLaf.setup();
            } else {
                UIManager.put("InstanceItem.defaultColor", new ColorUIResource(222, 230, 237));
                UIManager.put("InstanceItem.hoveredColor", new ColorUIResource(224, 234, 244));
                UIManager.put("InstanceItem.pressedColor", new ColorUIResource(216, 224, 240));

                UIManager.put("ProgressBar.selectionBackground", Color.BLACK);
                UIManager.put("ProgressBar.selectionForeground", Color.BLACK);

                FlatIntelliJLaf.setup();
            }

            JDialog.setDefaultLookAndFeelDecorated(true);
            JFrame.setDefaultLookAndFeelDecorated(true);

            JTabbedPane viewSelector = new JTabbedPane(JTabbedPane.LEFT);
            AppWindow appWindow = new AppWindow(TESLauncher.TITLE, TESLauncher.WIDTH, TESLauncher.HEIGHT, viewSelector);
            TESLauncher.window = appWindow;

            this.playView = new PlayView();
            if (this.darkTheme) {
                this.playView.getProgressBar().setForeground(new Color(64, 75, 93));
            } else {
                this.playView.getProgressBar().setForeground(new Color(222, 230, 237));
            }

            viewSelector.addTab("Play", this.playView.getRoot());
            viewSelector.addTab("Accounts", new AccountsView().getRoot());
            viewSelector.addTab("Settings", new SettingsView().getRoot());
            viewSelector.addTab("About", new AboutView().getRoot());

            appWindow.setVisible(true);
        });
    }

    public void doTask(Runnable r) {
        this.taskPool.submit(r);
    }

    public void shutdown() {
        this.taskPool.shutdown();
    }

    private static TESLauncher instance;

    public static TESLauncher getInstance() {
        return TESLauncher.instance;
    }

    private static void setInstance(TESLauncher instance) {
        TESLauncher.instance = instance;
    }

    public PlayView getPlayView() {
        return this.playView;
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

    public AccountsManager getAccountsManager() {
        return this.accountsManager;
    }

    public InstanceManager getInstanceManager() {
        return this.instanceManager;
    }
}
