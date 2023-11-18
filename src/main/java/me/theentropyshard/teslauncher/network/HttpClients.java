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

import me.theentropyshard.teslauncher.network.progress.ProgressListener;
import me.theentropyshard.teslauncher.network.progress.ProgressNetworkInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public final class HttpClients {
    private static OkHttpClient HTTP_CLIENT;

    public static void init(String userAgent) {
        HttpClients.HTTP_CLIENT = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.MINUTES)
                .addNetworkInterceptor(new UserAgentInterceptor(userAgent))
                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                .build();
    }

    private static void checkInit() {
        if (HttpClients.HTTP_CLIENT == null) {
            throw new IllegalStateException("HttpClients is not initialized. Call HttpClients.init(); please");
        }
    }

    public static OkHttpClient getDefault() {
        HttpClients.checkInit();

        return HttpClients.HTTP_CLIENT;
    }

    public static OkHttpClient withUserAgent(String userAgent) {
        return HttpClients.withUserAgent(HttpClients.getDefault(), userAgent);
    }

    public static OkHttpClient withUserAgent(OkHttpClient baseClient, String userAgent) {
        return baseClient.newBuilder()
                .addNetworkInterceptor(new UserAgentInterceptor(userAgent))
                .build();
    }

    public static OkHttpClient withProgress(ProgressListener progressListener) {
        return HttpClients.withProgress(HttpClients.getDefault(), progressListener);
    }

    public static OkHttpClient withProgress(OkHttpClient baseClient, ProgressListener progressListener) {
        return baseClient.newBuilder()
                .addNetworkInterceptor(new ProgressNetworkInterceptor(progressListener))
                .build();
    }

    private HttpClients() {
        throw new UnsupportedOperationException();
    }
}
