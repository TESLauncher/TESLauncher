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
import me.theentropyshard.teslauncher.logging.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

public class HttpDownload {
    

    private static final long EXPECTED_SIZE_NOT_SET = -1L;

    private OkHttpClient httpClient;
    private final String url;
    private final Path saveAs;
    private final Path copyTo;
    private final boolean forceDownload;
    private final String sha1;
    private final boolean executable;
    private final long expectedSize;

    private HttpDownload(OkHttpClient httpClient, String url, Path saveAs, Path copyTo, boolean forceDownload, String sha1, boolean executable, long expectedSize) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient == null");
        this.url = Objects.requireNonNull(url, "url == null");
        this.saveAs = Objects.requireNonNull(saveAs, "saveAs == null");
        this.copyTo = copyTo;
        this.forceDownload = forceDownload;
        this.sha1 = sha1;
        this.executable = executable;
        this.expectedSize = expectedSize;
    }

    public Path getSaveAs() {
        return this.saveAs;
    }

    public void execute() throws IOException {
        boolean needsDownload = false;

        if (Files.exists(this.saveAs)) {
            long size = Files.size(this.saveAs);
            boolean hashMatches = HashUtils.sha1(this.saveAs).equals(this.sha1);

            if (this.expectedSize == size) {
                if (this.sha1 == null) {
                    return;
                } else {
                    if (hashMatches) {
                        return;
                    } else {
                        Log.debug("File '" + this.saveAs + "' exists, size matches, but SHA-1 does not match");
                    }

                    needsDownload = true;
                }
            } else if (this.expectedSize != HttpDownload.EXPECTED_SIZE_NOT_SET) {
                if (hashMatches) {
                    Log.debug("File '" + this.saveAs + "' exists, SHA-1 matches, but size does not match");
                } else {
                    Log.debug("File '" + this.saveAs + "' exists, but size and SHA-1 do not match");
                }

                needsDownload = true;
            }
        }

        long size = this.size();
        boolean partiallyDownloaded = this.expectedSize > size && Files.exists(this.saveAs);

        if (partiallyDownloaded || this.forceDownload || !Files.exists(this.saveAs) || needsDownload) {
            Request.Builder builder = new Request.Builder()
                    .url(this.url)
                    .get();

            if (partiallyDownloaded && size >= 0) {
                builder.header("Range", "bytes=" + size + "-");
            }

            FileUtils.createDirectoryIfNotExists(this.saveAs.getParent());

            this.downloadFile(builder.build(), partiallyDownloaded, size);
            this.checkHash();

            if (this.executable) {
                new File(this.saveAs.toString()).setExecutable(true);
            }

            this.copyFile();
        }
    }

    public void downloadFile(Request request, boolean partiallyDownloaded, long size) throws IOException {
        try (Response response = this.httpClient.newCall(request).execute();
             ReadableByteChannel src = Channels.newChannel(Objects.requireNonNull(response.body()).byteStream())) {
            if (partiallyDownloaded && size > 0) {
                try (FileChannel fileChannel = FileChannel.open(this.saveAs, StandardOpenOption.APPEND)) {
                    fileChannel.transferFrom(src, size, Long.MAX_VALUE);
                }
            } else {
                try (FileChannel fileChannel = FileChannel.open(this.saveAs,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
                    fileChannel.transferFrom(src, 0, Long.MAX_VALUE);
                }
            }
        }
    }

    public void checkHash() throws IOException {
        if (this.sha1 != null) {
            String sha1 = HashUtils.sha1(this.saveAs);
            if (!this.sha1.equals(sha1)) {
                FileUtils.delete(this.saveAs);
                throw new IOException("SHA-1 does not match for file '" + this.saveAs + "'. Bad file was deleted");
            }
        }
    }

    public void copyFile() throws IOException {
        if (this.copyTo == null) {
            return;
        }

        if (Files.exists(this.copyTo) && Files.size(this.copyTo) == this.expectedSize && HashUtils.sha1(this.copyTo).equals(this.sha1)) {
            return;
        }

        Files.copy(this.saveAs, this.copyTo, StandardCopyOption.REPLACE_EXISTING);
    }

    public long size() {
        if (Files.exists(this.saveAs)) {
            try {
                return Files.size(this.saveAs);
            } catch (IOException e) {
                Log.stackTrace("Could not get file size of " + this.saveAs, e);
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

    public static final class Builder {
        private OkHttpClient httpClient;
        private String url;
        private Path saveAs;
        private Path copyTo;
        private String sha1;
        private boolean forceDownload;
        private boolean executable;
        private long expectedSize = HttpDownload.EXPECTED_SIZE_NOT_SET;

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

        public Builder copyTo(Path copyTo) {
            this.copyTo = copyTo;
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
            return new HttpDownload(
                    this.httpClient, this.url, this.saveAs,
                    this.copyTo, this.forceDownload, this.sha1,
                    this.executable, this.expectedSize
            );
        }
    }
}
