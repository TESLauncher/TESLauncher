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

package me.theentropyshard.teslauncher.gson;

import com.google.gson.*;
import me.theentropyshard.teslauncher.minecraft.Argument;
import me.theentropyshard.teslauncher.minecraft.Library;
import me.theentropyshard.teslauncher.minecraft.Os;
import me.theentropyshard.teslauncher.minecraft.Rule;
import me.theentropyshard.teslauncher.minecraft.models.VersionAssetIndex;
import me.theentropyshard.teslauncher.minecraft.models.VersionInfo;
import me.theentropyshard.teslauncher.utils.EnumOS;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class DetailedVersionInfoDeserializerOld implements JsonDeserializer<VersionInfo> {
    public DetailedVersionInfoDeserializerOld() {

    }

    @Override
    public VersionInfo deserialize(JsonElement element, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        VersionInfo versionInfo = new VersionInfo();

        JsonObject root = element.getAsJsonObject();

        if (root.has("arguments")) {
            versionInfo.newFormat = true;

            JsonObject argsObject = root.getAsJsonObject("arguments");

            JsonArray gameArgsArr = argsObject.getAsJsonArray("game");
            JsonArray jvmArgsArr = argsObject.getAsJsonArray("jvm");

            List<Argument> gameArguments = new ArrayList<>();
            List<Argument> jvmArguments = new ArrayList<>();

            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Rule.Action.class, new ActionTypeAdapter())
                    .create();

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
                    versionInfo.gameArgs.add(argument);
                }
            }

            for (Argument argument : jvmArguments) {
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

        JsonArray libsArray = root.getAsJsonArray("libraries");

        List<String> libs = new ArrayList<>();
        for (JsonElement elem : libsArray) {
            JsonObject libObject = elem.getAsJsonObject();
            JsonObject downloadsObject = libObject.getAsJsonObject("downloads");
            if (downloadsObject.has("artifact")) {
                JsonObject artifactObject = downloadsObject.getAsJsonObject("artifact");
                libs.add(artifactObject.get("path").getAsString());
            }
        }
        versionInfo.librariesPaths.addAll(libs);

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
