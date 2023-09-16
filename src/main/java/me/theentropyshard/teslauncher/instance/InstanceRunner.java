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
import me.theentropyshard.teslauncher.minecraft.MinecraftDownloader;
import me.theentropyshard.teslauncher.minecraft.models.VersionInfo;
import me.theentropyshard.teslauncher.utils.EnumOS;
import me.theentropyshard.teslauncher.utils.Http;
import me.theentropyshard.teslauncher.utils.PathUtils;
import org.apache.commons.text.StringSubstitutor;

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
            TESLauncher launcher = TESLauncher.instance;
            InstanceManager instanceManager = launcher.getInstanceManager();
            Path mcDirOfInstance = PathUtils.createDirectories(instanceManager.getMcDirOfInstance(this.instance.getName()));
            Path tmpNativesDir = PathUtils.createDirectories(
                    launcher.getInstancesDir().resolve(this.instance.getName()).resolve("natives-tmp")
                            .resolve(this.instance.getMinecraftVersion())
            );

            Path clientsDir = launcher.getClientsDir();
            Path librariesDir = launcher.getLibrariesDir();
            if (!this.instance.wasEverPlayed()) {
                MinecraftDownloader downloader = new MinecraftDownloader(
                        clientsDir,
                        launcher.getAssetsDir(),
                        librariesDir,
                        tmpNativesDir
                );

                downloader.downloadMinecraft(this.instance.getMinecraftVersion());

                this.instance.setWasEverPlayed(true);
                instanceManager.saveInstance(this.instance);
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

            List<String> classpath = new ArrayList<>();
            for (String libPath : versionInfo.librariesPaths) {
                classpath.add(librariesDir.resolve(libPath).toAbsolutePath().toString());
            }
            classpath.add(clientsDir.resolve(versionInfo.id).resolve(versionInfo.id + ".jar").toAbsolutePath().toString());

            Map<String, Object> argVars = new HashMap<>();

            boolean hasLogInfo = versionInfo.logConfigId != null && versionInfo.logConfigUrl != null &&
                    versionInfo.logArgument != null;

            if (hasLogInfo) {
                Path log4j2ConfigsDir = TESLauncher.instance.getLog4j2ConfigsDir();

                Path log4j2ConfigFile = log4j2ConfigsDir.resolve(versionInfo.logConfigId);
                if (!Files.exists(log4j2ConfigFile)) {
                    byte[] bytes = Http.get(versionInfo.logConfigUrl);
                    Files.write(log4j2ConfigFile, bytes);
                }

                argVars.put("path", log4j2ConfigFile.toAbsolutePath().toString());
            }

            // JVM
            argVars.put("natives_directory", tmpNativesDir.toAbsolutePath().toString());
            argVars.put("launcher_name", "TESLauncher");
            argVars.put("launcher_version", "1.0.0");
            argVars.put("classpath", String.join(File.pathSeparator, classpath));

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
                Path assetsAbsolute = launcher.getAssetsDir().toAbsolutePath();
                argVars.put("assets_root", assetsAbsolute.toString());
                argVars.put("game_assets", assetsAbsolute.resolve("virtual").resolve("legacy").toString());
            }

            StringSubstitutor substitutor = new StringSubstitutor(argVars);

            List<String> jvmArgs = new ArrayList<>();

            if (hasLogInfo) {
                jvmArgs.add(versionInfo.logArgument);
            }

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
            processBuilder.directory(instanceManager.getMcDirOfInstance(this.instance.getName()).getParent().toAbsolutePath().toFile());
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            int exitCode = process.waitFor();
            System.out.println("Minecraft process exited with exit code " + exitCode);
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
