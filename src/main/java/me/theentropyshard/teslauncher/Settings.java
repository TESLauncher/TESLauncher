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


import me.theentropyshard.teslauncher.utils.FileUtils;
import me.theentropyshard.teslauncher.utils.Json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * I don't usually like making fields public, but ok, those are settings
 */
public class Settings {
    public String language = "english";
    public boolean darkTheme = false;

    public Settings() {

    }

    public Settings load(Path file) throws IOException {
        if (!Files.exists(file)) {
            return this;
        }

        String content = FileUtils.readUtf8(file);
        return Json.parse(content, Settings.class);
    }

    public void save(Path file) throws IOException {
        String content = Json.write(this);
        FileUtils.writeUtf8(file, content);
    }
}
