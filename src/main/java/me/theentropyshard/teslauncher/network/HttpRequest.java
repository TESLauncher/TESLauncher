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

package me.theentropyshard.teslauncher.network;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Objects;

public class HttpRequest implements AutoCloseable {
    private final OkHttpClient httpClient;
    private final Gson gson;

    private Response response;

    public HttpRequest(OkHttpClient httpClient) {
        this(httpClient, null);
    }

    public HttpRequest(OkHttpClient httpClient, Gson gson) {
        this.httpClient = httpClient;
        this.gson = gson;
    }

    private void send(String url) throws IOException {
        if (this.response != null) {
            return;
        }

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        this.response = this.httpClient.newCall(request).execute();
    }

    public String asString(String url) throws IOException {
        this.send(url);

        return Objects.requireNonNull(this.response.body()).string();
    }

    public <T> T asObject(String url, Class<T> clazz) throws IOException {
        if (this.gson == null) {
            throw new IllegalStateException("HttpRequest was created without Gson");
        }

        this.send(url);

        return this.gson.fromJson(Objects.requireNonNull(this.response.body()).string(), clazz);
    }

    @Override
    public void close() {
        if (this.response != null) {
            this.response.close();
        }
    }
}
