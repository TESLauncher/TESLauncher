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
import me.theentropyshard.teslauncher.gui.dialogs.MinecraftDownloadDialog;
import me.theentropyshard.teslauncher.java.JavaManager;
import me.theentropyshard.teslauncher.minecraft.*;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InstanceRunner extends Thread {
    private static final Logger LOG = LogManager.getLogger(InstanceRunner.class);

    private final Account account;
    private final Instance instance;

    private Path clientCopyTmp;

    public InstanceRunner(Account account, Instance instance) {
        this.account = account;
        this.instance = instance;
    }

    @Override
    public void run() {
        SwingUtilities.invokeLater(TESLauncher.getInstance().getGui()::disableBeforePlay);

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

            MinecraftDownloader downloader = new GuiMinecraftDownloader(
                    versionsDir,
                    assetsDir,
                    librariesDir,
                    nativesDir,
                    launcher.getInstanceManager().getMinecraftDir(this.instance).resolve("resources"),
                    new MinecraftDownloadDialog()
            );

            downloader.downloadMinecraft(this.instance.getMinecraftVersion());

            Path clientJson = versionsDir.resolve(this.instance.getMinecraftVersion())
                    .resolve(this.instance.getMinecraftVersion() + ".json");
            VersionInfo versionInfo = Json.parse(FileUtils.readUtf8(clientJson), VersionInfo.class);

            List<String> arguments = this.getArguments(versionInfo, nativesDir, librariesDir, versionsDir);
            List<String> command = this.buildRunCommand(versionInfo, arguments);
            //System.out.println("Starting Minecraft with the command:\n" + command);

            this.instance.setLastTimePlayed(Instant.now());

            long start = System.currentTimeMillis();

            int exitCode = this.runGameProcess(command);
            LOG.info("Minecraft process finished with exit code " + exitCode);

            if (this.clientCopyTmp != null && Files.exists(this.clientCopyTmp)) {
                Files.delete(this.clientCopyTmp);
            }

            long end = System.currentTimeMillis();

            long timePlayedSeconds = (end - start) / 1000;
            String timePlayed = TimeUtils.getHoursMinutesSeconds(timePlayedSeconds);
            if (!timePlayed.trim().isEmpty()) {
                LOG.info("You played for " + timePlayed + "!");
            }

            this.instance.setTotalPlayedForSeconds(this.instance.getTotalPlayedForSeconds() + timePlayedSeconds);
            this.instance.setLastPlayedForSeconds(timePlayedSeconds);
            this.instance.save();
        } catch (Exception e) {
            LOG.error("Exception occurred while trying to start Minecraft " + this.instance.getMinecraftVersion(), e);
        } finally {
            SwingUtilities.invokeLater(TESLauncher.getInstance().getGui()::enableAfterPlay);
        }
    }

    private void applyJarMods(VersionInfo versionInfo, List<String> classpath, Path clientsDir) {
        Path originalClientPath = clientsDir.resolve(versionInfo.id).resolve(versionInfo.id + ".jar").toAbsolutePath();

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
                            String path = modFileToAdd.toAbsolutePath().toString();
                            String base = unpackDir.toAbsolutePath().toString();
                            String relative = new File(base).toURI().relativize(new File(path).toURI()).getPath();

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

    private List<String> resolveClasspath(VersionInfo versionInfo, Path librariesDir, Path clientsDir) {
        List<String> classpath = new ArrayList<>();

        for (Library library : versionInfo.libraries) {
            DownloadArtifact artifact = library.downloads.artifact;
            if (artifact == null) {
                continue;
            }

            if (RuleMatcher.applyOnThisPlatform(library)) {
                classpath.add(librariesDir.resolve(artifact.path).toAbsolutePath().toString());
            }
        }

        this.applyJarMods(versionInfo, classpath, clientsDir);

        return classpath;
    }

    private List<String> getArguments(VersionInfo versionInfo, Path tmpNativesDir, Path librariesDir, Path clientsDir) throws IOException {
        TESLauncher launcher = TESLauncher.getInstance();
        Path mcDirOfInstance = launcher.getInstanceManager().getMinecraftDir(this.instance);
        FileUtils.createDirectoryIfNotExists(mcDirOfInstance);
        Path assetsDir = launcher.getAssetsDir();

        List<String> arguments = new ArrayList<>();

        List<String> classpath = this.resolveClasspath(versionInfo, librariesDir, clientsDir);

        Map<String, Object> argVars = new HashMap<>();

        // JVM
        argVars.put("natives_directory", tmpNativesDir.toAbsolutePath().toString());
        argVars.put("launcher_name", "TESLauncher");
        argVars.put("launcher_version", "1.0.0");
        argVars.put("classpath", String.join(File.pathSeparator, classpath));

        VersionAssetIndex vAssetIndex = versionInfo.assetIndex;
        AssetIndex assetIndex = Json.parse(
                FileUtils.readUtf8(assetsDir.resolve("indexes").resolve(vAssetIndex.id + ".json")),
                AssetIndex.class
        );

        // Game
        if (versionInfo.newFormat) {
            argVars.put("client", "-");
            argVars.put("auth_xuid", "-");
            argVars.put("auth_player_name", this.account.getUsername());
            argVars.put("version_name", versionInfo.id);
            argVars.put("game_directory", mcDirOfInstance.toAbsolutePath().toString());
            argVars.put("assets_root", assetsDir.toAbsolutePath().toString());
            argVars.put("assets_index_name", versionInfo.assets);
            argVars.put("auth_uuid", this.account.getUuid().toString());
            argVars.put("auth_access_token", this.account.getAccessToken());
            argVars.put("user_type", "msa");
            argVars.put("version_type", versionInfo.type);
        } else {
            argVars.put("auth_uuid", this.account.getUuid().toString());
            argVars.put("auth_access_token", this.account.getAccessToken());
            argVars.put("auth_session", "-");
            argVars.put("user_properties", "-");
            argVars.put("game_directory", mcDirOfInstance.toAbsolutePath().toString());
            argVars.put("version_type", versionInfo.type);
            argVars.put("user_type", "msa");
            argVars.put("assets_index_name", versionInfo.assets);
            argVars.put("version_name", versionInfo.id);
            argVars.put("auth_player_name", this.account.getUsername());
            argVars.put("uuid", this.account.getUuid().toString());
            argVars.put("accessToken", this.account.getAccessToken());
            if (assetIndex.mapToResources) {
                argVars.put("assets_root", mcDirOfInstance.resolve("resources"));
                argVars.put("game_assets", mcDirOfInstance.resolve("resources"));
            } else if ("legacy".equals(vAssetIndex.id)) {
                Path virtualAssets = assetsDir.resolve("virtual").resolve("legacy").toAbsolutePath();
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

        for (Argument argument : versionInfo.jvmArgs) {
            if (RuleMatcher.applyOnThisPlatform(argument)) {
                for (String value : argument.value) {
                    arguments.add(substitutor.replace(value));
                }
            }
        }

        try {
            String[] split = versionInfo.id.split("\\.");
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

        arguments.add(versionInfo.mainClass);

        for (Argument argument : versionInfo.gameArgs) {
            if (RuleMatcher.applyOnThisPlatform(argument)) {
                for (String value : argument.value) {
                    arguments.add(substitutor.replace(value));
                }
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

    private List<String> buildRunCommand(VersionInfo versionInfo, List<String> arguments) {
        List<String> command = new ArrayList<>();

        String javaExecutable;
        if (versionInfo.javaVersion != null) {
            javaExecutable = this.getJavaExecutable(versionInfo.javaVersion.component);
        } else {
            try {
                String[] split = versionInfo.id.split("\\.");
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
        Path runDir = manager.getMinecraftDir(this.instance).getParent().toAbsolutePath();
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
