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

import me.theentropyshard.teslauncher.TESLauncher;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Http {
    private static final String GET_METHOD = "GET";

    private static HttpURLConnection buildConnection(String url, String method) throws IOException {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setRequestMethod(method);
        c.setRequestProperty("User-Agent", TESLauncher.USER_AGENT);

        return c;
    }

    public static byte[] get(String url) throws IOException {
        return Http.get(url, new NopProgressListener());
    }

    public static byte[] get(String url, ProgressListener listener) throws IOException {
        HttpURLConnection c = Http.buildConnection(url, Http.GET_METHOD);

        return Http.inputStreamToByteArray(Http.getNeededInputStream(c), listener, c);
    }

    private static InputStream getNeededInputStream(HttpURLConnection c) throws IOException {
        return c.getErrorStream() == null ? c.getInputStream() : c.getErrorStream();
    }

    private static byte[] inputStreamToByteArray(InputStream inputStream, ProgressListener listener, HttpURLConnection c) throws IOException {
        long contentLengthLong = c.getContentLengthLong();

        byte[] buffer = new byte[8192];
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (BufferedInputStream input = new BufferedInputStream(inputStream)) {
            long count = 0L;
            int numRead;
            do {
                numRead = input.read(buffer, 0, 8192);
                if (numRead != -1) {
                    count += numRead;
                    output.write(buffer, 0, numRead);
                }
                listener.onData(contentLengthLong, count, numRead == -1);
            } while (numRead != -1);
        }

        return output.toByteArray();
    }

    public interface ProgressListener {
        void onData(long totalBytes, long currentBytes, boolean done);
    }

    public static final class NopProgressListener implements ProgressListener {
        public void onData(long totalBytes, long currentBytes, boolean done) {
            // No-operation progress listener
        }
    }

    private Http() {
        throw new UnsupportedOperationException();
    }
}
