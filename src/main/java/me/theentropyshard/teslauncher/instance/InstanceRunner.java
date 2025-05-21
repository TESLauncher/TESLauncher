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

import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.gui.components.InstanceItem;
import me.theentropyshard.teslauncher.gui.console.LauncherConsole;
import me.theentropyshard.teslauncher.gui.dialogs.ProgressDialog;
import me.theentropyshard.teslauncher.gui.utils.MessageBox;
import me.theentropyshard.teslauncher.logging.Log;
import me.theentropyshard.teslauncher.minecraft.MinecraftInstance;
import me.theentropyshard.teslauncher.minecraft.account.Account;
import me.theentropyshard.teslauncher.minecraft.account.OfflineAccount;
import me.theentropyshard.teslauncher.minecraft.account.microsoft.MicrosoftAccount;
import me.theentropyshard.teslauncher.minecraft.auth.microsoft.AuthException;
import me.theentropyshard.teslauncher.minecraft.data.Version;
import me.theentropyshard.teslauncher.minecraft.download.GuiMinecraftDownloader;
import me.theentropyshard.teslauncher.minecraft.download.MinecraftDownloader;
import me.theentropyshard.teslauncher.minecraft.launch.MinecraftError;
import me.theentropyshard.teslauncher.minecraft.launch.MinecraftLauncher;
import me.theentropyshard.teslauncher.utils.FileUtils;
import me.theentropyshard.teslauncher.utils.ProcessReader;
import me.theentropyshard.teslauncher.utils.TimeUtils;
import me.theentropyshard.teslauncher.utils.json.Json;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class InstanceRunner extends Thread {


    private final Account account;
    private final MinecraftInstance instance;
    private final InstanceItem item;

    private Path tempClientCopy;

    private Process process;

    public InstanceRunner(MinecraftInstance instance, Account account, InstanceItem item) {
        this.account = account;
        this.instance = instance;
        this.item = item;

        this.setName("Instance '" + this.instance.getName() + "'");
    }

    @Override
    public synchronized void start() {
        if (this.instance.isRunning()) {
            return;
        }

        this.instance.setRunning(true);
        this.item.setEnabled(false);

        super.start();
    }

    public void stopGame() {
        if (this.process == null || !this.process.isAlive()) {
            return;
        }

        this.process.destroy();

        Log.info("Destroyed Minecraft process for instance " + this.instance.getName());
    }

    @Override
    public void run() {
        boolean useDialog = TESLauncher.getInstance().getSettings().useDownloadDialog;

        int exitCode;
        String minecraftVersion = this.instance.getMinecraftVersion();
        try {
            if (!(this.account instanceof OfflineAccount)) {
                Log.info("Authenticating...");
            }

            try {
                this.account.authenticate();
            } catch (AuthException e) {
                this.instance.setRunning(false);
                this.item.setEnabled(true);
                Log.error("Could not authenticate", e);
                MessageBox.showErrorMessage(TESLauncher.frame, e.getMessage());
            }

            TESLauncher tesLauncher = TESLauncher.getInstance();

            Path versionsDir = tesLauncher.getVersionsDir();
            Path librariesDir = tesLauncher.getLibrariesDir();
            Path assetsDir = tesLauncher.getAssetsDir();
            Path runtimesDir = tesLauncher.getRuntimesDir();
            Path nativesDir = versionsDir.resolve(minecraftVersion).resolve("natives");

            Path minecraftDir = this.instance.getMinecraftDir();

            String javaPath = this.instance.getJavaPath();
            if (javaPath == null || javaPath.isEmpty()) {
                javaPath = null;
            }
            this.checkMinecraftInstallation(useDialog, versionsDir, assetsDir, librariesDir, nativesDir, runtimesDir, minecraftDir,
                minecraftVersion, javaPath == null);

            Path clientJson = versionsDir.resolve(minecraftVersion).resolve(minecraftVersion + ".json");
            Version version = Json.parse(FileUtils.readUtf8(clientJson), Version.class);

            MinecraftLauncher launcher = new MinecraftLauncher(librariesDir, runtimesDir, nativesDir, javaPath);

            this.instance.setLastTimePlayed(LocalDateTime.now());
            this.instance.save();

            int option = TESLauncher.getInstance().getSettings().whenMCLaunchesOption;

            boolean consoleWasOpen = LauncherConsole.instance.getFrame().isVisible();

            switch (option) {
                case 1:
                    TESLauncher.frame.setVisible(false);
                    break;
                case 2:
                    TESLauncher.frame.setVisible(false);
                    LauncherConsole.instance.setVisible(false);
                    break;
            }

            long start = System.currentTimeMillis();

            exitCode = this.startProcess(
                launcher,
                classpath -> {
                    this.applyJarMods(version, classpath, versionsDir);
                }, this.account, version, minecraftDir, minecraftDir,
                this.instance.getMinimumMemoryMegabytes(), this.instance.getMaximumMemoryMegabytes(),
                this.instance.getCustomJvmFlags(), option == 3
            );

            long end = System.currentTimeMillis();

            int exitsOption = TESLauncher.getInstance().getSettings().whenMCExitsOption;

            if (exitsOption == 0) {
                switch (option) {
                    case 1:
                        TESLauncher.frame.setVisible(true);
                        break;
                    case 2:
                        if (consoleWasOpen) {
                            LauncherConsole.instance.setVisible(true);
                        }
                        TESLauncher.frame.setVisible(true);
                        break;
                }
            }

            String exitMessage = "Minecraft process finished with exit code " + exitCode;
            if (exitCode == 0) {
                Log.info(exitMessage);
            } else {
                Log.error(exitMessage);
            }

            long seconds = (end - start) / 1000;
            String timePlayed = TimeUtils.getHoursMinutesSeconds(seconds);
            if (!timePlayed.trim().isEmpty()) {
                Log.info("You played for " + timePlayed + "!");
            }

            this.instance.updatePlaytime(seconds);
            this.instance.save();

            TESLauncher.getInstance().getInstanceManager().increaseTotalPlaytime(seconds);

            SwingUtilities.invokeLater(() -> {
                TESLauncher.getInstance().getGui().updateStats();
            });

            if (exitCode == 0 && exitsOption == 1) {
                TESLauncher.getInstance().shutdown();
            }
        } catch (Exception e) {
            Log.error("Exception occurred while trying to start Minecraft " + minecraftVersion, e);
        } finally {
            this.instance.setRunning(false);
            this.item.setEnabled(true);
            this.deleteTempClient();
        }
    }

    private int startProcess(
        MinecraftLauncher launcher,
        Consumer<List<String>> beforeLaunch, Account account, Version version,
        Path runDir, Path minecraftDir, long minMem, long maxMem, Set<String> jvmFlags, boolean exitAfterLaunch
    ) throws Exception {
        this.process = launcher.launch(beforeLaunch, account, version, runDir, minecraftDir, minMem, maxMem, jvmFlags, exitAfterLaunch);

        new ProcessReader(this.process).read(line -> {
            this.readProcessOutput(line, account);
        });

        try {
            return this.process.waitFor();
        } catch (InterruptedException e) {
            throw new IOException("Unable to wait for process to end");
        }
    }

    private void readProcessOutput(String line, Account account) {
        if (account instanceof MicrosoftAccount) {
            line = line.replace(account.getAccessToken(), "**ACCESSTOKEN**");
        }
        line = line.replace(account.getUsername(), "**USERNAME**");
        line = line.replace(account.getUuid().toString(), "**UUID**");

        MinecraftError.checkForError(line);

        Log.minecraft(line);
    }

    private void checkMinecraftInstallation(boolean useDialog, Path versionsDir, Path assetsDir, Path librariesDir,
                                            Path nativesDir, Path runtimesDir, Path minecraftDir, String minecraftVersion,
                                            boolean downloadJava) throws IOException {

        MinecraftDownloader downloader;
        if (useDialog) {
            downloader = new GuiMinecraftDownloader(
                versionsDir,
                assetsDir,
                librariesDir,
                nativesDir,
                runtimesDir,
                minecraftDir.resolve("resources"),
                new ProgressDialog("Downloading Minecraft " + minecraftVersion),
                downloadJava
            );
        } else {
            downloader = new MinecraftDownloader(
                versionsDir,
                assetsDir,
                librariesDir,
                nativesDir,
                runtimesDir,
                minecraftDir.resolve("resources"),
                this.item,
                downloadJava
            );
        }

        downloader.downloadMinecraft(minecraftVersion);
    }

    private void deleteTempClient() {
        if (this.tempClientCopy == null || !Files.exists(this.tempClientCopy)) {
            return;
        }

        try {
            FileUtils.delete(this.tempClientCopy);
        } catch (IOException e) {
            Log.error("Unable to delete temporary copy of the client '" + this.tempClientCopy + "'", e);
        }
    }

    private void applyJarMods(Version version, List<String> classpath, Path clientsDir) {
        Path originalClientPath = clientsDir.resolve(version.getId()).resolve(version.getId() + ".jar").toAbsolutePath();

        List<JarMod> jarMods = this.instance.getJarMods();

        if (jarMods == null || jarMods.isEmpty() || jarMods.stream().noneMatch(JarMod::isActive)) {
            classpath.add(originalClientPath.toString());
        } else {
            try {
                this.tempClientCopy = Files.copy(originalClientPath, this.instance.getWorkDir()
                    .resolve(originalClientPath.getFileName().toString() + System.currentTimeMillis() + ".jar"));

                List<File> zipFilesToMerge = new ArrayList<>();

                for (JarMod jarMod : jarMods) {
                    if (!jarMod.isActive()) {
                        continue;
                    }

                    zipFilesToMerge.add(Paths.get(jarMod.getFullPath()).toFile());
                }

                try (ZipFile copyZip = new ZipFile(this.tempClientCopy.toFile())) {
                    copyZip.removeFile("META-INF/MANIFEST.MF");
                    copyZip.removeFile("META-INF/MOJANG_C.DSA");
                    copyZip.removeFile("META-INF/MOJANG_C.SF");

                    for (File modFile : zipFilesToMerge) {
                        Path unpackDir = this.instance.getWorkDir().resolve(modFile.getName().replace(".", "_"));
                        try (ZipFile modZip = new ZipFile(modFile)) {
                            if (Files.exists(unpackDir)) {
                                FileUtils.delete(unpackDir);
                            }
                            FileUtils.createDirectoryIfNotExists(unpackDir);

                            modZip.extractAll(unpackDir.toAbsolutePath().toString());
                        }

                        List<Path> modFiles = FileUtils.walk(unpackDir);

                        ZipParameters zipParameters = new ZipParameters();

                        for (Path modFileToAdd : modFiles) {
                            String relative = unpackDir.toAbsolutePath().toUri().relativize(modFileToAdd.toAbsolutePath().toUri()).getPath();
                            zipParameters.setFileNameInZip(relative);
                            copyZip.addFile(modFileToAdd.toFile(), zipParameters);
                        }

                        FileUtils.delete(unpackDir);
                    }
                }

                classpath.add(this.tempClientCopy.toString());
            } catch (IOException e) {
                Log.error("Cannot apply jar mods", e);
            }
        }
    }
}
