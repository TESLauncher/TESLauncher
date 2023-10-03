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

package me.theentropyshard.teslauncher.http;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;

public class FileDownloaderNIO extends FileDownloader {
    public FileDownloaderNIO(String userAgent) {
        super(userAgent);
    }

    @Override
    public void download(String url, Path savePath, ProgressListener listener) throws IOException {
        Response response = this.makeRequest(url);

        try (ReadableByteChannel rbc = Channels.newChannel(response.getInputStream());
             FileOutputStream fos = new FileOutputStream(savePath.toFile());
             FileChannel channel = fos.getChannel()) {
            long bytesWritten = 0;
            while (bytesWritten < response.getContentLength()) {
                bytesWritten += channel.transferFrom(rbc, bytesWritten, response.getContentLength() - bytesWritten);
                listener.onProgress(response.getContentLength(), bytesWritten, false, savePath.getFileName().toString());
            }
            listener.onProgress(response.getContentLength(), bytesWritten, true, savePath.getFileName().toString());
        }
    }
}
