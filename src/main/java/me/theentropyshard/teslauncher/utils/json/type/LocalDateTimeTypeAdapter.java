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

package me.theentropyshard.teslauncher.utils.json.type;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import me.theentropyshard.teslauncher.logging.Log;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public class LocalDateTimeTypeAdapter extends TypeAdapter<LocalDateTime> {


    public LocalDateTimeTypeAdapter() {

    }

    @Override
    public void write(JsonWriter writer, LocalDateTime dateTime) throws IOException {
        writer.value(dateTime.toString());
    }

    @Override
    public LocalDateTime read(JsonReader reader) throws IOException {
        String text = reader.nextString();

        try {
            return LocalDateTime.parse(text);
        } catch (DateTimeParseException e) {
            Log.warn("Cannot parse LocalDateTime '" + text + "'");
        }

        return LocalDateTime.MIN;
    }
}
