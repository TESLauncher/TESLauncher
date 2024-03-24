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

import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.accounts.Account;
import me.theentropyshard.teslauncher.accounts.MicrosoftAccount;
import me.theentropyshard.teslauncher.gui.components.InstanceItem;
import me.theentropyshard.teslauncher.gui.dialogs.MinecraftDownloadDialog;
import me.theentropyshard.teslauncher.java.JavaManager;
import me.theentropyshard.teslauncher.minecraft.*;
import me.theentropyshard.teslauncher.minecraft.argument.Argument;
import me.theentropyshard.teslauncher.minecraft.argument.ArgumentType;
import me.theentropyshard.teslauncher.minecraft.auth.microsoft.AuthException;
import me.theentropyshard.teslauncher.utils.FileUtils;
import me.theentropyshard.teslauncher.utils.Json;
import me.theentropyshard.teslauncher.utils.TimeUtils;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import org.apache.commons.text.StringSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstanceRunner extends Thread {
    private static final Logger LOG = LogManager.getLogger(InstanceRunner.class);

    private final Account account;
    private final Instance instance;
    private final InstanceItem item;

    private Path clientCopyTmp;

    public InstanceRunner(Account account, InstanceItem item) {
        this.account = account;
        this.instance = item.getAssociatedInstance();
        this.item = item;
    }

    @Override
    public void run() {
        boolean useDialog = TESLauncher.getInstance().getSettings().useDownloadDialog;

        if (useDialog) {
            SwingUtilities.invokeLater(TESLauncher.getInstance().getGui()::disableBeforePlay);
        }

        try {
            try {
                this.account.authenticate();
            } catch (AuthException e) {
                LOG.error(e);
                JOptionPane.showMessageDialog(TESLauncher.getInstance().getGui().getAppWindow().getFrame(),
                        e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }

            TESLauncher launcher = TESLauncher.getInstance();

            Path versionsDir = launcher.getVersionsDir();
            Path librariesDir = launcher.getLibrariesDir();
            Path assetsDir = launcher.getAssetsDir();
            Path nativesDir = versionsDir.resolve(this.instance.getMinecraftVersion()).resolve("natives");

            MinecraftDownloader downloader;
            if (useDialog) {
                downloader = new GuiMinecraftDownloader(
                        versionsDir,
                        assetsDir,
                        librariesDir,
                        nativesDir,
                        launcher.getInstanceManager().getMinecraftDir(this.instance).resolve("resources"),
                        new MinecraftDownloadDialog()
                );
            } else {
                downloader = new MinecraftDownloader(
                        versionsDir,
                        assetsDir,
                        librariesDir,
                        nativesDir,
                        launcher.getInstanceManager().getMinecraftDir(this.instance).resolve("resources"),
                        this.item
                );
            }

            downloader.downloadMinecraft(this.instance.getMinecraftVersion());

            Path clientJson = versionsDir.resolve(this.instance.getMinecraftVersion())
                    .resolve(this.instance.getMinecraftVersion() + ".json");
            Version version = Json.parse(FileUtils.readUtf8(clientJson), Version.class);

            List<String> arguments = this.getArguments(version, nativesDir, librariesDir, versionsDir);
            List<String> command = this.buildRunCommand(version, arguments);

            this.instance.setLastTimePlayed(Instant.now());

            long start = System.currentTimeMillis();

            int exitCode = this.runGameProcess(command);

            long end = System.currentTimeMillis();

            LOG.info("Minecraft process finished with exit code " + exitCode);

            long seconds = (end - start) / 1000;
            String timePlayed = TimeUtils.getHoursMinutesSeconds(seconds);
            if (!timePlayed.trim().isEmpty()) {
                LOG.info("You played for " + timePlayed + "!");
            }

            this.instance.updatePlaytime(seconds);
            this.instance.save();
        } catch (Exception e) {
            LOG.error("Exception occurred while trying to start Minecraft " + this.instance.getMinecraftVersion(), e);
        } finally {
            this.removeTempClient();

            if (useDialog) {
                SwingUtilities.invokeLater(TESLauncher.getInstance().getGui()::enableAfterPlay);
            }
        }
    }

    private void removeTempClient() {
        if (this.clientCopyTmp == null || !Files.exists(this.clientCopyTmp)) {
            return;
        }

        try {
            FileUtils.delete(this.clientCopyTmp);
        } catch (IOException e) {
            LOG.error("Unable to delete temporary copy of the client '{}'", this.clientCopyTmp, e);
        }
    }

    private void applyJarMods(Version version, List<String> classpath, Path clientsDir) {
        Path originalClientPath = clientsDir.resolve(version.getId()).resolve(version.getId() + ".jar").toAbsolutePath();

        List<JarMod> jarMods = this.instance.getJarMods();

        if (jarMods == null || jarMods.isEmpty() || jarMods.stream().noneMatch(JarMod::isActive)) {
            classpath.add(originalClientPath.toString());
        } else {
            try {
                InstanceManager instanceManager = TESLauncher.getInstance().getInstanceManager();
                Path instanceDir = instanceManager.getInstanceDir(this.instance);
                Path copyOfClient = Files.copy(originalClientPath, instanceDir
                        .resolve(originalClientPath.getFileName().toString() + System.currentTimeMillis() + ".jar"));
                this.clientCopyTmp = copyOfClient;

                List<File> zipFilesToMerge = new ArrayList<>();

                for (JarMod jarMod : jarMods) {
                    if (!jarMod.isActive()) {
                        continue;
                    }

                    zipFilesToMerge.add(Paths.get(jarMod.getFullPath()).toFile());
                }

                try (ZipFile copyZip = new ZipFile(copyOfClient.toFile())) {
                    copyZip.removeFile("META-INF/MANIFEST.MF");
                    copyZip.removeFile("META-INF/MOJANG_C.DSA");
                    copyZip.removeFile("META-INF/MOJANG_C.SF");

                    for (File modFile : zipFilesToMerge) {
                        Path unpackDir = instanceDir.resolve(modFile.getName().replace(".", "_"));
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

                classpath.add(copyOfClient.toString());
            } catch (IOException e) {
                LOG.error("Exception while applying jar mods", e);
            }
        }
    }

    private List<String> resolveClasspath(Version version, Path librariesDir, Path clientsDir) {
        List<String> classpath = new ArrayList<>();

        for (Library library : version.getLibraries()) {
            Library.Artifact artifact = library.getDownloads().getArtifact();
            if (artifact == null) {
                continue;
            }

            if (RuleMatcher.applyOnThisPlatform(library)) {
                classpath.add(librariesDir.resolve(artifact.getPath()).toAbsolutePath().toString());
            }
        }

        this.applyJarMods(version, classpath, clientsDir);

        return classpath;
    }

    private List<String> getArguments(Version version, Path tmpNativesDir, Path librariesDir, Path clientsDir) throws IOException {
        TESLauncher launcher = TESLauncher.getInstance();
        Path mcDirOfInstance = launcher.getInstanceManager().getMinecraftDir(this.instance);
        FileUtils.createDirectoryIfNotExists(mcDirOfInstance);
        Path assetsDir = launcher.getAssetsDir();

        List<String> arguments = new ArrayList<>();

        List<String> classpath = this.resolveClasspath(version, librariesDir, clientsDir);

        Map<String, Object> argVars = new HashMap<>();

        // JVM
        argVars.put("natives_directory", tmpNativesDir.toAbsolutePath().toString());
        argVars.put("launcher_name", TESLauncher.NAME);
        argVars.put("launcher_version", TESLauncher.VERSION);
        argVars.put("classpath", String.join(File.pathSeparator, classpath));

        Version.AssetIndex vAssetIndex = version.getAssetIndex();
        AssetIndex assetIndex = Json.parse(
                FileUtils.readUtf8(assetsDir.resolve("indexes").resolve(vAssetIndex.getId() + ".json")),
                AssetIndex.class
        );

        boolean newFormat = version.getMinecraftArguments() == null;

        // Game
        if (newFormat) {
            argVars.put("client", "-");
            argVars.put("auth_xuid", "-");
            argVars.put("auth_player_name", this.account.getUsername());
            argVars.put("version_name", version.getId());
            argVars.put("game_directory", mcDirOfInstance.toAbsolutePath().toString());
            argVars.put("assets_root", assetsDir.toAbsolutePath().toString());
            argVars.put("assets_index_name", version.getAssets());
            argVars.put("auth_uuid", this.account.getUuid().toString());
            argVars.put("auth_access_token", this.account.getAccessToken());
            argVars.put("user_type", "msa");
            argVars.put("version_type", version.getType().getJsonName());
        } else {
            argVars.put("auth_uuid", this.account.getUuid().toString());
            argVars.put("auth_access_token", this.account.getAccessToken());
            argVars.put("auth_session", "-");
            argVars.put("user_properties", "-");
            argVars.put("game_directory", mcDirOfInstance.toAbsolutePath().toString());
            argVars.put("version_type", version.getType().getJsonName());
            argVars.put("user_type", "msa");
            argVars.put("assets_index_name", version.getAssets());
            argVars.put("version_name", version.getId());
            argVars.put("auth_player_name", this.account.getUsername());
            argVars.put("uuid", this.account.getUuid().toString());
            argVars.put("accessToken", this.account.getAccessToken());
            if (assetIndex.isMapToResources()) {
                argVars.put("assets_root", mcDirOfInstance.resolve("resources"));
                argVars.put("game_assets", mcDirOfInstance.resolve("resources"));
            } else if (assetIndex.isVirtual()) {
                Path virtualAssets = assetsDir.resolve("virtual").resolve(vAssetIndex.getId()).toAbsolutePath();
                argVars.put("assets_root", virtualAssets.toString());
                argVars.put("game_assets", virtualAssets.toString());
            } else {
                argVars.put("assets_root", assetsDir.toString());
                argVars.put("game_assets", assetsDir.toString());
            }
        }

        argVars.put("user_properties", "{}");

        argVars.put("resolution_width", "960");
        argVars.put("resolution_height", "540");

        StringSubstitutor substitutor = new StringSubstitutor(argVars);

        int minimumMemoryInMegabytes = this.instance.getMinimumMemoryInMegabytes();
        int maximumMemoryInMegabytes = this.instance.getMaximumMemoryInMegabytes();

        if (minimumMemoryInMegabytes < 512) {
            minimumMemoryInMegabytes = 512;
        }

        if (maximumMemoryInMegabytes <= 0) {
            maximumMemoryInMegabytes = 2048;
        }

        if (minimumMemoryInMegabytes > maximumMemoryInMegabytes) {
            maximumMemoryInMegabytes = minimumMemoryInMegabytes;
        }

        this.instance.setMinimumMemoryInMegabytes(minimumMemoryInMegabytes);
        this.instance.setMaximumMemoryInMegabytes(maximumMemoryInMegabytes);

        arguments.add("-Xms" + minimumMemoryInMegabytes + "m");
        arguments.add("-Xmx" + maximumMemoryInMegabytes + "m");

        if (newFormat) {
            for (Argument argument : version.getArguments().get(ArgumentType.JVM)) {
                if (RuleMatcher.applyOnThisPlatform(argument)) {
                    for (String value : argument.getValue()) {
                        arguments.add(substitutor.replace(value));
                    }
                }
            }
        } else {
            // Old format does not have any JVM args preset, so we set it manually here
            arguments.add(substitutor.replace("-Djava.library.path=${natives_directory}"));

            arguments.add(substitutor.replace("-cp"));
            arguments.add(substitutor.replace("${classpath}"));
        }

        try {
            String[] split = version.getId().split("\\.");
            int minorVersion = Integer.parseInt(split[1]);
            int patch = Integer.parseInt(split[2]);

            if (minorVersion == 16 && patch == 5 && !(this.account instanceof MicrosoftAccount)) {
                arguments.add("-Dminecraft.api.auth.host=https://nope.invalid");
                arguments.add("-Dminecraft.api.account.host=https://nope.invalid");
                arguments.add("-Dminecraft.api.session.host=https://nope.invalid");
                arguments.add("-Dminecraft.api.services.host=https://nope.invalid");
            }
        } catch (Exception ignored) {

        }

        arguments.add(version.getMainClass());

        if (newFormat) {
            for (Argument argument : version.getArguments().get(ArgumentType.GAME)) {
                if (RuleMatcher.applyOnThisPlatform(argument)) {
                    for (String value : argument.getValue()) {
                        arguments.add(substitutor.replace(value));
                    }
                }
            }
        } else {
            for (String arg : version.getMinecraftArguments().split("\\s")) {
                arguments.add(substitutor.replace(arg));
            }
        }

        arguments.remove("--demo"); // :)

        arguments.remove("--quickPlayPath");
        arguments.remove("${quickPlayPath}");

        arguments.remove("--quickPlaySingleplayer");
        arguments.remove("${quickPlaySingleplayer}");

        arguments.remove("--quickPlayMultiplayer");
        arguments.remove("${quickPlayMultiplayer}");

        arguments.remove("--quickPlayRealms");
        arguments.remove("${quickPlayRealms}");

        return arguments;
    }

    private List<String> buildRunCommand(Version version, List<String> arguments) {
        List<String> command = new ArrayList<>();

        String javaExecutable;

        Version.JavaVersion javaVersion = version.getJavaVersion();
        if (javaVersion != null) {
            javaExecutable = this.getJavaExecutable(javaVersion.getComponent());
        } else {
            try {
                String[] split = version.getId().split("\\.");
                int minorVersion = Integer.parseInt(split[1]);

                if (minorVersion >= 17) {
                    javaExecutable = this.getJavaExecutable("java-runtime-gamma");
                } else {
                    javaExecutable = this.getJavaExecutable("jre-legacy");
                }
            } catch (Exception ignored) {
                javaExecutable = this.getJavaExecutable("jre-legacy");
            }
        }
        String javaPath = this.instance.getJavaPath();
        if (javaPath == null || javaPath.isEmpty()) {
            this.instance.setJavaPath(javaExecutable);
            javaPath = javaExecutable;
        }

        command.add(javaPath);
        command.addAll(arguments);

        return command;
    }

    private int runGameProcess(List<String> command) throws IOException, InterruptedException {
        InstanceManager manager = TESLauncher.getInstance().getInstanceManager();

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Path runDir = manager.getInstanceDir(this.instance).toAbsolutePath();
        processBuilder.environment().put("APPDATA", runDir.toString());
        processBuilder.directory(runDir.toFile());
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        InputStream inputStream = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            LOG.info(line);
        }

        return process.waitFor();
    }

    private String getJavaExecutable(String componentName) {
        JavaManager javaManager = TESLauncher.getInstance().getJavaManager();
        return javaManager.getJavaExecutable(componentName);
    }
}
