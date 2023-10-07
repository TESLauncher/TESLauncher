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

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import me.theentropyshard.teslauncher.minecraft.Rule;

import java.io.IOException;

public class ActionTypeAdapter extends TypeAdapter<Rule.Action> {
    public ActionTypeAdapter() {

    }

    @Override
    public void write(JsonWriter writer, Rule.Action action) throws IOException {
        writer.value(action.getJsonName());
    }

    @Override
    public Rule.Action read(JsonReader reader) throws IOException {
        return Rule.Action.getByName(reader.nextString());
    }
}
