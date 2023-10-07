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
