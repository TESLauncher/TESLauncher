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

package me.theentropyshard.teslauncher.gui.view;

import me.theentropyshard.teslauncher.Settings;
import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.gui.Gui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class SettingsView extends JPanel {
    public SettingsView() {
        this.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;

        {
            JPanel themeSettings = new JPanel(new GridLayout(0, 1));
            themeSettings.setBorder(new TitledBorder("Theme"));
            JRadioButton darkThemeButton = new JRadioButton("Dark");
            darkThemeButton.addActionListener(e -> {
                Gui gui = TESLauncher.getInstance().getGui();
                gui.setDarkTheme(true);
                gui.updateLookAndFeel();
                TESLauncher.getInstance().getSettings().darkTheme = true;
            });
            JRadioButton lightThemeButton = new JRadioButton("Light");
            lightThemeButton.addActionListener(e -> {
                Gui gui = TESLauncher.getInstance().getGui();
                gui.setDarkTheme(false);
                gui.updateLookAndFeel();
                TESLauncher.getInstance().getSettings().darkTheme = false;
            });
            themeSettings.add(darkThemeButton);
            themeSettings.add(lightThemeButton);
            Settings settings = TESLauncher.getInstance().getSettings();
            ButtonGroup buttonGroup = new ButtonGroup();
            buttonGroup.add(darkThemeButton);
            buttonGroup.add(lightThemeButton);
            darkThemeButton.setSelected(settings.darkTheme);
            lightThemeButton.setSelected(!settings.darkTheme);


            gbc.gridy++;
            this.add(themeSettings, gbc);
        }

        {
            JPanel uiSettings = new JPanel(new GridLayout(3, 3));
            uiSettings.setBorder(new TitledBorder("UI"));

            JComboBox<String> options = new JComboBox<>(new String[]{"Dialog", "Circle"});
            if (TESLauncher.getInstance().getSettings().useDownloadDialog) {
                options.setSelectedIndex(0);
            } else {
                options.setSelectedIndex(1);
            }

            options.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    switch (e.getItem().toString()) {
                        case "Dialog":
                            TESLauncher.getInstance().getSettings().useDownloadDialog = true;
                            break;
                        case "Circle":
                            TESLauncher.getInstance().getSettings().useDownloadDialog = false;
                            break;
                        default:
                            throw new RuntimeException("Unreachable");
                    }
                }
            });

            JLabel label = new JLabel("Display progress: ");

            uiSettings.add(label);
            uiSettings.add(options);

            JLabel dialogPosition = new JLabel("Dialog position: ");

            JComboBox<String> position = new JComboBox<>(new String[]{"Relative to parent", "Always centered"});
            if (TESLauncher.getInstance().getSettings().dialogRelativeToParent) {
                position.setSelectedIndex(0);
            } else {
                position.setSelectedIndex(1);
            }

            position.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    switch (e.getItem().toString()) {
                        case "Relative to parent":
                            TESLauncher.getInstance().getSettings().dialogRelativeToParent = true;
                            break;
                        case "Always centered":
                            TESLauncher.getInstance().getSettings().dialogRelativeToParent = false;
                            break;
                        default:
                            throw new RuntimeException("Unreachable");
                    }
                }
            });

            uiSettings.add(dialogPosition);
            uiSettings.add(position);

            JCheckBox showAmountOfTime = new JCheckBox("Show the amount of time that has passed since the release date");
            showAmountOfTime.addActionListener(e -> {
                TESLauncher.getInstance().getSettings().showAmountOfTime = showAmountOfTime.isSelected();
            });
            showAmountOfTime.setSelected(TESLauncher.getInstance().getSettings().showAmountOfTime);
            uiSettings.add(showAmountOfTime);

            gbc.gridy++;
            this.add(uiSettings, gbc);
        }

        {
            JPanel otherSettings = new JPanel(new GridLayout(3, 3));
            otherSettings.setBorder(new TitledBorder("Other"));

            JCheckBox prettyJson = new JCheckBox("Write pretty JSON files (useful for development/debugging)");
            prettyJson.addActionListener(e -> {
                TESLauncher.getInstance().getSettings().writePrettyJson = prettyJson.isSelected();
            });
            prettyJson.setSelected(TESLauncher.getInstance().getSettings().writePrettyJson);
            otherSettings.add(prettyJson);
            otherSettings.add(Box.createHorizontalGlue());

            JLabel whenMinecraftLaunchesLabel = new JLabel("When Minecraft launches: ");
            otherSettings.add(whenMinecraftLaunchesLabel);
            String[] whenLaunchesOptions = {
                "Do nothing",
                "Hide launcher",
                "Hide launcher and console",
                "Exit launcher (Time spent on instance won't be counted)"
            };
            JComboBox<String> whenLaunchesBehavior = new JComboBox<>(whenLaunchesOptions);
            whenLaunchesBehavior.addItemListener(e -> {
                if (e.getStateChange() != ItemEvent.SELECTED) {
                    return;
                }

                TESLauncher.getInstance().getSettings().whenMCLaunchesOption = whenLaunchesBehavior.getSelectedIndex();
            });
            int whenLaunchesIndex = TESLauncher.getInstance().getSettings().whenMCLaunchesOption;
            if (whenLaunchesIndex < 0 || whenLaunchesIndex >= whenLaunchesOptions.length) {
                whenLaunchesIndex = 0;
            }
            whenLaunchesBehavior.setSelectedIndex(whenLaunchesIndex);
            otherSettings.add(whenLaunchesBehavior);

            JLabel whenMinecraftExits = new JLabel("When Minecraft exits: ");
            otherSettings.add(whenMinecraftExits);
            String[] whenExitsOptions = {
                "Do nothing",
                "Exit launcher if Minecraft exit code is 0 (ok)"
            };
            JComboBox<String> whenExitsBehavior = new JComboBox<>(whenExitsOptions);
            whenExitsBehavior.addItemListener(e -> {
                TESLauncher.getInstance().getSettings().whenMCExitsOption = whenExitsBehavior.getSelectedIndex();
            });
            int whenExitsIndex = TESLauncher.getInstance().getSettings().whenMCExitsOption;
            if (whenExitsIndex < 0 || whenExitsIndex >= whenExitsOptions.length) {
                whenExitsIndex = 0;
            }
            whenExitsBehavior.setSelectedIndex(whenExitsIndex);
            otherSettings.add(whenExitsBehavior);

            gbc.gridy++;
            gbc.weighty = 1;
            this.add(otherSettings, gbc);
        }
    }
}
