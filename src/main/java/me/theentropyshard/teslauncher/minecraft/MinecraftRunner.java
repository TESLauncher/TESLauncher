/*
 *  Copyright 2023 TheEntropyShard (https://github.com/TheEntropyShard)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package me.theentropyshard.teslauncher.minecraft;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Path;

public class MinecraftRunner {
    private final Path clientsDir;
    private final Path assetsDir;
    private final Path librariesDir;
    private final Path nativesDir;
    private final Gson gson;

    public MinecraftRunner(Path clientsDir, Path assetsDir, Path librariesDir, Path nativesDir) {
        this.clientsDir = clientsDir;
        this.assetsDir = assetsDir;
        this.librariesDir = librariesDir;
        this.nativesDir = nativesDir;
        this.gson = new Gson();
    }

    public void runMinecraft(String versionId) throws IOException {
        Path mainJar = this.clientsDir.resolve(versionId).resolve(versionId + ".jar");
        Path jsonPath = this.clientsDir.resolve(versionId).resolve(versionId + ".json");

    }
}
