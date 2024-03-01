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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DownloadList {
    public static final int MAX_CONNECTIONS = 8;

    private final DownloadListener downloadListener;
    private final List<HttpDownload> downloads;
    private final ExecutorService executorService;
    private final AtomicInteger completedTasks;

    private boolean finished;

    public DownloadList(DownloadListener downloadListener) {
        this.downloadListener = downloadListener;
        this.downloads = new ArrayList<>();
        this.executorService = Executors.newFixedThreadPool(DownloadList.MAX_CONNECTIONS);
        this.completedTasks = new AtomicInteger(0);
    }

    public synchronized void add(HttpDownload download) {
        this.downloads.add(download);
    }

    public int size() {
        return this.downloads.size();
    }

    public synchronized void downloadAll() throws IOException {
        if (this.finished) {
            throw new IllegalStateException("This download list has already finished downloading. Please consider creating a new one");
        }

        for (HttpDownload download : this.downloads) {
            DownloadTask downloadTask = new DownloadTask(
                    () -> {
                        try {
                            download.execute();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    },
                    () -> {
                        int completed = this.completedTasks.incrementAndGet();
                        this.downloadListener.onTaskFinished(this.downloads.size(), completed);
                    }
            );

            this.executorService.execute(downloadTask);
        }

        this.executorService.shutdown();
        try {
            if (!this.executorService.awaitTermination(15, TimeUnit.MINUTES)) {
                this.executorService.shutdownNow();
            }
        } catch (InterruptedException ex) {
            this.executorService.shutdownNow();
        }

        this.finished = true;
    }
}
