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

package me.theentropyshard.teslauncher;

import me.theentropyshard.teslauncher.utils.IOUtils;
import me.theentropyshard.teslauncher.utils.Json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Settings {
    private String language = "english";

    public Settings() {

    }

    public Settings load(Path file) throws IOException {
        if (!Files.exists(file)) {
            return this;
        }

        String content = IOUtils.readUtf8String(file);
        return Json.parse(content, Settings.class);
    }

    public void save(Path file) throws IOException {
        String content = Json.write(this);
        IOUtils.writeUtf8String(file, content);
    }

    public String getLanguage() {
        return this.language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
