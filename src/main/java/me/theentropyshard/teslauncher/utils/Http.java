/*
 *  Copyright 2023 TheEntropyShard (https://github.com/TheEntropyShard)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package me.theentropyshard.teslauncher.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public final class Http {
    public static final String USER_AGENT = "TESLauncher/1.0.0";

    private static final String GET_METHOD = "GET";
    private static final String POST_METHOD = "POST";

    private static HttpURLConnection buildConnection(String url, String method) throws IOException {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setRequestMethod(method);
        c.setRequestProperty("User-Agent", Http.USER_AGENT);

        return c;
    }

    public static byte[] get(String url) throws IOException {
        HttpURLConnection c = Http.buildConnection(url, Http.GET_METHOD);

        return Http.inputStreamToByteArray(c.getErrorStream() == null ? c.getInputStream() : c.getErrorStream());
    }

    public static byte[] post(String url, String contentType, byte[] payload) throws IOException {
        HttpURLConnection c = Http.buildConnection(url, Http.POST_METHOD);
        c.setRequestProperty("Content-Type", contentType);
        c.setRequestProperty("Content-Length", String.valueOf(payload.length));
        c.setDoOutput(true);
        OutputStream outputStream = c.getOutputStream();
        outputStream.write(payload);
        outputStream.flush();

        return Http.inputStreamToByteArray(c.getErrorStream() == null ? c.getInputStream() : c.getErrorStream());
    }

    private static byte[] inputStreamToByteArray(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[512];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int numRead;
        while ((numRead = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, numRead);
        }
        baos.flush();

        return baos.toByteArray();
    }

    private Http() {
        throw new UnsupportedOperationException();
    }
}
