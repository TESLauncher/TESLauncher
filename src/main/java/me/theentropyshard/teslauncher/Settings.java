/*
 * TESLauncher - https://github.com/TESLauncher/TESLauncher
 * Copyright (C) 2023-2024 TESLauncher
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
import me.theentropyshard.teslauncher.utils.json.Json;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * I don't usually like making fields public, but ok, those are settings
 */
public class Settings {
    private static final Logger LOG = LogManager.getLogger(Settings.class);

    public String language = "english";
    public boolean darkTheme = false;
    public String lastDir = System.getProperty("user.dir");
    public boolean useDownloadDialog = true;
    public String lastInstanceGroup = "<default>";
    public boolean dialogRelativeToParent = true;
    public boolean writePrettyJson = false;

    public Settings() {

    }

    public static Settings load(Path file) {
        if (!Files.exists(file)) {
            return new Settings();
        }

        try {
            return Json.parse(FileUtils.readUtf8(file), Settings.class);
        } catch (IOException e) {
            LOG.error("Could not load settings from {}, using defaults", file, e);
        }

        return new Settings();
    }

    public void save(Path file) {
        try {
            FileUtils.writeUtf8(file, Json.write(this));
        } catch (IOException e) {
            LOG.error("Could not save settings to {}", file);
        }
    }
}
