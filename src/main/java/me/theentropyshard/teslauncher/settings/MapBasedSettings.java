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
    public String getValue(String key) {
        return this.data.get(key);
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
