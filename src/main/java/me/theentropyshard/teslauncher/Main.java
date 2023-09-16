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

package me.theentropyshard.teslauncher;

import me.theentropyshard.teslauncher.http.FileDownloader;
import me.theentropyshard.teslauncher.http.FileDownloaderImpl;
import me.theentropyshard.teslauncher.http.ProgressListener;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        //new TESLauncher(args);

        ProgressListener listener = new ProgressListener() {
            private boolean firstTime;

            @Override
            public void onProgress(long bytesRead, long contentLength, boolean done) {
                if (done) {
                    System.out.println("Completed");
                } else {
                    if (this.firstTime) {
                        this.firstTime = false;

                        if (contentLength == -1) {
                            System.out.println("Content-Length: unknown");
                        } else {
                            System.out.format("Content-Length: %d\n", contentLength);
                        }
                    }

                    System.out.println(bytesRead);

                    if (contentLength != -1) {
                        System.out.format("%d%% done\n", (100 * bytesRead) / contentLength);
                    }
                }
            }
        };
        FileDownloader downloader = new FileDownloaderImpl("TESLauncher/1.0.0", listener);

        Path savePath = Paths.get("./1.12.2.jar");
        try {
            downloader.download("https://piston-data.mojang.com/v1/objects/0f275bc1547d01fa5f56ba34bdc87d981ee12daf/client.jar", savePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
