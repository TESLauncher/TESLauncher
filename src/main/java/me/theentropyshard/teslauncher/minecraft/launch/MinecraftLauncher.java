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

package me.theentropyshard.teslauncher.minecraft.launch;

import me.theentropyshard.teslauncher.BuildConfig;
import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.logging.Log;
import me.theentropyshard.teslauncher.minecraft.account.Account;
import me.theentropyshard.teslauncher.minecraft.account.microsoft.MicrosoftAccount;
import me.theentropyshard.teslauncher.minecraft.data.AssetIndex;
import me.theentropyshard.teslauncher.minecraft.data.Library;
import me.theentropyshard.teslauncher.minecraft.data.Version;
import me.theentropyshard.teslauncher.minecraft.data.argument.Argument;
import me.theentropyshard.teslauncher.minecraft.data.argument.ArgumentType;
import me.theentropyshard.teslauncher.utils.FileUtils;
import me.theentropyshard.teslauncher.utils.OperatingSystem;
import me.theentropyshard.teslauncher.utils.ProcessReader;
import me.theentropyshard.teslauncher.utils.json.Json;
import org.apache.commons.text.StringSubstitutor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MinecraftLauncher {


    private final Path librariesDir;
    private final Path runtimesDir;
    private final Path nativesDir;
    private final String overrideJavaExecutable;

    private final List<String> classpath;

    public MinecraftLauncher(Path librariesDir, Path runtimesDir, Path nativesDir, String overrideJavaExecutable) {
        this.librariesDir = librariesDir;
        this.runtimesDir = runtimesDir;
        this.nativesDir = nativesDir;
        this.overrideJavaExecutable = overrideJavaExecutable;

        this.classpath = new ArrayList<>();
    }

    public int launch(Consumer<List<String>> beforeLaunch, Account account, Version version,
                      Path runDir, Path minecraftDir, long minMem, long maxMem, Set<String> jvmFlags, boolean exitAfterLaunch) throws IOException {
        this.classpath.clear();

        beforeLaunch.accept(this.classpath);
        List<String> arguments = this.getArguments(account, version, this.nativesDir, this.librariesDir, minecraftDir, minMem, maxMem,
            jvmFlags);
        List<String> command = this.buildRunCommand(version, arguments, this.runtimesDir);

        return this.runGameProcess(command, runDir, account, exitAfterLaunch);
    }

    private void resolveClasspath(Version version, Path librariesDir) {
        for (Library library : version.getLibraries()) {
            Library.Artifact artifact = library.getDownloads().getArtifact();
            if (artifact == null) {
                continue;
            }

            if (library.applyOnThisPlatform()) {
                this.classpath.add(librariesDir.resolve(artifact.getPath()).toAbsolutePath().toString());
            }
        }
    }

    private List<String> getArguments(Account account, Version version, Path tmpNativesDir, Path librariesDir, Path minecraftDir,
                                      long minMemoryMegabytes, long maxMemoryMegabytes, Set<String> jvmFlags
    ) throws IOException {
        TESLauncher launcher = TESLauncher.getInstance();
        FileUtils.createDirectoryIfNotExists(minecraftDir);
        Path assetsDir = launcher.getAssetsDir();

        List<String> arguments = new ArrayList<>();

        arguments.add("-Dfile.encoding=utf-8");
        arguments.add("-Dconsole.encoding=utf-8");

        if (jvmFlags != null && !jvmFlags.isEmpty()) {
            arguments.addAll(jvmFlags);
        }

        this.resolveClasspath(version, librariesDir);

        Map<String, Object> argumentMap = new HashMap<>();

        // JVM
        argumentMap.put("natives_directory", tmpNativesDir.toAbsolutePath().toString());
        argumentMap.put("launcher_name", BuildConfig.APP_NAME);
        argumentMap.put("launcher_version", BuildConfig.APP_VERSION);
        argumentMap.put("classpath", String.join(File.pathSeparator, this.classpath));

        Version.AssetIndex vAssetIndex = version.getAssetIndex();
        AssetIndex assetIndex = Json.parse(
            FileUtils.readUtf8(assetsDir.resolve("indexes").resolve(vAssetIndex.getId() + ".json")),
            AssetIndex.class
        );

        boolean newFormat = version.getMinecraftArguments() == null;

        // Game
        if (newFormat) {
            argumentMap.put("client", "-");
            argumentMap.put("auth_xuid", "-");
            argumentMap.put("auth_player_name", account.getUsername());
            argumentMap.put("version_name", version.getId());
            argumentMap.put("game_directory", minecraftDir.toAbsolutePath().toString());
            argumentMap.put("assets_root", assetsDir.toAbsolutePath().toString());
            argumentMap.put("assets_index_name", version.getAssets());
            argumentMap.put("auth_uuid", account.getUuid().toString());
            argumentMap.put("auth_access_token", account.getAccessToken());
            argumentMap.put("user_type", "msa");
            argumentMap.put("version_type", version.getType().getJsonName());
        } else {
            argumentMap.put("auth_uuid", account.getUuid().toString());
            argumentMap.put("auth_access_token", account.getAccessToken());
            argumentMap.put("auth_session", "-");
            argumentMap.put("user_properties", "-");
            argumentMap.put("game_directory", minecraftDir.toAbsolutePath().toString());
            argumentMap.put("version_type", version.getType().getJsonName());
            argumentMap.put("user_type", "msa");
            argumentMap.put("assets_index_name", version.getAssets());
            argumentMap.put("version_name", version.getId());
            argumentMap.put("auth_player_name", account.getUsername());
            argumentMap.put("uuid", account.getUuid().toString());
            argumentMap.put("accessToken", account.getAccessToken());
            if (assetIndex.isMapToResources()) {
                argumentMap.put("assets_root", minecraftDir.resolve("resources"));
                argumentMap.put("game_assets", minecraftDir.resolve("resources"));
            } else if (assetIndex.isVirtual()) {
                Path virtualAssets = assetsDir.resolve("virtual").resolve(vAssetIndex.getId()).toAbsolutePath();
                argumentMap.put("assets_root", virtualAssets.toString());
                argumentMap.put("game_assets", virtualAssets.toString());
            } else {
                argumentMap.put("assets_root", assetsDir.toString());
                argumentMap.put("game_assets", assetsDir.toString());
            }
        }

        argumentMap.put("user_properties", "{}");

        argumentMap.put("resolution_width", "960");
        argumentMap.put("resolution_height", "540");

        StringSubstitutor substitutor = new StringSubstitutor(argumentMap);

        arguments.add("-Xms" + minMemoryMegabytes + "m");
        arguments.add("-Xmx" + maxMemoryMegabytes + "m");

        if (newFormat) {
            for (Argument argument : version.getArguments().get(ArgumentType.JVM)) {
                if (argument.applyOnThisPlatform()) {
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
                Log.info("Fooling Minecraft 1.16.5, so multiplayer works for offline account");

                arguments.add("-Dminecraft.api.auth.host=https://nope.invalid");
                arguments.add("-Dminecraft.api.account.host=https://nope.invalid");
                arguments.add("-Dminecraft.api.session.host=https://nope.invalid");
                arguments.add("-Dminecraft.api.services.host=https://nope.invalid");
            }
        } catch (Exception ignored) {

        }

        String mainClass = version.getMainClass();
        Log.info("Main class: " + mainClass);
        arguments.add(mainClass);

        if (newFormat) {
            for (Argument argument : version.getArguments().get(ArgumentType.GAME)) {
                if (argument.applyOnThisPlatform()) {
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

        if (this.overrideJavaExecutable == null) {
            command.add(MinecraftLauncher.getMojangJavaExecutable(version, runtimesDir));
        } else {
            Log.info("Using custom JRE: " + this.overrideJavaExecutable);
            command.add(this.overrideJavaExecutable);
        }
        command.addAll(arguments);

        return command;
    }

    private int runGameProcess(List<String> command, Path runDir, Account account, boolean exitAfterLaunch) throws IOException {
        Log.info("Working directory: " + runDir);

        List<String> censoredCommand = command.stream()
            .map(s -> (account instanceof MicrosoftAccount) ? s.replace(account.getAccessToken(), "**ACCESSTOKEN**") : s)
            .map(s -> s.replace(account.getUsername(), "**USERNAME**"))
            .map(s -> s.replace(account.getUuid().toString(), "**UUID**"))
            .collect(Collectors.toList());
        Log.info("Running: " + censoredCommand);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.environment().put("APPDATA", runDir.toString());
        processBuilder.directory(runDir.toFile());
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        if (exitAfterLaunch) {
            TESLauncher.getInstance().shutdown();
        }

        new ProcessReader(process).read(line -> {
            this.readProcessOutput(line, account);
        });

        try {
            return process.waitFor();
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

    private static String getMojangJavaExecutable(Version version, Path runtimesDir) {
        Version.JavaVersion javaVersion = version.getJavaVersion();

        if (javaVersion != null) {
            String javaExecutable = MinecraftLauncher.getJavaExecutable(javaVersion.getComponent(), runtimesDir);
            Log.info("Using Mojang JRE " + javaVersion.getMajorVersion() + ": " + javaExecutable);

            return javaExecutable;
        } else {
            String fallbackJavaPath = MinecraftLauncher.getFallbackJavaPath(version.getId(), runtimesDir);
            Log.warn("Could not find Java version in version info. Using fallback JRE: " + fallbackJavaPath);

            return fallbackJavaPath;
        }
    }

    public static String getFallbackJavaPath(String minecraftVersion, Path runtimesDir) {
        try {
            String[] parts = minecraftVersion.split("\\.");
            int minorVersion = Integer.parseInt(parts[1]);
            int patch = Integer.parseInt(parts[2]);

            if (minorVersion >= 21 || minorVersion == 20 && patch >= 5) {
                return MinecraftLauncher.getJavaExecutable("java-runtime-delta", runtimesDir);
            } else if (minorVersion >= 18) {
                return MinecraftLauncher.getJavaExecutable("java-runtime-gamma", runtimesDir);
            } else if (minorVersion == 17) {
                return MinecraftLauncher.getJavaExecutable("java-runtime-alpha", runtimesDir);
            } else {
                return MinecraftLauncher.getJavaExecutable("jre-legacy", runtimesDir);
            }
        } catch (Exception ignored) {
            return MinecraftLauncher.getJavaExecutable("jre-legacy", runtimesDir);
        }
    }

    public static String getJavaExecutable(String componentName, Path runtimesDir) {
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
