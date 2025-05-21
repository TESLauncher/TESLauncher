/*
 * TESLauncher - https://github.com/TESLauncher/TESLauncher
 * Copyright (C) 2023-2025 TESLauncher
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

package me.theentropyshard.teslauncher.mclogs;

import me.theentropyshard.teslauncher.mclogs.model.LimitsResponse;
import me.theentropyshard.teslauncher.mclogs.model.PasteResponse;
import me.theentropyshard.teslauncher.utils.CallUnwrapAdapter;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class McLogsApi {
    public static final String BASE_URL = "https://api.mclo.gs/";

    private final Retrofit retrofit;
    private final McLogsHttpApi mcLogsApi;

    public McLogsApi(OkHttpClient httpClient) {
        this.retrofit = new Retrofit.Builder()
            .baseUrl(McLogsApi.BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(new CallUnwrapAdapter.Factory())
            .build();

        this.mcLogsApi = this.retrofit.create(McLogsHttpApi.class);
    }

    public PasteResponse pasteLog(String log) {
        return this.mcLogsApi.pasteLog(log);
    }

    public LimitsResponse getLimits() {
        return this.mcLogsApi.getLimits();
    }
}
