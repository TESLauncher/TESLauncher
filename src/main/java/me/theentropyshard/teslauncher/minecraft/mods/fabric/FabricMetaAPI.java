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

package me.theentropyshard.teslauncher.minecraft.mods.fabric;

import com.google.gson.JsonObject;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Objects;

import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.utils.json.Json;

public class FabricMetaAPI {
    public static final String OFFICIAL_FABRIC_URL = "https://meta.fabricmc.net/";

    private final String url;

    public FabricMetaAPI() {
        this(FabricMetaAPI.OFFICIAL_FABRIC_URL);
    }

    public FabricMetaAPI(String url) {
        this.url = url;
    }

    public JsonObject getLauncherProfile(String minecraftVersion, String loaderVersion) throws IOException {
        Request request = new Request.Builder()
            .url(HttpUrl.get(this.url + "v2/versions/loader/" + minecraftVersion + "/" + loaderVersion + "/profile/json"))
            .build();

        try (Response response = TESLauncher.getInstance().getHttpClient().newCall(request).execute()) {
            return Json.parse(Objects.requireNonNull(response.body()).string(), JsonObject.class);
        }
    }
}
