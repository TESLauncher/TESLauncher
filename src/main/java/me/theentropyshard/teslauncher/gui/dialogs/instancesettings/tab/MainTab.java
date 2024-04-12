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

package me.theentropyshard.teslauncher.gui.dialogs.instancesettings.tab;

import me.theentropyshard.teslauncher.instance.Instance;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.IOException;

public class MainTab extends SettingsTab {

    private final JTextField nameField;
    private final JTextField groupField;

    public MainTab(String name, Instance instance, JDialog dialog) {
        super(name, instance, dialog);

        JPanel root = this.getRoot();
        root.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;

        JPanel common = new JPanel(new GridLayout(2, 2));

        JLabel nameLabel = new JLabel("Name:");
        common.add(nameLabel);

        this.nameField = new JTextField();
        this.nameField.setText(instance.getName());
        common.add(this.nameField);

        JLabel groupLabel = new JLabel("Group:");
        common.add(groupLabel);

        this.groupField = new JTextField();
        this.groupField.setText(instance.getGroupName());
        common.add(this.groupField);

        common.setBorder(new TitledBorder("Common"));

        gbc.gridy++;
        gbc.weighty = 1;
        root.add(common, gbc);
    }

    @Override
    public void save() throws IOException {
        Instance instance = this.getInstance();
        instance.setName(this.nameField.getText());
        instance.setGroupName(this.groupField.getText());
    }
}
