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

package me.theentropyshard.teslauncher.instance;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface InstanceManager {
    void load() throws IOException;

    void reload() throws IOException;

    void save(Instance instance) throws IOException;

    void createInstance(String name, String groupName, String minecraftVersion) throws IOException;

    void removeInstance(String name) throws IOException;

    boolean instanceExists(String name);

    Path getInstanceDir(Instance instance);

    Path getMinecraftDir(Instance instance);

    Instance getInstanceByName(String name);

    List<Instance> getInstances();
}
