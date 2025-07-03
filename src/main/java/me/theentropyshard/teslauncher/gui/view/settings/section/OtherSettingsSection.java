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

package me.theentropyshard.teslauncher.gui.view.settings.section;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.language.Language;

public class OtherSettingsSection extends SettingsSection {
    private final JCheckBox prettyJson;
    private final JLabel whenMinecraftLaunchesLabel;
    private final JComboBox<String> whenLaunchesBehavior;
    private final JLabel whenMinecraftExits;
    private final JComboBox<String> whenExitsBehavior;

    public OtherSettingsSection() {
        super("Other", new GridLayout(3, 3));

        this.prettyJson = new JCheckBox("Write pretty JSON files (useful for development/debugging)");
        this.prettyJson.setSelected(TESLauncher.getInstance().getSettings().writePrettyJson);
        this.prettyJson.addActionListener(e -> {
            TESLauncher.getInstance().getSettings().writePrettyJson = this.prettyJson.isSelected();
        });
        this.add(this.prettyJson);
        this.add(Box.createHorizontalGlue());

        this.whenMinecraftLaunchesLabel = new JLabel("When Minecraft launches: ");
        this.add(this.whenMinecraftLaunchesLabel);
        String[] whenLaunchesOptions = {
            "Do nothing",
            "Hide launcher",
            "Hide launcher and console",
            "Exit launcher (Time spent on instance won't be counted)"
        };
        this.whenLaunchesBehavior = new JComboBox<>(whenLaunchesOptions);
        this.whenLaunchesBehavior.addItemListener(e -> {
            if (e.getStateChange() != ItemEvent.SELECTED) {
                return;
            }

            TESLauncher.getInstance().getSettings().whenMCLaunchesOption = this.whenLaunchesBehavior.getSelectedIndex();
        });
        int whenLaunchesIndex = TESLauncher.getInstance().getSettings().whenMCLaunchesOption;
        if (whenLaunchesIndex < 0 || whenLaunchesIndex >= whenLaunchesOptions.length) {
            whenLaunchesIndex = 0;
        }
        this.whenLaunchesBehavior.setSelectedIndex(whenLaunchesIndex);
        this.add(this.whenLaunchesBehavior);

        this.whenMinecraftExits = new JLabel("When Minecraft exits: ");
        this.add(this.whenMinecraftExits);
        String[] whenExitsOptions = {
            "Do nothing",
            "Exit launcher if Minecraft exit code is 0 (ok)"
        };
        this.whenExitsBehavior = new JComboBox<>(whenExitsOptions);
        this.whenExitsBehavior.addItemListener(e -> {
            TESLauncher.getInstance().getSettings().whenMCExitsOption = this.whenExitsBehavior.getSelectedIndex();
        });
        int whenExitsIndex = TESLauncher.getInstance().getSettings().whenMCExitsOption;
        if (whenExitsIndex < 0 || whenExitsIndex >= whenExitsOptions.length) {
            whenExitsIndex = 0;
        }
        this.whenExitsBehavior.setSelectedIndex(whenExitsIndex);
        this.add(this.whenExitsBehavior);
    }

    @Override
    public void updateLanguage(Language language) {

    }
}
