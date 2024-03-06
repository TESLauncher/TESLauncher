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

package me.theentropyshard.teslauncher.network.download;

import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.network.progress.ProgressNetworkInterceptor;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class DownloadList {
    public static final int MAX_CONNECTIONS = 8;

    private final DownloadListener downloadListener;
    private final List<HttpDownload> downloads;
    private final AtomicLong downloadedBytes;
    private long totalSize;

    private boolean finished;

    public DownloadList(DownloadListener downloadListener) {
        this.downloadListener = downloadListener;
        this.downloads = new ArrayList<>();
        this.downloadedBytes = new AtomicLong(0);
    }

    public synchronized void add(HttpDownload download) {
        this.totalSize += download.expectedSize();
        this.downloads.add(download);
    }

    public synchronized void addAll(Collection<HttpDownload> downloads) {
        downloads.forEach(this::add);
    }

    public int size() {
        return this.downloads.size();
    }

    public long getTotalSize() {
        return this.totalSize;
    }

    public synchronized void downloadAll() throws IOException {
        if (this.finished) {
            throw new IllegalStateException("This download list has already finished downloading. Please consider creating a new one");
        }

        ExecutorService executorService = Executors.newFixedThreadPool(DownloadList.MAX_CONNECTIONS);

        OkHttpClient parent = TESLauncher.getInstance().getHttpClient();

        for (HttpDownload download : this.downloads) {
            OkHttpClient httpClient = parent.newBuilder()
                    .addNetworkInterceptor(new ProgressNetworkInterceptor((
                            (contentLength, bytesRead, bytesThisTime, done) -> {
                        this.downloadListener.updateProgress(this.totalSize, this.downloadedBytes.addAndGet(bytesThisTime));
                    })))
                    .build();
            download.setHttpClient(httpClient);

            Runnable runnable = () -> {
                try {
                    download.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };

            executorService.execute(runnable);
        }

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(15, TimeUnit.MINUTES)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException ex) {
            executorService.shutdownNow();
        }

        this.finished = true;
    }
}
