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

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TESLauncher {
    public static final String TITLE = "TESLauncher";
    public static final int WIDTH = 960;
    public static final int HEIGHT = 540;

    private final Path workDir;
    private final Path minecraftDir;
    private final Path instancesDir;
    private final Path clientsDir;
    private final Path assetsDir;
    private final Path librariesDir;
    private final Path log4j2ConfigsDir;
    private final InstanceManager instanceManager;
    private final AccountsManager accountsManager;
    private final ExecutorService taskPool;

    private boolean darkTheme;

    public static TESLauncher instance;
    public static AppWindow window;

    public TESLauncher(String[] args) {
        TESLauncher.instance = this;

        this.workDir = Paths.get(System.getProperty("user.dir")).resolve("dev");
        this.minecraftDir = this.workDir.resolve("minecraft");
        this.instancesDir = this.minecraftDir.resolve("instances");
        this.clientsDir = this.minecraftDir.resolve("clients");
        this.assetsDir = this.minecraftDir.resolve("assets");
        this.librariesDir = this.minecraftDir.resolve("libraries");
        this.log4j2ConfigsDir = this.minecraftDir.resolve("log4j2");

        try {
            this.prepareDirs();
        } catch (IOException e) {
            throw new RuntimeException("Unable to create launcher directories", e);
        }

        this.instanceManager = new InstanceManagerImpl(this.instancesDir);
        try {
            this.instanceManager.load();
        } catch (IOException e) {
            throw new RuntimeException("Unable to load instances", e);
        }

        this.accountsManager = new AccountsManager(this.workDir);
        this.accountsManager.loadAccounts();

        this.taskPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        this.darkTheme = false;

        SwingUtilities.invokeLater(() -> {
            if (this.darkTheme) {
                UIManager.put("InstanceItem.defaultColor", new ColorUIResource(64, 75, 93));
                UIManager.put("InstanceItem.hoveredColor", new ColorUIResource(70, 80, 100));
                UIManager.put("InstanceItem.pressedColor", new ColorUIResource(60, 70, 86));

                FlatDarculaLaf.setup();
            } else {
                UIManager.put("InstanceItem.defaultColor", new ColorUIResource(222, 230, 237));
                UIManager.put("InstanceItem.hoveredColor", new ColorUIResource(224, 234, 244));
                UIManager.put("InstanceItem.pressedColor", new ColorUIResource(216, 224, 240));

                FlatIntelliJLaf.setup();
            }

            JDialog.setDefaultLookAndFeelDecorated(true);
            JFrame.setDefaultLookAndFeelDecorated(true);

            JTabbedPane viewSelector = new JTabbedPane(JTabbedPane.LEFT);
            AppWindow appWindow = new AppWindow(TESLauncher.TITLE, TESLauncher.WIDTH, TESLauncher.HEIGHT, viewSelector);
            TESLauncher.window = appWindow;

            viewSelector.addTab("Play", new PlayView().getRoot());
            viewSelector.addTab("Accounts", new AccountsView().getRoot());
            viewSelector.addTab("Settings", new SettingsView().getRoot());
            viewSelector.addTab("About", new AboutView().getRoot());

            appWindow.setVisible(true);
        });
    }

    public void prepareDirs() throws IOException {
        PathUtils.createDirectories(this.workDir);
        PathUtils.createDirectories(this.minecraftDir);
        PathUtils.createDirectories(this.clientsDir);
        PathUtils.createDirectories(this.assetsDir);
        PathUtils.createDirectories(this.instancesDir);
        PathUtils.createDirectories(this.librariesDir);
        PathUtils.createDirectories(this.log4j2ConfigsDir);
    }

    public void doTask(Runnable r) {
        this.taskPool.submit(r);
    }

    public InstanceManager getInstanceManager() {
        return this.instanceManager;
    }

    public AccountsManager getAccountsManager() {
        return this.accountsManager;
    }

    public Path getWorkDir() {
        return this.workDir;
    }

    public Path getMinecraftDir() {
        return this.minecraftDir;
    }

    public Path getInstancesDir() {
        return this.instancesDir;
    }

    public Path getClientsDir() {
        return this.clientsDir;
    }

    public Path getAssetsDir() {
        return this.assetsDir;
    }

    public Path getLibrariesDir() {
        return this.librariesDir;
    }

    public Path getLog4j2ConfigsDir() {
        return this.log4j2ConfigsDir;
    }
}
