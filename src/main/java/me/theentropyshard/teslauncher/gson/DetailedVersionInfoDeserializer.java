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

package me.theentropyshard.teslauncher.gson;

import com.google.gson.*;
import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.minecraft.*;
import me.theentropyshard.teslauncher.minecraft.VersionAssetIndex;
import me.theentropyshard.teslauncher.minecraft.VersionInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DetailedVersionInfoDeserializer extends AbstractJsonDeserializer<VersionInfo> {
    private final TESLauncher launcher;

    public DetailedVersionInfoDeserializer(TESLauncher launcher) {
        this.launcher = launcher;
    }

    @Override
    public VersionInfo deserialize(JsonObject root) throws JsonParseException {
        VersionInfo versionInfo = new VersionInfo();

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Rule.Action.class, new ActionTypeAdapter())
                .create();

        if (root.has("javaVersion")) {
            versionInfo.javaVersion = gson.fromJson(root.get("javaVersion"), JavaVersion.class);
        } else {
            this.launcher.getLogger().warn("Unable to find javaVersion key");
        }

        if (root.has("arguments")) {
            versionInfo.newFormat = true;

            JsonObject argsObject = root.getAsJsonObject("arguments");

            JsonArray gameArgsArr = argsObject.getAsJsonArray("game");
            JsonArray jvmArgsArr = argsObject.getAsJsonArray("jvm");

            List<Argument> gameArguments = new ArrayList<>();
            List<Argument> jvmArguments = new ArrayList<>();

            for (JsonElement elem : gameArgsArr) {
                if (elem.isJsonPrimitive()) {
                    String stringArg = elem.getAsString();
                    gameArguments.add(Argument.withValues(stringArg));
                } else if (elem.isJsonObject()) {
                    Argument argument = gson.fromJson(elem.getAsJsonObject(), Argument.class);
                    gameArguments.add(argument);
                }
            }

            for (JsonElement elem : jvmArgsArr) {
                if (elem.isJsonPrimitive()) {
                    String stringArg = elem.getAsString();
                    jvmArguments.add(Argument.withValues(stringArg));
                } else if (elem.isJsonObject()) {
                    Argument argument = gson.fromJson(elem.getAsJsonObject(), Argument.class);
                    jvmArguments.add(argument);
                }
            }

            for (Argument argument : gameArguments) {
                if (RuleMatcher.applyOnThisPlatform(argument)) {
                    versionInfo.gameArgs.add(argument);
                }
            }

            for (Argument argument : jvmArguments) {
                if (RuleMatcher.applyOnThisPlatform(argument)) {
                    versionInfo.jvmArgs.add(argument);
                }
            }
        } else if (root.has("minecraftArguments")) {
            versionInfo.newFormat = false;
            String minecraftArguments = root.get("minecraftArguments").getAsString();
            versionInfo.gameArgs.add(Argument.withValues(minecraftArguments.split("\\s")));

            // Old format does not have any JVM args preset, so we set it manually here
            versionInfo.jvmArgs.add(Argument.withValues("-Djava.library.path=${natives_directory}"));

            versionInfo.jvmArgs.add(Argument.withValues("-cp"));
            versionInfo.jvmArgs.add(Argument.withValues("${classpath}"));

        } else {
            throw new JsonParseException("Minecraft arguments were not found");
        }

        versionInfo.mainClass = root.get("mainClass").getAsString();
        versionInfo.id = root.get("id").getAsString();
        versionInfo.type = root.get("type").getAsString();
        versionInfo.assets = root.get("assets").getAsString();

        versionInfo.downloads = gson.fromJson(root.get("downloads"), ClientDownloads.class);

        JsonArray libsArray = root.getAsJsonArray("libraries");

        Library[] libraries = new GsonBuilder()
                .registerTypeAdapter(Rule.Action.class, new ActionTypeAdapter())
                .create().fromJson(libsArray, Library[].class);
        versionInfo.libraries.addAll(Arrays.asList(libraries));

        if (root.has("logging")) {
            JsonObject loggingObject = root.getAsJsonObject("logging");
            JsonObject clientObject = loggingObject.getAsJsonObject("client");
            versionInfo.logArgument = clientObject.get("argument").getAsString();
            JsonObject fileObject = clientObject.getAsJsonObject("file");
            versionInfo.logConfigId = fileObject.get("id").getAsString();
            versionInfo.logConfigUrl = fileObject.get("url").getAsString();
        }

        if (root.has("assetIndex")) {
            JsonObject assetIndex = root.getAsJsonObject("assetIndex");
            versionInfo.assetIndex = new Gson().fromJson(assetIndex, VersionAssetIndex.class);
        }

        return versionInfo;
    }
}
