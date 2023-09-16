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

package me.theentropyshard.teslauncher.gson;

import com.google.gson.*;
import me.theentropyshard.teslauncher.version.model.VersionInfo;
import me.theentropyshard.teslauncher.version.model.VersionManifest;

import java.util.ArrayList;
import java.util.List;

public class VersionManifestDeserializer extends AbstractJsonDeserializer<VersionManifest> {
    private final Gson gson;

    public VersionManifestDeserializer() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(VersionInfo.class, new VersionInfoDeserializer())
                .create();
    }

    @Override
    public VersionManifest deserialize(JsonObject root) {
        JsonObject latestObject = root.getAsJsonObject("latest");

        List<VersionInfo> versions = new ArrayList<>();
        JsonArray versionsArray = root.getAsJsonArray("versions");
        for (JsonElement element : versionsArray) {
            versions.add(this.gson.fromJson(element, VersionInfo.class));
        }

        return new VersionManifest(
                latestObject.get("release").getAsString(),
                latestObject.get("snapshot").getAsString(),
                versions.toArray(new VersionInfo[0])
        );
    }
}
