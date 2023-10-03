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

import com.google.gson.JsonObject;
import me.theentropyshard.teslauncher.version.VersionType;
import me.theentropyshard.teslauncher.version.model.VersionInfo;

import java.time.OffsetDateTime;

public class VersionInfoDeserializer extends AbstractJsonDeserializer<VersionInfo> {
    public VersionInfoDeserializer() {

    }

    @Override
    public VersionInfo deserialize(JsonObject root) {
        return new VersionInfo(
                root.get("id").getAsString(),
                VersionType.byJsonName(root.get("type").getAsString()),
                OffsetDateTime.parse(root.get("releaseTime").getAsString()),
                root.get("url").getAsString(),
                root.get("sha1").getAsString(),
                root.get("complianceLevel").getAsInt() == 1
        );
    }
}
