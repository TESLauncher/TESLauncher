package me.theentropyshard.teslauncher.settings;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
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
