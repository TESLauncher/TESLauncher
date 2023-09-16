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

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FileDownloaderImpl implements FileDownloader {
    private final OkHttpClient httpClient;
    private final Map<String, String> headers;

    public FileDownloaderImpl(String userAgent, ProgressListener listener) {
        this.httpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(chain -> {
                    Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                            .body(new ProgressResponseBody(originalResponse.body(), listener))
                            .build();
                })
                .build();
        this.headers = new HashMap<>();
        this.headers.put("User-Agent", userAgent);
    }

    @Override
    public void download(String url, Path savePath) throws IOException {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        this.headers.forEach(builder::header);

        Request request = builder.build();
        try (Response response = this.httpClient.newCall(request).execute()) {
            ResponseBody body = response.body();
            Files.write(savePath, Objects.requireNonNull(body).bytes());
        }
    }
}
