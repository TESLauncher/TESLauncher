/*
 * TESLauncher - https://github.com/TESLauncher/TESLauncher
 * Copyright (C) 2023-2025 TESLauncher
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

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import me.theentropyshard.teslauncher.minecraft.data.DownloadType;

import java.io.IOException;

public class DownloadTypeTypeAdapter extends TypeAdapter<DownloadType> {
    public DownloadTypeTypeAdapter() {

    }

    @Override
    public void write(JsonWriter writer, DownloadType type) throws IOException {
        writer.value(type.getJsonName());
    }

    @Override
    public DownloadType read(JsonReader reader) throws IOException {
        return DownloadType.getByName(reader.nextString());
    }
}
