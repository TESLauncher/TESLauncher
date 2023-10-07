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

    public Response makeRequest(String url, long downloadedBytes) throws IOException {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setRequestMethod("GET");
        c.setRequestProperty("User-Agent", this.userAgent);
        if (downloadedBytes > 0) {
            c.setRequestProperty("Range", "bytes=" + downloadedBytes + "-");
        }

        InputStream inputStream = c.getErrorStream() == null ? c.getInputStream() : c.getErrorStream();

        return new Response(inputStream, c.getContentLengthLong(), c.getResponseCode());
    }

    public void download(String url, Path savePath, long bytesAlreadyHave) throws IOException {
        this.download(url, savePath, bytesAlreadyHave, FileDownloader.NO_OP_LISTENER);
    }

    public abstract void download(String url, Path savePath, long bytesAlreadyHave, ProgressListener listener) throws IOException;

    public String getUserAgent() {
        return this.userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
