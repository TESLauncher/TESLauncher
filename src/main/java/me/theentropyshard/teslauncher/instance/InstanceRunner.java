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
import me.theentropyshard.teslauncher.gson.ActionTypeAdapter;
import me.theentropyshard.teslauncher.gson.DetailedVersionInfoDeserializerOld;
import me.theentropyshard.teslauncher.gui.playview.PlayView;
import me.theentropyshard.teslauncher.http.ProgressListener;
import me.theentropyshard.teslauncher.minecraft.*;
import me.theentropyshard.teslauncher.minecraft.models.AssetIndex;
import me.theentropyshard.teslauncher.minecraft.models.VersionAssetIndex;
import me.theentropyshard.teslauncher.minecraft.models.VersionInfo;
import me.theentropyshard.teslauncher.utils.EnumOS;
import me.theentropyshard.teslauncher.utils.PathUtils;
import org.apache.commons.text.StringSubstitutor;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

public class InstanceRunner extends Thread {
    private final Instance instance;

    private Gson gson;

    public InstanceRunner(Instance instance) {
        this.instance = instance;
    }

    @Override
    public void run() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(VersionInfo.class, new DetailedVersionInfoDeserializerOld())
                .registerTypeAdapter(AutoCloseable.class, new ActionTypeAdapter())
                .create();

        try {
            TESLauncher launcher = TESLauncher.getInstance();
            InstanceManager instanceManager = launcher.getInstanceManager();
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
            //if (!this.instance.wasEverPlayed()) {
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

                //this.instance.setWasEverPlayed(true);
                //instanceManager.save(this.instance);
            //}

            Path clientJson = clientsDir.resolve(this.instance.getMinecraftVersion())
                    .resolve(this.instance.getMinecraftVersion() + ".json");
            VersionInfo versionInfo = this.gson.fromJson(new InputStreamReader(
                    Files.newInputStream(clientJson),
                    StandardCharsets.UTF_8
            ), VersionInfo.class);

            List<String> command = this.buildRunCommand(this.getArguments(versionInfo, tmpNativesDir, librariesDir, clientsDir));
            System.out.println("Starting Minecraft with the command:\n" + command);

            long start = System.currentTimeMillis();

            int exitCode = this.runGameProcess(command);
            System.out.println("Minecraft process finished with exit code " + exitCode);

            long end = System.currentTimeMillis();

            System.out.println("You played for " + ((end - start) / 1000) + " seconds!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<String> resolveClasspath(VersionInfo versionInfo, Path librariesDir, Path clientsDir) {
        List<String> classpath = new ArrayList<>();

        /*for (String libPath : versionInfo.librariesPaths) {
            classpath.add(librariesDir.resolve(libPath).toAbsolutePath().toString());
        }*/

        for (Library library : versionInfo.libraries) {
            DownloadArtifact artifact = library.downloads.artifact;
            if (artifact == null) {
                continue;
            }

            Rule.Action lastAction = Rule.Action.DISALLOW;
            if (library.rules == null || library.rules.isEmpty()) {
                lastAction = Rule.Action.ALLOW;
            } else {
                for (Rule rule : library.rules) {
                    Os os = rule.os;
                    if (os == null) {
                        lastAction = rule.action;
                    } else {
                        boolean versionMatches = os.version != null &&
                                Pattern.compile(os.version).matcher(EnumOS.getVersion()).matches();
                        if (EnumOS.getOsName().equals(os.name) ||
                                versionMatches || EnumOS.getArch().equals("x" + os.arch)) {
                            lastAction = rule.action;
                        }
                    }
                }
            }

            if (lastAction == Rule.Action.ALLOW) {
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

        argVars.put("resolution_width", "960");
        argVars.put("resolution_height", "540");

        StringSubstitutor substitutor = new StringSubstitutor(argVars);

        /*for (String arg : versionInfo.jvmArgs) {
            arguments.add(substitutor.replace(arg));
        }*/

        for (Argument argument : versionInfo.jvmArgs) {
            Rule.Action lastAction = Rule.Action.DISALLOW;
            if (argument.rules == null || argument.rules.isEmpty()) {
                lastAction = Rule.Action.ALLOW;
            } else {
                for (Rule rule : argument.rules) {
                    Os os = rule.os;
                    if (os == null) {
                        lastAction = rule.action;
                    } else {
                        boolean versionMatches = os.version != null &&
                                Pattern.compile(os.version).matcher(EnumOS.getVersion()).matches();
                        if (EnumOS.getOsName().equals(os.name) ||
                                versionMatches || EnumOS.getArch().equals("x" + os.arch)) {
                            lastAction = rule.action;
                        }
                    }
                }
            }

            if (lastAction == Rule.Action.ALLOW) {
                for (String value : argument.value) {
                    arguments.add(substitutor.replace(value));
                }
            }
        }

        try {
            String[] split = versionInfo.id.split("\\.");
            int minorVersion = Integer.parseInt(split[1]);
            int patch = Integer.parseInt(split[2]);

            // TODO this is a workaround
            if (minorVersion == 16 && patch == 5) {
                arguments.add("-Dminecraft.api.auth.host=https://nope.invalid");
                arguments.add("-Dminecraft.api.account.host=https://nope.invalid");
                arguments.add("-Dminecraft.api.session.host=https://nope.invalid");
                arguments.add("-Dminecraft.api.services.host=https://nope.invalid");
            }
        } catch (Exception ignored) {

        }

        arguments.add(versionInfo.mainClass);

        /*for (String arg : versionInfo.gameArgs) {
            arguments.add(substitutor.replace(arg));
        }*/

        for (Argument argument : versionInfo.gameArgs) {
            Rule.Action lastAction = Rule.Action.DISALLOW;
            if (argument.rules == null || argument.rules.isEmpty()) {
                lastAction = Rule.Action.ALLOW;
            } else {
                for (Rule rule : argument.rules) {
                    Os os = rule.os;
                    if (os == null) {
                        lastAction = rule.action;
                    } else {
                        boolean versionMatches = os.version != null &&
                                Pattern.compile(os.version).matcher(EnumOS.getVersion()).matches();
                        if (EnumOS.getOsName().equals(os.name) ||
                                versionMatches || EnumOS.getArch().equals("x" + os.arch)) {
                            lastAction = rule.action;
                        }
                    }
                }
            }

            if (lastAction == Rule.Action.ALLOW) {
                for (String value : argument.value) {
                    arguments.add(substitutor.replace(value));
                }
            }
        }

        arguments.remove("--demo"); // :)

        return arguments;
    }

    private List<String> buildRunCommand(List<String> arguments) {
        List<String> command = new ArrayList<>();

        command.add(this.getJavaExecutable());
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

    private String getJavaExecutable() {
        String binDir = System.getProperty("java.home") + File.separator + "bin";
        //String binDir = "E:\\Users\\Aleksey\\.jdks\\liberica-17.0.3\\bin";
        if (EnumOS.getOS() == EnumOS.WINDOWS) {
            return binDir + File.separator + "javaw.exe";
        } else {
            return binDir + File.separator + "java";
        }
    }
}
