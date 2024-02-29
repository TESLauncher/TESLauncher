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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.minecraft.*;
import me.theentropyshard.teslauncher.utils.Json;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DetailedVersionInfoDeserializer extends AbstractJsonDeserializer<VersionInfo> {
    private static final Logger LOG = LogManager.getLogger(DetailedVersionInfoDeserializer.class);

    public DetailedVersionInfoDeserializer() {

    }

    private List<Argument> processArgs(JsonArray array) {
        List<Argument> arguments = new ArrayList<>();

        for (JsonElement elem : array) {
            if (elem.isJsonPrimitive()) {
                String stringArg = elem.getAsString();
                arguments.add(Argument.withValues(stringArg));
            } else if (elem.isJsonObject()) {
                Argument argument = Json.parse(elem.getAsJsonObject(), Argument.class);
                arguments.add(argument);
            }
        }

        return arguments;
    }

    @Override
    public VersionInfo deserialize(JsonObject root) throws JsonParseException {
        VersionInfo versionInfo = new VersionInfo();

        if (root.has("javaVersion")) {
            versionInfo.javaVersion = Json.parse(root.get("javaVersion"), JavaVersion.class);
        } else {
            LOG.warn("Unable to find javaVersion key");
        }

        if (root.has("arguments")) {
            versionInfo.newFormat = true;

            JsonObject argsObject = root.getAsJsonObject("arguments");

            JsonArray gameArgsArr = argsObject.getAsJsonArray("game");
            JsonArray jvmArgsArr = argsObject.getAsJsonArray("jvm");

            List<Argument> gameArguments = this.processArgs(gameArgsArr);
            List<Argument> jvmArguments = this.processArgs(jvmArgsArr);

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

        versionInfo.downloads = Json.parse(root.get("downloads"), ClientDownloads.class);

        JsonArray libsArray = root.getAsJsonArray("libraries");

        Library[] libraries = Json.parse(libsArray, Library[].class);
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
            versionInfo.assetIndex = Json.parse(assetIndex, VersionAssetIndex.class);
        }

        return versionInfo;
    }
}
