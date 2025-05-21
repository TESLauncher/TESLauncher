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

package me.theentropyshard.teslauncher.gui.dialogs;

import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.minecraft.MinecraftInstance;
import me.theentropyshard.teslauncher.language.Language;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Set;
import java.util.Vector;

public class ChangeGroupDialog extends AppDialog {
    private String group;

    public ChangeGroupDialog(MinecraftInstance instance, Set<String> groups) {
        super(TESLauncher.frame, TESLauncher.getInstance().getLanguage().getString("gui.changeGroupDialog.title")
            .replace("$$INSTANCE_NAME$$", "«" + instance.getName() + "»"));

        JPanel root = new JPanel(new BorderLayout());

        InputMap inputMap = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESCAPE");

        ActionMap actionMap = root.getActionMap();
        actionMap.put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ChangeGroupDialog.this.getDialog().dispose();
            }
        });

        Language language = TESLauncher.getInstance().getLanguage();

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridy++;
        JLabel label = new JLabel(language.getString("gui.changeGroupDialog.label"));
        label.setBorder(new EmptyBorder(0, 0, 10, 0));
        centerPanel.add(label, gbc);

        JComboBox<String> comboBox = new JComboBox<>(new Vector<>(groups));
        comboBox.setSelectedItem(instance.getGroup());
        comboBox.addActionListener(e -> {
            this.group = String.valueOf(comboBox.getSelectedItem());
        });
        comboBox.setPreferredSize(new Dimension(250, comboBox.getPreferredSize().height));
        comboBox.setEditable(true);

        gbc.gridy++;
        centerPanel.add(comboBox, gbc);

        root.add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
            UIManager.getColor("Component.borderColor")));

        JButton okButton = new JButton(language.getString("gui.general.ok"));
        okButton.addActionListener(e -> {
            this.getDialog().dispose();
        });
        bottomPanel.add(okButton);

        root.add(bottomPanel, BorderLayout.SOUTH);

        this.getDialog().getRootPane().setDefaultButton(okButton);

        this.setContent(root);
        this.center(0);
        this.setVisible(true);
    }

    public String getGroup() {
        return this.group;
    }
}