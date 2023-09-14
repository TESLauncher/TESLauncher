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

import java.util.ArrayList;
import java.util.List;

public class HttpRequest {
    /**
     * Request url
     */
    private String url;

    /**
     * Request method
     * @see HttpMethod
     */
    private HttpMethod method;

    /**
     * Additional headers that may override common headers in HttpClient
     */
    private List<HttpHeader> additionalHeaders;

    /**
     * Request payload. Must be non-null if the request method is POST
     */
    private byte[] payload;

    /**
     * Content type in POST request
     */
    private String contentType;

    public HttpRequest() {
        this.additionalHeaders = new ArrayList<>();
    }

    public void addHeader(HttpHeader header) {
        this.additionalHeaders.add(header);
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HttpMethod getMethod() {
        return this.method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public List<HttpHeader> getAdditionalHeaders() {
        return this.additionalHeaders;
    }

    public void setAdditionalHeaders(List<HttpHeader> additionalHeaders) {
        this.additionalHeaders = additionalHeaders;
    }

    public byte[] getPayload() {
        return this.payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
