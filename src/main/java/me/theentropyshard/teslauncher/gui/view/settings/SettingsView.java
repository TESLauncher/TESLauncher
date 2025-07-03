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

package me.theentropyshard.teslauncher.gui.view.settings;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.gui.view.settings.section.OtherSettingsSection;
import me.theentropyshard.teslauncher.gui.view.settings.section.SettingsSection;
import me.theentropyshard.teslauncher.gui.view.settings.section.ThemeSettingsSection;
import me.theentropyshard.teslauncher.gui.view.settings.section.UiSettingsSection;
import me.theentropyshard.teslauncher.language.Language;

public class SettingsView extends JPanel {
    private final List<SettingsSection> sections;

    public SettingsView() {
        super(new GridBagLayout());

        this.sections = new ArrayList<>();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;

        this.addSection(new ThemeSettingsSection());
        this.addSection(new UiSettingsSection());
        this.addSection(new OtherSettingsSection());

        this.finish(gbc);
    }

    private void addSection(SettingsSection section) {
        this.sections.add(section);
    }

    private void finish(GridBagConstraints gbc) {
        int size = this.sections.size();

        for (int i = 0; i < size - 1; i++) {
            gbc.gridy++;
            this.add(this.sections.get(i), gbc);
        }

        gbc.gridy++;
        gbc.weighty = 1;
        this.add(this.sections.get(size - 1), gbc);
    }

    public void reloadLanguage() {
        Language language = TESLauncher.getInstance().getLanguage();

        for (SettingsSection section : this.sections) {
            section.updateLanguage(language);
        }
    }
}
