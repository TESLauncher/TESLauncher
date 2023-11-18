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
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

public class OkHttpFileDownloader extends FileDownloader {
    public static final StandardOpenOption[] NEW_FILE_OPTIONS = {StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE};
    public static final StandardOpenOption[] APPEND_OPTIONS = {StandardOpenOption.APPEND};

    private final OkHttpClient httpClient;

    public OkHttpFileDownloader(OkHttpClient httpClient) {
        super(TESLauncher.USER_AGENT);

        this.httpClient = httpClient;
    }

    private static StandardOpenOption[] getOpenOptions(boolean newDownload) {
        if (newDownload) {
            return OkHttpFileDownloader.NEW_FILE_OPTIONS;
        } else {
            return OkHttpFileDownloader.APPEND_OPTIONS;
        }
    }

    @Override
    public void download(String url, Path savePath, long bytesAlreadyHave, ProgressListener listener) throws IOException {
        Request.Builder builder = new Request.Builder();
        if (bytesAlreadyHave > 0) {
            builder.header("Range", "bytes=" + bytesAlreadyHave + "-");
        }
        Request request = builder
                .url(url)
                .get()
                .build();

        this.httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.err.println("Failed to download " + url);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response r) throws IOException {
                try (Response response = r) {
                    long contentLength = Long.parseLong(Objects.requireNonNull(response.header("Content-Length")));

                    try (InputStream inputStream = Objects.requireNonNull(response.body()).byteStream();
                         OutputStream out = Files.newOutputStream(savePath.toFile().toPath(), OkHttpFileDownloader.getOpenOptions(bytesAlreadyHave == 0));
                         OutputStream outputStream = new BufferedOutputStream(out)) {
                        byte[] buffer = new byte[1024 * 8];
                        long count = 0L;
                        int numRead;
                        do {
                            numRead = inputStream.read(buffer);
                            if (numRead != -1) {
                                count += numRead;
                                outputStream.write(buffer, 0, numRead);
                            }
                            if (bytesAlreadyHave > 0) {
                                listener.onProgress(contentLength + bytesAlreadyHave, bytesAlreadyHave + count, numRead == -1, savePath.getFileName().toString());
                            } else {
                                listener.onProgress(contentLength, count, numRead == -1, savePath.getFileName().toString());
                            }
                        } while (numRead != -1);

                        outputStream.flush();
                    }
                }
            }
        });
    }
}
