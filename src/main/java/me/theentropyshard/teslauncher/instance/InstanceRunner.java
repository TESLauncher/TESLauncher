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
import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.accounts.AccountsManager;
import me.theentropyshard.teslauncher.gson.DetailedVersionInfoDeserializerOld;
import me.theentropyshard.teslauncher.gui.playview.PlayView;
import me.theentropyshard.teslauncher.http.ProgressListener;
import me.theentropyshard.teslauncher.minecraft.MinecraftDownloader;
import me.theentropyshard.teslauncher.minecraft.models.AssetIndex;
import me.theentropyshard.teslauncher.minecraft.models.VersionAssetIndex;
import me.theentropyshard.teslauncher.minecraft.models.VersionInfo;
import me.theentropyshard.teslauncher.utils.EnumOS;
import me.theentropyshard.teslauncher.utils.PathUtils;
import org.apache.commons.text.StringSubstitutor;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstanceRunner extends Thread {
    private final Instance instance;

    public InstanceRunner(Instance instance) {
        this.instance = instance;
    }

    @Override
    public void run() {
        try {
            TESLauncher launcher = TESLauncher.getInstance();
            InstanceManager instanceManager = launcher.getInstanceManager();
            Path mcDirOfInstance = PathUtils.createDirectoryIfNotExists(instanceManager.getMinecraftDir(this.instance));
            Path tmpNativesDir = PathUtils.createDirectoryIfNotExists(
                    launcher.getInstancesDir().resolve(this.instance.getName()).resolve("natives-tmp")
                            .resolve(this.instance.getMinecraftVersion())
            );

            ProgressListener progressListener = (contentLength, bytesRead, done, fileName) -> {
                SwingUtilities.invokeLater(() -> {
                    PlayView playView = TESLauncher.getInstance().getPlayView();
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

            Path clientsDir = launcher.getVersionsDir();
            Path librariesDir = launcher.getLibrariesDir();
            Path assetsDir = launcher.getAssetsDir();
            if (!this.instance.wasEverPlayed()) {
                MinecraftDownloader downloader = new MinecraftDownloader(
                        clientsDir,
                        assetsDir,
                        librariesDir,
                        tmpNativesDir,
                        instanceManager.getMinecraftDir(this.instance).resolve("resources"),
                        progressListener
                );

                TESLauncher.getInstance().getPlayView().getProgressBar().setVisible(true);
                TESLauncher.getInstance().getPlayView().getProgressBar().setEnabled(true);
                downloader.downloadMinecraft(this.instance.getMinecraftVersion());
                TESLauncher.getInstance().getPlayView().getProgressBar().setVisible(false);
                TESLauncher.getInstance().getPlayView().getProgressBar().setEnabled(false);

                this.instance.setWasEverPlayed(true);
                instanceManager.save(this.instance);
            }

            Path clientJson = clientsDir.resolve(this.instance.getMinecraftVersion())
                    .resolve(this.instance.getMinecraftVersion() + ".json");
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(VersionInfo.class, new DetailedVersionInfoDeserializerOld())
                    .create();
            VersionInfo versionInfo = gson.fromJson(new InputStreamReader(
                    Files.newInputStream(clientJson),
                    StandardCharsets.UTF_8
            ), VersionInfo.class);

            System.out.println("Libraries:");
            List<String> classpath = new ArrayList<>();
            for (String libPath : versionInfo.librariesPaths) {
                System.out.println("\t" + libPath);
                classpath.add(librariesDir.resolve(libPath).toAbsolutePath().toString());
            }
            classpath.add(clientsDir.resolve(versionInfo.id).resolve(versionInfo.id + ".jar").toAbsolutePath().toString());

            Map<String, Object> argVars = new HashMap<>();

            boolean hasLogInfo = versionInfo.logConfigId != null && versionInfo.logConfigUrl != null &&
                    versionInfo.logArgument != null;

            /*if (hasLogInfo) {
                Path log4j2ConfigsDir = TESLauncher.getInstance().getLog4jConfigsDir();

                Path log4j2ConfigFile = log4j2ConfigsDir.resolve(versionInfo.logConfigId);
                if (!Files.exists(log4j2ConfigFile)) {
                    byte[] bytes = Http.get(versionInfo.logConfigUrl);
                    Files.write(log4j2ConfigFile, bytes);
                }

                argVars.put("path", log4j2ConfigFile.toAbsolutePath().toString());
            }*/

            // JVM
            argVars.put("natives_directory", tmpNativesDir.toAbsolutePath().toString());
            argVars.put("launcher_name", "TESLauncher");
            argVars.put("launcher_version", "1.0.0");
            argVars.put("classpath", String.join(File.pathSeparator, classpath));

            VersionAssetIndex vAssetIndex = versionInfo.assetIndex;
            AssetIndex assetIndex = gson.fromJson(Files.newBufferedReader(
                    assetsDir.resolve("indexes").resolve(vAssetIndex.id + ".json")
            ), AssetIndex.class);

            // Game
            if (versionInfo.newFormat) {
                argVars.put("clientid", "-");
                argVars.put("auth_xuid", "-");
                argVars.put("auth_player_name", AccountsManager.getCurrentUsername());
                argVars.put("version_name", versionInfo.id);
                argVars.put("game_directory", mcDirOfInstance.toAbsolutePath().toString());
                argVars.put("assets_root", launcher.getAssetsDir().toAbsolutePath().toString());
                argVars.put("assets_index_name", versionInfo.assets);
                argVars.put("auth_uuid", "-");
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

            StringSubstitutor substitutor = new StringSubstitutor(argVars);

            List<String> jvmArgs = new ArrayList<>();

            /*if (hasLogInfo) {
                jvmArgs.add(substitutor.replace(versionInfo.logArgument));
            }*/

            for (String arg : versionInfo.jvmArgs) {
                jvmArgs.add(substitutor.replace(arg));
            }

            List<String> gameArgs = new ArrayList<>();
            for (String arg : versionInfo.gameArgs) {
                gameArgs.add(substitutor.replace(arg));
            }

            String mainClass = versionInfo.mainClass;

            String javaPath = this.getJavaExecutable();

            List<String> command = new ArrayList<>();
            command.add(javaPath);
            command.addAll(jvmArgs);
            command.add(mainClass);
            command.addAll(gameArgs);

            System.out.println("Starting Minecraft with the command:\n" + command);

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(instanceManager.getMinecraftDir(this.instance).getParent().toAbsolutePath().toFile());
            processBuilder.redirectErrorStream(true);

            long start = System.currentTimeMillis();
            Process process = processBuilder.start();
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            int exitCode = process.waitFor();
            System.out.println("Minecraft process finished with exit code " + exitCode);
            long end = System.currentTimeMillis();

            System.out.println("You played for " + ((end - start) / 1000) + " seconds!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getJavaExecutable() {
        String binDir = System.getProperty("java.home") + File.separator + "bin";
        //String binDir = "E:\\Users\\Aleksey\\.jdks\\liberica-17.0.3\\bin";
        if (EnumOS.getOS() == EnumOS.WINDOWS) {
            return binDir + File.separator + "javaw.exe";
        } else {
            return binDir + File.separator + "java";
        }
    }
}
