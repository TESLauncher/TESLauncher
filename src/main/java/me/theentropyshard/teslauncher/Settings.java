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


import me.theentropyshard.teslauncher.logging.Log;
import me.theentropyshard.teslauncher.utils.FileUtils;
import me.theentropyshard.teslauncher.utils.json.Json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * I don't usually like making fields public, but ok, those are settings
 */
public class Settings {
    public String language = "english";
    public boolean darkTheme = false;
    public String lastDir = System.getProperty("user.dir");
    public boolean useDownloadDialog = true;
    public String lastInstanceGroup = "<default>";
    public boolean dialogRelativeToParent = true;
    public boolean writePrettyJson = false;
    public boolean showAmountOfTime = false;

    public Settings() {

    }

    public static Settings load(Path file) {
        if (!Files.exists(file)) {
            return new Settings();
        }

        try {
            return Json.parse(FileUtils.readUtf8(file), Settings.class);
        } catch (IOException e) {
            Log.stackTrace("Could not load settings from" + file + ", using defaults", e);
        }

        return new Settings();
    }

    public void save(Path file) {
        try {
            FileUtils.writeUtf8(file, this.writePrettyJson ? Json.writePretty(this) : Json.write(this));
        } catch (IOException e) {
            Log.stackTrace("Could not save settings to " + file, e);
        }
    }
}
