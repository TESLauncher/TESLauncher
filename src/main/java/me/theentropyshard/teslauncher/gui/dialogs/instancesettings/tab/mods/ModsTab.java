/*
 * TESLauncher - https://github.com/TESLauncher/TESLauncher
 * Copyright (C) 2023-2025 TESLauncher
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

package me.theentropyshard.teslauncher.gui.dialogs.instancesettings.tab.mods;

import javax.swing.*;
import java.io.IOException;

import me.theentropyshard.teslauncher.gui.dialogs.instancesettings.tab.SettingsTab;
import me.theentropyshard.teslauncher.minecraft.MinecraftInstance;

public class ModsTab extends SettingsTab {
    public ModsTab(MinecraftInstance instance, JDialog dialog) {
        super("Mods", instance, dialog);


    }

    @Override
    public void save() throws IOException {

    }
}
