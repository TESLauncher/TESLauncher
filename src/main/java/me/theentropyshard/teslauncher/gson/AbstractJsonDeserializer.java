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

package me.theentropyshard.teslauncher.gson;

import com.google.gson.*;

import java.lang.reflect.Type;

public abstract class AbstractJsonDeserializer<T> implements JsonDeserializer<T> {
    public abstract T deserialize(JsonObject root);

    @Override
    public T deserialize(JsonElement element, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        return this.deserialize(element.getAsJsonObject());
    }
}
