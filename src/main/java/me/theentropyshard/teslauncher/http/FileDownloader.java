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

import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.network.ProgressListener;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public abstract class FileDownloader {
    private static final ProgressListener NO_OP_LISTENER = ((bytesRead, contentLength, done, fileName) -> {
    });

    private String userAgent;

    public FileDownloader(String userAgent) {
        this.userAgent = userAgent;
    }

    public ResponseBody makeRequest(String url, long downloadedBytes) throws IOException {
        Request.Builder builder = new Request.Builder()
                .url(url)
                .get();

        if (downloadedBytes > 0) {
            builder.header("Range", "bytes=" + downloadedBytes + "-");
        }

        Response response = TESLauncher.getInstance().getHttpClient().newCall(builder.build()).execute();
        return Objects.requireNonNull(response.body());
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
