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

package me.theentropyshard.teslauncher.network;

import okhttp3.OkHttpClient;

public class Network {
    public static final String USER_AGENT = "TESLauncher/1.0.0";

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .addNetworkInterceptor(new UserAgentInterceptor(Network.USER_AGENT))
            .build();

    public static OkHttpClient getHttpClient() {
        return Network.HTTP_CLIENT;
    }

    public static OkHttpClient createProgressClient(ProgressListener listener) {
        return Network.HTTP_CLIENT.newBuilder()
                .addNetworkInterceptor(new ProgressInterceptor(listener))
                .build();
    }
}
