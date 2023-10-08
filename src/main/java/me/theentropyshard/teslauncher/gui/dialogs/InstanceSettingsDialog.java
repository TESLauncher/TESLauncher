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

package me.theentropyshard.teslauncher.gui.dialogs;

import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.instance.Instance;
import me.theentropyshard.teslauncher.instance.InstanceManager;

public class InstanceSettingsDialog extends AppDialog {
    public InstanceSettingsDialog(String instanceName) {
        super(TESLauncher.window.getFrame(), "Instance Settings - " + instanceName);

        InstanceManager instanceManager = TESLauncher.getInstance().getInstanceManager();
        Instance instance = instanceManager.getInstanceByName(instanceName);

        if (instance == null) {
            throw new RuntimeException("Instance with name '" + instanceName + "' does not exist");
        }


    }
}
