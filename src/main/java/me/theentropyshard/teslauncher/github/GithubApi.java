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

package me.theentropyshard.teslauncher.github;

import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.network.HttpRequest;
import me.theentropyshard.teslauncher.network.download.HttpDownload;
import me.theentropyshard.teslauncher.network.progress.ProgressListener;
import me.theentropyshard.teslauncher.network.progress.ProgressNetworkInterceptor;
import me.theentropyshard.teslauncher.utils.json.Json;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GithubApi {
    private static final String URL_LATEST = "https://api.github.com/repos/%s/%s/releases/latest";
    private static final String URL = "https://api.github.com/repos/%s/%s/releases";

    public GithubRelease getLatestRelease(String owner, String repo) throws IOException {
        try (HttpRequest request = new HttpRequest(TESLauncher.getInstance().getHttpClient())) {
            return Json.parse(request.asString(String.format(GithubApi.URL_LATEST, owner, repo)), GithubRelease.class);
        }
    }

    public List<GithubRelease> getAllReleases(String owner, String repo) throws IOException {
        try (HttpRequest request = new HttpRequest(TESLauncher.getInstance().getHttpClient())) {
            String string = request.asString(GithubApi.URL.formatted(owner, repo));

            return new ArrayList<>(List.of(Json.parse(string, GithubRelease[].class)));
        }
    }

    public void downloadRelease(Path saveAs, GithubRelease release, int index, ProgressListener listener) throws IOException {
        GithubRelease.Asset asset = release.assets.get(index);

        OkHttpClient httpClient = TESLauncher.getInstance().getHttpClient().newBuilder()
                .addNetworkInterceptor(new ProgressNetworkInterceptor(listener))
                .build();

        HttpDownload download = new HttpDownload.Builder()
                .url(asset.browser_download_url)
                .saveAs(saveAs)
                .expectedSize(asset.size)
                .httpClient(httpClient)
                .build();

        download.execute();
    }
}
