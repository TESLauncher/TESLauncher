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

package me.theentropyshard.teslauncher.utils.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import me.theentropyshard.teslauncher.minecraft.accounts.Account;
import me.theentropyshard.teslauncher.minecraft.accounts.AccountDeserializer;
import me.theentropyshard.teslauncher.minecraft.data.DownloadType;
import me.theentropyshard.teslauncher.minecraft.data.Version;
import me.theentropyshard.teslauncher.minecraft.data.VersionType;
import me.theentropyshard.teslauncher.minecraft.data.argument.ArgumentType;
import me.theentropyshard.teslauncher.minecraft.data.gson.*;
import me.theentropyshard.teslauncher.minecraft.data.rule.Rule;
import me.theentropyshard.teslauncher.utils.json.type.InstantTypeAdapter;
import me.theentropyshard.teslauncher.utils.json.type.LocalDateTimeTypeAdapter;
import me.theentropyshard.teslauncher.utils.json.type.OffsetDateTimeTypeAdapter;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public final class Json {
    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .disableHtmlEscaping()
            .disableJdkUnsafe()
            //
            .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeTypeAdapter())
            //
            .registerTypeAdapter(Account.class, new AccountDeserializer())
            .registerTypeAdapter(Version.class, new VersionDeserializer())
            .registerTypeAdapter(VersionType.class, new VersionTypeTypeAdapter())
            .registerTypeAdapter(Rule.Action.class, new RuleActionTypeAdapter())
            .registerTypeAdapter(ArgumentType.class, new ArgumentTypeTypeAdapter())
            .registerTypeAdapter(DownloadType.class, new DownloadTypeTypeAdapter())
            //
            .create();

    private static final Gson PRETTY_GSON = Json.GSON.newBuilder().setPrettyPrinting().create();

    public static <T> T parse(String json, Type clazz) {
        return Json.GSON.fromJson(json, clazz);
    }

    public static <T> T parse(String json, Class<T> clazz) {
        return Json.GSON.fromJson(json, clazz);
    }

    public static <T> T parse(JsonElement element, Type clazz) {
        return Json.GSON.fromJson(element, clazz);
    }

    public static <T> T parse(JsonElement element, Class<T> clazz) {
        return Json.GSON.fromJson(element, clazz);
    }

    public static String write(Object o) {
        return Json.GSON.toJson(o);
    }

    public static String writePretty(Object o) {
        return Json.PRETTY_GSON.toJson(o);
    }

    private Json() {
        throw new UnsupportedOperationException();
    }
}
