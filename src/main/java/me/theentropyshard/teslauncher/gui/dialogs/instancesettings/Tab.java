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

package me.theentropyshard.teslauncher.gui.dialogs.instancesettings;

import me.theentropyshard.teslauncher.instance.Instance;

import javax.swing.*;
import java.io.IOException;

public abstract class Tab {
    private final JDialog dialog;
    private final String name;
    private final Instance instance;
    private final JPanel root;

    public Tab(String name, Instance instance, JDialog dialog) {
        this.name = name;
        this.instance = instance;
        this.dialog = dialog;
        this.root = new JPanel();
    }

    public abstract void save() throws IOException;

    public String getName() {
        return this.name;
    }

    public Instance getInstance() {
        return this.instance;
    }

    public JDialog getDialog() {
        return this.dialog;
    }

    public JPanel getRoot() {
        return this.root;
    }
}
