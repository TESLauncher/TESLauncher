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

package me.theentropyshard.teslauncher.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;

public abstract class FileDownloader {
    private static final ProgressListener NO_OP_LISTENER = ((bytesRead, contentLength, done, fileName) -> {
    });

    private String userAgent;

    public FileDownloader(String userAgent) {
        this.userAgent = userAgent;
    }

    public Response makeRequest(String url) throws IOException {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setRequestMethod("GET");
        c.setRequestProperty("User-Agent", this.userAgent);

        InputStream inputStream = c.getErrorStream() == null ? c.getInputStream() : c.getErrorStream();

        return new Response(inputStream, c.getContentLengthLong());
    }

    public void download(String url, Path savePath) throws IOException {
        this.download(url, savePath, FileDownloader.NO_OP_LISTENER);
    }

    public abstract void download(String url, Path savePath, ProgressListener listener) throws IOException;

    public String getUserAgent() {
        return this.userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
