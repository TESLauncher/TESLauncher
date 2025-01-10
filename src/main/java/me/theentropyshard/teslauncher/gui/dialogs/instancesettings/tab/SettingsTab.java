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

package me.theentropyshard.teslauncher.gui.dialogs.instancesettings.tab;

import me.theentropyshard.teslauncher.minecraft.MinecraftInstance;

import javax.swing.*;
import java.io.IOException;

public abstract class SettingsTab {
    private final JDialog dialog;
    private final String name;
    private final MinecraftInstance instance;
    private final JPanel root;

    public SettingsTab(String name, MinecraftInstance instance, JDialog dialog) {
        this.name = name;
        this.instance = instance;
        this.dialog = dialog;
        this.root = new JPanel();
    }

    public abstract void save() throws IOException;

    public String getName() {
        return this.name;
    }

    public MinecraftInstance getInstance() {
        return this.instance;
    }

    public JDialog getDialog() {
        return this.dialog;
    }

    public JPanel getRoot() {
        return this.root;
    }
}
