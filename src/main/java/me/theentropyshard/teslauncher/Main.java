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

public class Main {
    public static void main(String[] args) {
        new TESLauncher(args);

        /*
        String url = "https://piston-data.mojang.com/v1/objects/0c3ec587af28e5a785c0b4a7b8a30f9a8f78f838/client.jar";
        HttpClient client = new HttpClient();
        client.setUserAgent(Http.USER_AGENT);

        HttpRequest request = new HttpRequest();
        request.setMethod(HttpMethod.GET);
        request.setUrl(url);

        try (HttpResponse response = client.send(request)) {
            int contentLength = response.getContentLength();
            int megs = contentLength / 1024 / 1024;
            System.out.println("File size in MiB: " + megs);

            InputStream inputStream = response.getInputStream();

            byte[] buffer = new byte[4096];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int numRead;
            long count = 0;
            while ((numRead = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, numRead);
                count += numRead;
                System.out.println("Progress: " + (count / 1024 / 1024) + " / " + megs);
            }
            baos.flush();

            System.out.println("Downloaded file");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        */
    }
}
