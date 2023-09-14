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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class HttpResponse implements Closeable {
    private final int responseCode;
    private final InputStream inputStream;
    private final int contentLength;
    private final boolean successful;

    public HttpResponse(int responseCode, InputStream inputStream, int contentLength) {
        this.responseCode = responseCode;
        this.inputStream = inputStream;
        this.contentLength = contentLength;
        this.successful = responseCode < 400;
    }

    @Override
    public void close() throws IOException {
        if (this.inputStream != null) {
            this.inputStream.close();
        }
    }

    @Override
    public String toString() {
        return "HttpResponse[responseCode = " + this.responseCode + "]";
    }

    public int getResponseCode() {
        return this.responseCode;
    }

    public InputStream getInputStream() {
        return this.inputStream;
    }

    public int getContentLength() {
        return this.contentLength;
    }

    public boolean isSuccessful() {
        return this.successful;
    }
}
