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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.accounts.AccountsManager;
import me.theentropyshard.teslauncher.gson.ActionTypeAdapter;
import me.theentropyshard.teslauncher.gson.DetailedVersionInfoDeserializer;
import me.theentropyshard.teslauncher.gson.InstantTypeAdapter;
import me.theentropyshard.teslauncher.gui.playview.PlayView;
import me.theentropyshard.teslauncher.http.ProgressListener;
import me.theentropyshard.teslauncher.java.JavaManager;
import me.theentropyshard.teslauncher.minecraft.*;
import me.theentropyshard.teslauncher.minecraft.models.AssetIndex;
import me.theentropyshard.teslauncher.minecraft.models.VersionAssetIndex;
import me.theentropyshard.teslauncher.minecraft.models.VersionInfo;
import me.theentropyshard.teslauncher.utils.EnumOS;
import me.theentropyshard.teslauncher.utils.PathUtils;
import me.theentropyshard.teslauncher.utils.TimeUtils;
import org.apache.commons.text.StringSubstitutor;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

public class InstanceRunner extends Thread {
    private final Instance instance;

    private final Gson gson;
    private ProgressListener progressListener;

    public InstanceRunner(Instance instance) {
        this.instance = instance;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
                .registerTypeAdapter(VersionInfo.class, new DetailedVersionInfoDeserializer(TESLauncher.getInstance()))
                .registerTypeAdapter(Rule.Action.class, new ActionTypeAdapter())
                .create();
    }

    @Override
    public void run() {
        try {
            TESLauncher launcher = TESLauncher.getInstance();
            InstanceManager instanceManager = launcher.getInstanceManager();

            this.progressListener = (contentLength, bytesRead, done, fileName) -> {
                SwingUtilities.invokeLater(() -> {
                    PlayView playView = TESLauncher.getInstance().getGui().getPlayView();
                    JProgressBar progressBar = playView.getProgressBar();
                    progressBar.setMaximum((int) contentLength);
                    progressBar.setMinimum(0);
                    progressBar.setValue((int) bytesRead);

                    if (done) {
                        progressBar.setString("Downloaded " + fileName);
                    } else {
                        progressBar.setString("Downloading " + fileName + ": " + (bytesRead / 1024) + " KB / " + (contentLength / 1024) + " KB");
                    }
                });
            };

            Path versionsDir = launcher.getVersionsDir();
            Path librariesDir = launcher.getLibrariesDir();
            Path assetsDir = launcher.getAssetsDir();
            Path nativesDir = versionsDir.resolve(this.instance.getMinecraftVersion()).resolve("natives");

            // TODO: check version different way, not like this
            MinecraftDownloader downloader = new MinecraftDownloader(
                    versionsDir,
                    assetsDir,
                    librariesDir,
                    nativesDir,
                    instanceManager.getMinecraftDir(this.instance).resolve("resources"),
                    this.progressListener
            );

            JProgressBar progressBar = TESLauncher.getInstance().getGui().getPlayView().getProgressBar();
            progressBar.setVisible(true);
            progressBar.setEnabled(true);
            downloader.downloadMinecraft(this.instance.getMinecraftVersion());
            progressBar.setVisible(false);
            progressBar.setEnabled(false);

            Path clientJson = versionsDir.resolve(this.instance.getMinecraftVersion())
                    .resolve(this.instance.getMinecraftVersion() + ".json");
            VersionInfo versionInfo = this.gson.fromJson(new InputStreamReader(
                    Files.newInputStream(clientJson),
                    StandardCharsets.UTF_8
            ), VersionInfo.class);

            List<String> command = this.buildRunCommand(versionInfo, this.getArguments(versionInfo, nativesDir, librariesDir, versionsDir));
            System.out.println("Starting Minecraft with the command:\n" + command);

            this.instance.setLastTimePlayed(Instant.now());

            long start = System.currentTimeMillis();

            int exitCode = this.runGameProcess(command);
            System.out.println("Minecraft process finished with exit code " + exitCode);

            long end = System.currentTimeMillis();

            long timePlayedSeconds = (end - start) / 1000;
            String timePlayed = TimeUtils.getHoursMinutesSeconds(timePlayedSeconds);
            System.out.println("You played for " + timePlayed + " seconds!");

            this.instance.setTotalPlayedForSeconds(this.instance.getTotalPlayedForSeconds() + timePlayedSeconds);
            this.instance.setLastPlayedForSeconds(timePlayedSeconds);

            instanceManager.save(this.instance);
        } catch (Exception e) {
            e.printStackTrace();
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

        classpath.add(clientsDir.resolve(versionInfo.id).resolve(versionInfo.id + ".jar").toAbsolutePath().toString());

        return classpath;
    }

    private List<String> getArguments(VersionInfo versionInfo, Path tmpNativesDir, Path librariesDir, Path clientsDir) throws IOException {
        TESLauncher launcher = TESLauncher.getInstance();
        InstanceManager instanceManager = launcher.getInstanceManager();
        Path mcDirOfInstance = PathUtils.createDirectoryIfNotExists(instanceManager.getMinecraftDir(this.instance));
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
        AssetIndex assetIndex = this.gson.fromJson(Files.newBufferedReader(
                assetsDir.resolve("indexes").resolve(vAssetIndex.id + ".json")
        ), AssetIndex.class);

        // Game
        if (versionInfo.newFormat) {
            argVars.put("clientid", "-");
            argVars.put("auth_xuid", "-");
            argVars.put("auth_player_name", AccountsManager.getCurrentUsername());
            argVars.put("version_name", versionInfo.id);
            argVars.put("game_directory", mcDirOfInstance.toAbsolutePath().toString());
            argVars.put("assets_root", assetsDir.toAbsolutePath().toString());
            argVars.put("assets_index_name", versionInfo.assets);
            argVars.put("auth_uuid", UUID.randomUUID().toString());
            argVars.put("auth_access_token", "-");
            argVars.put("user_type", "msa");
            argVars.put("version_type", versionInfo.type);
        } else {
            argVars.put("auth_uuid", "-");
            argVars.put("auth_access_token", "-");
            argVars.put("auth_session", "-");
            argVars.put("user_properties", "-");
            argVars.put("game_directory", mcDirOfInstance.toAbsolutePath().toString());
            argVars.put("version_type", versionInfo.type);
            argVars.put("user_type", "msa");
            argVars.put("assets_index_name", versionInfo.assets);
            argVars.put("version_name", versionInfo.id);
            argVars.put("auth_player_name", AccountsManager.getCurrentUsername());
            argVars.put("uuid", "-");
            argVars.put("accessToken", "-");
            if (assetIndex.mapToResources || vAssetIndex.id.equals("legacy")) {
                argVars.put("assets_root", instanceManager.getMinecraftDir(this.instance).resolve("resources"));
                argVars.put("game_assets", instanceManager.getMinecraftDir(this.instance).resolve("resources"));
            } else {
                argVars.put("assets_root", assetsDir.toString());
                argVars.put("game_assets", assetsDir.toString());
            }
        }

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

            // TODO this is a workaround for 1.16.5
            if (minorVersion == 16 && patch == 5) {
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
                    System.out.println("Value: " + value);
                    String replaced = substitutor.replace(value);
                    System.out.println("Replaced: " + replaced);
                    arguments.add(replaced);
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

        String javaExecutable = this.getJavaExecutable(versionInfo.javaVersion.component);
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
        processBuilder.directory(manager.getMinecraftDir(this.instance).getParent().toAbsolutePath().toFile());
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        InputStream inputStream = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        return process.waitFor();
    }

    private String getJavaExecutable(String componentName) {
        JavaManager javaManager = TESLauncher.getInstance().getJavaManager();
        // TODO: see in JavaManager
        if (!javaManager.runtimeExists(componentName)) {
            try {
                JProgressBar progressBar = TESLauncher.getInstance().getGui().getPlayView().getProgressBar();
                progressBar.setVisible(true);
                progressBar.setEnabled(true);
                javaManager.downloadRuntime(componentName, this.progressListener);
                progressBar.setVisible(false);
                progressBar.setEnabled(false);
            } catch (IOException e) {
                TESLauncher.getInstance().getLogger().error(e);
            }
        }
        return javaManager.getJavaExecutable(componentName);
    }
}
