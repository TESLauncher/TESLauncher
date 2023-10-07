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

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

// TODO: this one does not really count progress, we need to make a wrapper around a byte channel
public class FileDownloaderNIO extends FileDownloader {
    public FileDownloaderNIO(String userAgent) {
        super(userAgent);
    }

    @Override
    public void download(String url, Path savePath, long bytesAlreadyHave, ProgressListener listener) throws IOException {
        Response response = this.makeRequest(url, bytesAlreadyHave);

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
