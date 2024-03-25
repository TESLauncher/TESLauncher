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

package me.theentropyshard.teslauncher.network.progress;

import okhttp3.Interceptor;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ProgressNetworkInterceptor implements Interceptor {
    private final ProgressListener progressListener;

    public ProgressNetworkInterceptor(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        @SuppressWarnings("resource")
        Response originalResponse = chain.proceed(chain.request());
        return originalResponse.newBuilder()
                .body(new ProgressResponseBody(originalResponse.body(), this.progressListener))
                .build();
    }
}
