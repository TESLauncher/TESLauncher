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

package me.theentropyshard.teslauncher.gui.dialogs.instancesettings;

import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.gui.dialogs.AppDialog;
import me.theentropyshard.teslauncher.instance.Instance;

import javax.swing.*;

public class InstanceSettingsDialog extends AppDialog {
    public InstanceSettingsDialog(Instance instance) {
        super(TESLauncher.window.getFrame(), "Instance Settings - " + instance.getName());

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);

        tabbedPane.addTab("Main", new MainTab(instance, this.getDialog()).getRoot());
        tabbedPane.addTab("Java", new JavaTab(instance, this.getDialog()).getRoot());

        this.setContent(tabbedPane);
        this.center(0);
        this.setVisible(true);
    }
}
