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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public abstract class MapBasedSettings implements Settings {
    private final Charset charset;
    private final Map<String, String> data;

    public MapBasedSettings(Charset charset) {
        this.charset = charset;
        this.data = new HashMap<>();
    }

    public abstract String serialize(Map<String, String> data) throws IOException;

    @Override
    public void save(OutputStream outputStream) throws IOException {
        Writer writer = new OutputStreamWriter(outputStream, this.charset);
        writer.write(this.serialize(new HashMap<>(this.data)));
        writer.flush();
    }

    @Override
    public String getString(String key) {
        return this.data.get(key);
    }

    @Override
    public String getString(String key, String def) {
        String string = this.getString(key);
        if (string == null) {
            return def;
        } else {
            return string;
        }
    }

    @Override
    public int getInt(String key) {
        return Integer.parseInt(this.getString(key));
    }

    @Override
    public int getInt(String key, int def) {
        try {
            return Integer.parseInt(this.getString(key));
        } catch (Exception e) {
            e.printStackTrace();
            return def;
        }
    }

    @Override
    public long getLong(String key) {
        return Long.parseLong(this.getString(key));
    }

    @Override
    public long getLong(String key, long def) {
        try {
            return Long.parseLong(this.getString(key));
        } catch (Exception e) {
            e.printStackTrace();
            return def;
        }
    }

    @Override
    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(this.getString(key));
    }

    @Override
    public boolean getBoolean(String key, boolean def) {
        String string = this.getString(key);
        if (string == null) {
            return def;
        }
        return Boolean.parseBoolean(string);
    }

    @Override
    public float getFloat(String key) {
        return Float.parseFloat(this.getString(key));
    }

    @Override
    public float getFloat(String key, float def) {
        try {
            return Float.parseFloat(this.getString(key));
        } catch (Exception e) {
            e.printStackTrace();
            return def;
        }
    }

    @Override
    public double getDouble(String key) {
        return Double.parseDouble(this.getString(key));
    }

    @Override
    public double getDouble(String key, double def) {
        try {
            return Double.parseDouble(this.getString(key));
        } catch (Exception e) {
            e.printStackTrace();
            return def;
        }
    }

    @Override
    public void setValue(String key, String value) {
        this.data.put(key, value);
    }

    @Override
    public boolean isEmpty() {
        return this.data.isEmpty();
    }

    public Charset getCharset() {
        return this.charset;
    }
}
