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

package me.theentropyshard.teslauncher.settings;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class JsonSettings extends MapBasedSettings {
    private final Gson gson;

    public JsonSettings(Gson gson) {
        this(gson, StandardCharsets.UTF_8);
    }

    public JsonSettings(Gson gson, Charset charset) {
        super(charset);
        this.gson = gson;
    }

    @Override
    public String serialize(Map<String, String> data) throws IOException {
        return this.gson.toJson(data);
    }

    @Override
    public void load(InputStream inputStream) throws IOException {
        Reader reader = new InputStreamReader(inputStream, this.getCharset());
        JsonObject json = this.gson.fromJson(reader, JsonObject.class);
        if (json == null) {
            return;
        }

        Map<String, JsonElement> map = json.asMap();
        for (Map.Entry<String, JsonElement> pair : map.entrySet()) {
            this.setValue(pair.getKey(), pair.getValue().getAsString());
        }
    }
}
