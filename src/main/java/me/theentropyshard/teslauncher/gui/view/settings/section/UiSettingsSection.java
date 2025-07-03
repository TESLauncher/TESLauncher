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

import me.theentropyshard.teslauncher.Settings;
import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.language.Language;

public class UiSettingsSection extends SettingsSection {
    private final JComboBox<String> instanceProgressIndicationCombo;
    private final JLabel displayProgressLabel;
    private final JLabel dialogPositionLabel;
    private final JComboBox<String> dialogPositionCombo;
    private final JCheckBox showAmountOfTimeCheckbox;

    public UiSettingsSection() {
        super("UI", new GridLayout(3, 3));

        this.instanceProgressIndicationCombo = new JComboBox<>(new String[]{"Dialog", "Circle"});

        if (TESLauncher.getInstance().getSettings().useDownloadDialog) {
            this.instanceProgressIndicationCombo.setSelectedIndex(0);
        } else {
            this.instanceProgressIndicationCombo.setSelectedIndex(1);
        }

        this.instanceProgressIndicationCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                Settings settings = TESLauncher.getInstance().getSettings();

                switch (e.getItem().toString()) {
                    case "Dialog" -> settings.useDownloadDialog = true;
                    case "Circle" -> settings.useDownloadDialog = false;
                    default -> throw new RuntimeException("Unreachable");
                }
            }
        });

        this.displayProgressLabel = new JLabel("Display progress: ");

        this.add(this.displayProgressLabel);
        this.add(this.instanceProgressIndicationCombo);

        this.dialogPositionLabel = new JLabel("Dialog position: ");
        this.add(this.dialogPositionLabel);

        this.dialogPositionCombo = new JComboBox<>(new String[]{"Relative to parent", "Always centered"});

        if (TESLauncher.getInstance().getSettings().dialogRelativeToParent) {
            this.dialogPositionCombo.setSelectedIndex(0);
        } else {
            this.dialogPositionCombo.setSelectedIndex(1);
        }

        this.dialogPositionCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                Settings settings = TESLauncher.getInstance().getSettings();

                switch (e.getItem().toString()) {
                    case "Relative to parent" -> settings.dialogRelativeToParent = true;
                    case "Always centered" -> settings.dialogRelativeToParent = false;
                    default -> throw new RuntimeException("Unreachable");
                }
            }
        });

        this.add(this.dialogPositionCombo);

        this.showAmountOfTimeCheckbox = new JCheckBox("Show the amount of time that has passed since the release date");
        this.showAmountOfTimeCheckbox.setSelected(TESLauncher.getInstance().getSettings().showAmountOfTime);
        this.showAmountOfTimeCheckbox.addActionListener(e -> {
            TESLauncher.getInstance().getSettings().showAmountOfTime = this.showAmountOfTimeCheckbox.isSelected();
        });
        this.add(this.showAmountOfTimeCheckbox);
    }

    @Override
    public void updateLanguage(Language language) {

    }
}
