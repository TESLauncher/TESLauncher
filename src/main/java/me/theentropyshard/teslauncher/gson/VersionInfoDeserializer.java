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
import me.theentropyshard.teslauncher.minecraft.models.VersionInfo;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VersionInfoDeserializer implements JsonDeserializer<VersionInfo> {
    public VersionInfoDeserializer() {

    }

    @Override
    public VersionInfo deserialize(JsonElement element, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        VersionInfo versionInfo = new VersionInfo();

        JsonObject root = element.getAsJsonObject();

        if (root.has("arguments")) {
            versionInfo.newFormat = true;

            JsonObject argsObject = root.getAsJsonObject("arguments");

            JsonArray gameArgsArr = argsObject.getAsJsonArray("game");
            for (JsonElement elem : gameArgsArr) {
                if (elem.isJsonPrimitive()) {
                    String stringArg = elem.getAsString();
                    versionInfo.gameArgs.add(stringArg);
                } else if (elem.isJsonObject()) {
                    // TODO
                    elem.getAsJsonObject();
                }
            }

            JsonArray jvmArgsArr = argsObject.getAsJsonArray("jvm");
            for (JsonElement elem : jvmArgsArr) {
                if (elem.isJsonPrimitive()) {
                    String stringArg = elem.getAsString();
                    versionInfo.jvmArgs.add(stringArg);
                } else if (elem.isJsonObject()) {
                    // TODO
                    elem.getAsJsonObject();
                }
            }
        } else if (root.has("minecraftArguments")) {
            versionInfo.newFormat = false;
            String minecraftArguments = root.get("minecraftArguments").getAsString();
            versionInfo.gameArgs.addAll(Arrays.asList(minecraftArguments.split("\\s")));

            // Old format does not have any JVM args preset, so we set it manually here
            versionInfo.jvmArgs.add("-Djava.library.path=${natives_directory}");

            versionInfo.jvmArgs.add("-cp");
            versionInfo.jvmArgs.add("${classpath}");

        } else {
            throw new JsonParseException("Minecraft arguments were not found");
        }

        versionInfo.mainClass = root.get("mainClass").getAsString();
        versionInfo.id = root.get("id").getAsString();
        versionInfo.type = root.get("type").getAsString();
        versionInfo.assets = root.get("assets").getAsString();

        List<String> libs = new ArrayList<>();
        JsonArray libsArray = root.getAsJsonArray("libraries");
        for (JsonElement elem : libsArray) {
            JsonObject libObject = elem.getAsJsonObject();
            JsonObject downloadsObject = libObject.getAsJsonObject("downloads");
            if (downloadsObject.has("artifact")) {
                JsonObject artifactObject = downloadsObject.getAsJsonObject("artifact");
                libs.add(artifactObject.get("path").getAsString());
            }
        }
        versionInfo.librariesPaths.addAll(libs);

        if (root.has("logging")) {
            JsonObject loggingObject = root.getAsJsonObject("logging");
            JsonObject clientObject = loggingObject.getAsJsonObject("client");
            versionInfo.logArgument = clientObject.get("argument").getAsString();
            JsonObject fileObject = clientObject.getAsJsonObject("file");
            versionInfo.logConfigId = fileObject.get("id").getAsString();
            versionInfo.logConfigUrl = fileObject.get("url").getAsString();
        }

        return versionInfo;
    }
}
