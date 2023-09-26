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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileDownloaderIO extends FileDownloader {
    public FileDownloaderIO(String userAgent) {
        super(userAgent);
    }

    @Override
    public void download(String url, Path savePath, ProgressListener listener) throws IOException {
        Response response = this.makeRequest(url);

        try (InputStream inputStream = new BufferedInputStream(response.getInputStream(), 4096);
             OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(savePath.toFile().toPath()))) {
            byte[] buffer = new byte[4096];
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            long count = 0L;
            int numRead;
            do {
                numRead = inputStream.read(buffer, 0, 4096);
                if (numRead != -1) {
                    count += numRead;
                    output.write(buffer, 0, numRead);
                }
                listener.onProgress(response.getContentLength(), count, numRead == -1, savePath.getFileName().toString());
            } while (numRead != -1);

            outputStream.write(output.toByteArray());
            outputStream.flush();
        }
    }
}
