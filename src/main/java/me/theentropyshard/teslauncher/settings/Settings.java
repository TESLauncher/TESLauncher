package me.theentropyshard.teslauncher.settings;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Settings {
    void load(InputStream inputStream) throws IOException;

    void save(OutputStream outputStream) throws IOException;

    String getValue(String key);

    void setValue(String key, String value);

    boolean isEmpty();
}
