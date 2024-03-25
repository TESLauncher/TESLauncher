/*
 * TESLauncher - https://github.com/TESLauncher/TESLauncher
 * Copyright (C) 2023-2024 TESLauncher
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

package me.theentropyshard.teslauncher.network;

import okhttp3.*;

import java.io.IOException;
import java.util.Objects;

public class HttpRequest implements AutoCloseable {
    private final OkHttpClient httpClient;

    private Response response;

    public HttpRequest(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    private void send(String url, RequestBody requestBody, Headers headers) throws IOException {
        if (this.response != null) {
            return;
        }

        Request.Builder builder = new Request.Builder().url(url).headers(headers);

        if (requestBody != null) {
            builder.post(requestBody);
        }

        this.response = this.httpClient.newCall(builder.build()).execute();
    }

    public String asString(String url) throws IOException {
        this.send(url, null, Headers.of());

        return Objects.requireNonNull(this.response.body()).string();
    }

    public String asString(String url, Headers headers) throws IOException {
        this.send(url, null, headers);

        return Objects.requireNonNull(this.response.body()).string();
    }

    public String asString(String url, RequestBody requestBody) throws IOException {
        this.send(url, requestBody, Headers.of());

        return Objects.requireNonNull(this.response.body()).string();
    }

    public String asString(String url, RequestBody requestBody, Headers headers) throws IOException {
        this.send(url, requestBody, headers);

        return Objects.requireNonNull(this.response.body()).string();
    }

    public int code() {
        if (this.response == null) {
            return -1;
        }

        return this.response.code();
    }

    @Override
    public void close() {
        if (this.response != null) {
            this.response.close();
        }
    }
}
