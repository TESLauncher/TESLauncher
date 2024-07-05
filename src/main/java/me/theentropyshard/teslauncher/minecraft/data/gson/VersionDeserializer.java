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

package me.theentropyshard.teslauncher.minecraft.data.gson;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import me.theentropyshard.teslauncher.minecraft.data.DownloadType;
import me.theentropyshard.teslauncher.minecraft.data.Library;
import me.theentropyshard.teslauncher.minecraft.data.Version;
import me.theentropyshard.teslauncher.minecraft.data.VersionType;
import me.theentropyshard.teslauncher.minecraft.data.argument.Argument;
import me.theentropyshard.teslauncher.minecraft.data.argument.ArgumentType;

import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class VersionDeserializer implements JsonDeserializer<Version> {
    public VersionDeserializer() {

    }

    @Override
    public Version deserialize(JsonElement root, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        JsonObject jsonObject = root.getAsJsonObject();

        Version version = new Version();

        version.setAssetIndex(ctx.deserialize(jsonObject.get("assetIndex"), Version.AssetIndex.class));

        if (jsonObject.has("javaVersion")) {
            version.setJavaVersion(ctx.deserialize(jsonObject.get("javaVersion"), Version.JavaVersion.class));
        }

        version.setId(jsonObject.get("id").getAsString());
        version.setMainClass(jsonObject.get("mainClass").getAsString());
        version.setType(ctx.deserialize(jsonObject.get("type"), VersionType.class));
        version.setReleaseTime(ctx.deserialize(jsonObject.get("releaseTime"), OffsetDateTime.class));
        version.setTime(ctx.deserialize(jsonObject.get("time"), OffsetDateTime.class));

        if (jsonObject.has("minecraftArguments")) {
            version.setMinecraftArguments(jsonObject.get("minecraftArguments").getAsString());
        }

        version.setAssets(jsonObject.get("assets").getAsString());

        if (jsonObject.has("complianceLevel")) {
            version.setComplianceLevel(jsonObject.get("complianceLevel").getAsInt());
        } else {
            version.setComplianceLevel(0);
        }

        version.setDownloads(ctx.deserialize(jsonObject.get("downloads"), new TypeToken<EnumMap<DownloadType, Version.Download>>() {}.getType()));
        version.setLibraries(ctx.deserialize(jsonObject.get("libraries"), new TypeToken<List<Library>>() {}.getType()));

        if (jsonObject.has("arguments")) {
            EnumMap<ArgumentType, List<Argument>> processedArguments = new EnumMap<>(ArgumentType.class);

            EnumMap<ArgumentType, JsonArray> unprocessedArguments = ctx.deserialize(jsonObject.get("arguments"), new TypeToken<EnumMap<ArgumentType, JsonArray>>() {}.getType());
            processedArguments.put(ArgumentType.JVM, this.processArgs(unprocessedArguments.get(ArgumentType.JVM), ctx));
            processedArguments.put(ArgumentType.GAME, this.processArgs(unprocessedArguments.get(ArgumentType.GAME), ctx));

            version.setArguments(processedArguments);
        }

        return version;
    }

    private List<Argument> processArgs(JsonArray array, JsonDeserializationContext ctx) {
        List<Argument> arguments = new ArrayList<>();

        for (JsonElement element : array) {
            if (element.isJsonPrimitive()) {
                arguments.add(Argument.withValues(element.getAsString()));
            } else if (element.isJsonObject()) {
                arguments.add(ctx.deserialize(element.getAsJsonObject(), Argument.class));
            }
        }

        return arguments;
    }
}
