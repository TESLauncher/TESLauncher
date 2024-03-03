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

package me.theentropyshard.teslauncher.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import me.theentropyshard.teslauncher.accounts.Account;
import me.theentropyshard.teslauncher.accounts.AccountDeserializer;
import me.theentropyshard.teslauncher.gson.ActionTypeAdapter;
import me.theentropyshard.teslauncher.gson.DetailedVersionInfoDeserializer;
import me.theentropyshard.teslauncher.gson.InstantTypeAdapter;
import me.theentropyshard.teslauncher.minecraft.Rule;
import me.theentropyshard.teslauncher.minecraft.VersionInfo;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Map;

public final class Json {
    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            //
            .registerTypeAdapter(VersionInfo.class, new DetailedVersionInfoDeserializer())
            .registerTypeAdapter(Rule.Action.class, new ActionTypeAdapter())
            .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
            .registerTypeAdapter(Account.class, new AccountDeserializer())
            //
            .create();

    private static final Gson PRETTY_GSON = Json.GSON.newBuilder().setPrettyPrinting().create();

    public static <T> T parse(String json, Type clazz) {
        return Json.GSON.fromJson(json, clazz);
    }

    public static <T> T parse(JsonElement element, Type clazz) {
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
