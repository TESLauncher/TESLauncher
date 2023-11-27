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

package me.theentropyshard.teslauncher.instance.newi;

import me.theentropyshard.teslauncher.utils.IOUtils;
import me.theentropyshard.teslauncher.utils.Json;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Instance {
    private static final Map<String, Instance> instances = new HashMap<>();

    private String name;
    private String group;
    private String version;

    public Instance(String name, String group, String version) {
        this.name = name;
        this.group = group;
        this.version = version;
    }

    public static void save(Path file, Instance instance) throws IOException {
        IOUtils.writeUtf8String(file, Json.write(instance));
    }

    public static Instance load(Path file) throws IOException {
        Instance instance = Json.parse(IOUtils.readUtf8String(file), Instance.class);

        Instance.instances.put(instance.getName(), instance);

        return instance;
    }

    public static Instance create(Path file, String name, String group, String version) throws IOException {
        Instance instance = new Instance(name, group, version);

        IOUtils.writeUtf8String(file, Json.write(instance));
        Instance.instances.put(name, instance);

        return instance;
    }

    public static Instance get(String name) {
        return Instance.instances.get(name);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return this.group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
