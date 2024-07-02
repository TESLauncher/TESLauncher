/*
 * TESLauncher - https://github.com/TESLauncher/TESLauncher
 * Copyright (C) 2023-2024 TESLauncher
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

package me.theentropyshard.teslauncher.minecraft.launch;

import me.theentropyshard.teslauncher.BuildConfig;
import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.accounts.Account;
import me.theentropyshard.teslauncher.accounts.MicrosoftAccount;
import me.theentropyshard.teslauncher.minecraft.*;
import me.theentropyshard.teslauncher.minecraft.argument.Argument;
import me.theentropyshard.teslauncher.minecraft.argument.ArgumentType;
import me.theentropyshard.teslauncher.utils.FileUtils;
import me.theentropyshard.teslauncher.utils.OperatingSystem;
import me.theentropyshard.teslauncher.utils.ProcessReader;
import me.theentropyshard.teslauncher.utils.json.Json;
import org.apache.commons.text.StringSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MinecraftLauncher {
    private static final Logger LOG = LogManager.getLogger(MinecraftLauncher.class);

    private final Path librariesDir;
    private final Path runtimesDir;
    private final Path nativesDir;

    private final List<String> classpath;

    public MinecraftLauncher(Path librariesDir, Path runtimesDir, Path nativesDir) {
        this.librariesDir = librariesDir;
        this.runtimesDir = runtimesDir;
        this.nativesDir = nativesDir;

        this.classpath = new ArrayList<>();
    }

    public int launch(Consumer<List<String>> beforeLaunch, Account account, Version version,
                      Path runDir, Path minecraftDir, long minMem, long maxMem) throws IOException {
        this.classpath.clear();

        beforeLaunch.accept(this.classpath);
        List<String> arguments = this.getArguments(account, version, this.nativesDir, this.librariesDir, minecraftDir, minMem, maxMem);
        List<String> command = this.buildRunCommand(version, arguments, this.runtimesDir);

        return this.runGameProcess(command, runDir);
    }

    private void resolveClasspath(Version version, Path librariesDir) {
        for (Library library : version.getLibraries()) {
            Library.Artifact artifact = library.getDownloads().getArtifact();
            if (artifact == null) {
                continue;
            }

            if (RuleMatcher.applyOnThisPlatform(library)) {
                this.classpath.add(librariesDir.resolve(artifact.getPath()).toAbsolutePath().toString());
            }
        }
    }

    private List<String> getArguments(Account account, Version version, Path tmpNativesDir, Path librariesDir, Path minecraftDir,
                                      long minMemoryMegabytes, long maxMemoryMegabytes
    ) throws IOException {
        TESLauncher launcher = TESLauncher.getInstance();
        FileUtils.createDirectoryIfNotExists(minecraftDir);
        Path assetsDir = launcher.getAssetsDir();

        List<String> arguments = new ArrayList<>();

        arguments.add("-Dfile.encoding=utf-8");
        arguments.add("-Dconsole.encoding=utf-8");

        this.resolveClasspath(version, librariesDir);

        Map<String, Object> argVars = new HashMap<>();

        // JVM
        argVars.put("natives_directory", tmpNativesDir.toAbsolutePath().toString());
        argVars.put("launcher_name", BuildConfig.APP_NAME);
        argVars.put("launcher_version", BuildConfig.APP_VERSION);
        argVars.put("classpath", String.join(File.pathSeparator, this.classpath));

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
            argVars.put("auth_player_name", account.getUsername());
            argVars.put("version_name", version.getId());
            argVars.put("game_directory", minecraftDir.toAbsolutePath().toString());
            argVars.put("assets_root", assetsDir.toAbsolutePath().toString());
            argVars.put("assets_index_name", version.getAssets());
            argVars.put("auth_uuid", account.getUuid().toString());
            argVars.put("auth_access_token", account.getAccessToken());
            argVars.put("user_type", "msa");
            argVars.put("version_type", version.getType().getJsonName());
        } else {
            argVars.put("auth_uuid", account.getUuid().toString());
            argVars.put("auth_access_token", account.getAccessToken());
            argVars.put("auth_session", "-");
            argVars.put("user_properties", "-");
            argVars.put("game_directory", minecraftDir.toAbsolutePath().toString());
            argVars.put("version_type", version.getType().getJsonName());
            argVars.put("user_type", "msa");
            argVars.put("assets_index_name", version.getAssets());
            argVars.put("version_name", version.getId());
            argVars.put("auth_player_name", account.getUsername());
            argVars.put("uuid", account.getUuid().toString());
            argVars.put("accessToken", account.getAccessToken());
            if (assetIndex.isMapToResources()) {
                argVars.put("assets_root", minecraftDir.resolve("resources"));
                argVars.put("game_assets", minecraftDir.resolve("resources"));
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

        arguments.add("-Xms" + minMemoryMegabytes + "m");
        arguments.add("-Xmx" + maxMemoryMegabytes + "m");

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

            if (minorVersion == 16 && patch == 5 && !(account instanceof MicrosoftAccount)) {
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

    private List<String> buildRunCommand(Version version, List<String> arguments, Path runtimesDir) {
        List<String> command = new ArrayList<>();

        String javaExecutable;

        Version.JavaVersion javaVersion = version.getJavaVersion();
        if (javaVersion != null) {
            javaExecutable = this.getJavaExecutable(javaVersion.getComponent(), runtimesDir);
        } else {
            try {
                String[] split = version.getId().split("\\.");
                int minorVersion = Integer.parseInt(split[1]);

                if (minorVersion >= 17) {
                    javaExecutable = this.getJavaExecutable("java-runtime-gamma", runtimesDir);
                } else {
                    javaExecutable = this.getJavaExecutable("jre-legacy", runtimesDir);
                }
            } catch (Exception ignored) {
                javaExecutable = this.getJavaExecutable("jre-legacy", runtimesDir);
            }
        }

        command.add(javaExecutable);
        command.addAll(arguments);

        return command;
    }

    private int runGameProcess(List<String> command, Path runDir) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.environment().put("APPDATA", runDir.toString());
        processBuilder.directory(runDir.toFile());
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        new ProcessReader(process).read(this::readProcessOutput);

        try {
            return process.waitFor();
        } catch (InterruptedException e) {
            throw new IOException("Unable to wait for process to end");
        }
    }

    private void readProcessOutput(String line) {
        MinecraftError.checkForError(line);

        LOG.info(line);
    }

    private String getJavaExecutable(String componentName, Path runtimesDir) {
        Path componentDir = runtimesDir.resolve(componentName);

        if (OperatingSystem.isMacOS()) {
            componentDir = componentDir.resolve("jre.bundle").resolve("Contents").resolve("Home");
        }

        return componentDir
                .resolve("bin")
                .resolve(OperatingSystem.getCurrent().getJavaExecutableName())
                .toString();
    }
}
