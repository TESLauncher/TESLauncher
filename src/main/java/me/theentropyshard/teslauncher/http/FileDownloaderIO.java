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
