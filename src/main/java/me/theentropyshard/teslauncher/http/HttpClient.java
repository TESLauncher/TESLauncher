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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HttpClient {
    private final List<HttpHeader> commonHeaders;

    private String userAgent;

    public HttpClient() {
        this.commonHeaders = new ArrayList<>();
    }

    public HttpResponse send(HttpRequest request) throws IOException {
        HttpURLConnection c = (HttpURLConnection) new URL(request.getUrl()).openConnection();
        c.setRequestMethod(request.getMethod().toString());
        c.setDoInput(true);
        c.setDoOutput(true);

        HttpClient.setHeaders(c, this.commonHeaders);
        HttpClient.setHeaders(c, request.getAdditionalHeaders());

        if (this.userAgent != null) {
            c.setRequestProperty("User-Agent", this.userAgent);
        }

        if (request.getMethod() == HttpMethod.POST) {
            byte[] payload = request.getPayload();

            if (payload == null) {
                throw new IOException("Null payload in POST request");
            }

            c.setRequestProperty("Content-Length", String.valueOf(payload.length));
            c.setRequestProperty("Content-Type", request.getContentType());

            OutputStream outputStream = c.getOutputStream();
            outputStream.write(payload);
            outputStream.flush();
        }

        int responseCode = c.getResponseCode();
        InputStream inputStream = c.getErrorStream() == null ? c.getInputStream() : c.getErrorStream();

        return new HttpResponse(responseCode, inputStream, c.getContentLength());
    }

    private static void setHeaders(HttpURLConnection c, List<HttpHeader> headers) {
        headers.forEach(header -> c.setRequestProperty(header.getKey(), header.getValue()));
    }

    public String getUserAgent() {
        return this.userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
