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

public final class Json {
    private static final Gson GSON = new Gson();

    public static <T> T parse(String json, Class<T> clazz) {
        return Json.GSON.fromJson(json, clazz);
    }

    public static String write(Object o) {
        return Json.GSON.toJson(o);
    }

    private Json() {
        throw new UnsupportedOperationException();
    }
}
