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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FileDownloaderIO extends FileDownloader {
    public FileDownloaderIO(String userAgent) {
        super(userAgent);
    }

    private StandardOpenOption[] getOpenOptions(boolean newDownload) {
        if (newDownload) {
            return new StandardOpenOption[] {StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE};
        } else {
            return new StandardOpenOption[] {StandardOpenOption.APPEND};
        }
    }

    @Override
    public void download(String url, Path savePath, long bytesAlreadyHave, ProgressListener listener) throws IOException {
        Response response = this.makeRequest(url, bytesAlreadyHave);

        try (InputStream inputStream = response.getInputStream();
             OutputStream out = Files.newOutputStream(savePath.toFile().toPath(), this.getOpenOptions(bytesAlreadyHave == 0));
             OutputStream outputStream = new BufferedOutputStream(out)) {
            byte[] buffer = new byte[2048];
            long count = 0L;
            int numRead;
            do {
                numRead = inputStream.read(buffer);
                if (numRead != -1) {
                    count += numRead;
                    outputStream.write(buffer, 0, numRead);
                }
                if (bytesAlreadyHave > 0) {
                    listener.onProgress(response.getContentLength() + bytesAlreadyHave, bytesAlreadyHave + count, numRead == -1, savePath.getFileName().toString());
                } else {
                    listener.onProgress(response.getContentLength(), count, numRead == -1, savePath.getFileName().toString());
                }
            } while (numRead != -1);

            outputStream.flush();
        }
    }
}
