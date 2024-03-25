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

package me.theentropyshard.teslauncher.network.download;

import me.theentropyshard.teslauncher.utils.FileUtils;
import me.theentropyshard.teslauncher.utils.HashUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

public class HttpDownload {
    private static final Logger LOG = LogManager.getLogger(HttpDownload.class);

    private OkHttpClient httpClient;
    private final String url;
    private final Path saveAs;
    private final boolean forceDownload;
    private final String sha1;
    private final boolean executable;
    private final long expectedSize;

    private HttpDownload(OkHttpClient httpClient, String url, Path saveAs, boolean forceDownload, String sha1, boolean executable, long expectedSize) {
        this.httpClient = httpClient;
        this.url = url;
        this.saveAs = saveAs;
        this.forceDownload = forceDownload;
        this.sha1 = sha1;
        this.executable = executable;
        this.expectedSize = expectedSize;
    }

    public Path getSaveAs() {
        return this.saveAs;
    }

    public void execute() throws IOException {
        if (this.saveAs == null) {
            throw new NullPointerException("saveAs == null");
        }

        long size = this.size();

        if (size == this.expectedSize && this.expectedSize != -1) {
            return;
        }

        boolean partiallyDownloaded = this.expectedSize > size;

        if (partiallyDownloaded || this.forceDownload || !Files.exists(this.saveAs)) {
            Request.Builder builder = new Request.Builder()
                    .url(this.url)
                    .get();

            if (partiallyDownloaded && size >= 0) {
                builder.header("Range", "bytes=" + size + "-");
            }

            FileUtils.createDirectoryIfNotExists(this.saveAs.getParent());

            try (Response response = this.httpClient.newCall(builder.build()).execute();
                 InputStream is = new BufferedInputStream(Objects.requireNonNull(response.body()).byteStream())) {
                if (partiallyDownloaded && size >= 0) {
                    try (FileChannel fileChannel = FileChannel.open(this.saveAs, StandardOpenOption.APPEND)) {
                        fileChannel.transferFrom(Channels.newChannel(is), size, Long.MAX_VALUE);
                    }
                } else {
                    Files.copy(is, this.saveAs, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            if (this.sha1 != null) {
                String sha1 = HashUtils.sha1(this.saveAs);
                if (!this.sha1.equals(sha1)) {
                    FileUtils.delete(this.saveAs);
                    throw new IOException("SHA-1 does not match for file '" + this.saveAs + "'. Bad file was deleted");
                }
            }

            if (this.executable) {
                new File(this.saveAs.toString()).setExecutable(true);
            }
        }
    }

    public long size() {
        if (this.exists()) {
            try {
                return Files.size(this.saveAs);
            } catch (IOException e) {
                LOG.error(e);
            }
        }

        return -1;
    }

    public void setHttpClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public long expectedSize() {
        return this.expectedSize;
    }

    public boolean exists() {
        return this.saveAs != null && Files.exists(this.saveAs);
    }

    public static final class Builder {
        private OkHttpClient httpClient;
        private String url;
        private Path saveAs;
        private String sha1;
        private boolean forceDownload;
        private boolean executable;
        private long expectedSize = -1L;

        public Builder() {

        }

        public Builder httpClient(OkHttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder saveAs(Path saveAs) {
            this.saveAs = saveAs;
            return this;
        }

        public Builder forceDownload() {
            this.forceDownload = true;
            return this;
        }

        public Builder executable(boolean executable) {
            this.executable = executable;
            return this;
        }

        public Builder sha1(String sha1) {
            this.sha1 = sha1;
            return this;
        }

        public Builder expectedSize(long expectedSize) {
            this.expectedSize = expectedSize;
            return this;
        }

        public HttpDownload build() {
            return new HttpDownload(this.httpClient, this.url, this.saveAs, this.forceDownload, this.sha1, this.executable, this.expectedSize);
        }
    }
}
