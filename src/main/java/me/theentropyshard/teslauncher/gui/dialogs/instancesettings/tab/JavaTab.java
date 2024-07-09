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

import com.formdev.flatlaf.FlatClientProperties;
import me.theentropyshard.teslauncher.instance.Instance;
import me.theentropyshard.teslauncher.gui.utils.MessageBox;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class JavaTab extends SettingsTab {
    public JavaTab(String name, Instance instance, JDialog dialog) {
        super(name, instance, dialog);

        JPanel root = this.getRoot();
        root.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;

        JPanel javaInstallation = new JPanel(new GridLayout(0, 1));
        JTextField javaPathTextField = new JTextField();
        javaPathTextField.setText(instance.getJavaPath());
        javaPathTextField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Path to java.exe");
        javaInstallation.add(javaPathTextField);
        javaInstallation.setBorder(new TitledBorder("Java Installation"));

        JPanel memorySettings = new JPanel(new GridLayout(2, 2));
        JLabel minMemoryLabel = new JLabel("Minimum memory (Megabytes):");
        JLabel maxMemoryLabel = new JLabel("Maximum memory (Megabytes):");
        JTextField minMemoryField = new JTextField();
        minMemoryField.setText(String.valueOf(instance.getMinimumMemoryInMegabytes()));
        minMemoryField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "512");
        JTextField maxMemoryField = new JTextField();
        maxMemoryField.setText(String.valueOf(instance.getMaximumMemoryInMegabytes()));
        maxMemoryField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "2048");
        memorySettings.add(minMemoryLabel);
        memorySettings.add(minMemoryField);
        memorySettings.add(maxMemoryLabel);
        memorySettings.add(maxMemoryField);
        memorySettings.setBorder(new TitledBorder("Memory Settings"));

        gbc.gridy++;
        root.add(javaInstallation, gbc);

        gbc.gridy++;
        root.add(memorySettings, gbc);

        JPanel otherSettings = new JPanel(new GridLayout(0, 1));
        otherSettings.setBorder(BorderFactory.createTitledBorder("Other"));

        JCheckBox useOptimizedArgs = new JCheckBox("Use optimized JVM arguments");
        useOptimizedArgs.setSelected(instance.isUseOptimizedArgs());
        useOptimizedArgs.addActionListener(e -> {
            instance.setUseOptimizedArgs(useOptimizedArgs.isSelected());
        });
        otherSettings.add(useOptimizedArgs);

        gbc.gridy++;
        gbc.weighty = 1;
        root.add(otherSettings, gbc);

        this.getDialog().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                instance.setJavaPath(javaPathTextField.getText());
                String minMemory = minMemoryField.getText();
                if (minMemory.isEmpty()) {
                    minMemory = "512";
                }

                String maxMemory = maxMemoryField.getText();
                if (maxMemory.isEmpty()) {
                    maxMemory = "2048";
                }

                int minimumMemoryInMegabytes = 512;
                try {
                    minimumMemoryInMegabytes = Integer.parseInt(minMemory);
                } catch (NumberFormatException ex) {
                    MessageBox.showErrorMessage(JavaTab.this.getDialog(),
                            "Too many megabytes! (" + ex.getMessage() + ")");
                }

                int maximumMemoryInMegabytes = 2048;
                try {
                    maximumMemoryInMegabytes = Integer.parseInt(maxMemory);
                } catch (NumberFormatException ex) {
                    MessageBox.showErrorMessage(JavaTab.this.getDialog(),
                            "Too many megabytes! (" + ex.getMessage() + ")");
                }

                if (minimumMemoryInMegabytes > maximumMemoryInMegabytes) {
                    MessageBox.showErrorMessage(JavaTab.this.getDialog(),
                            "Minimum amount of RAM cannot be larger than maximum");
                    return;
                }

                if (minimumMemoryInMegabytes < 512) {
                    MessageBox.showErrorMessage(
                            JavaTab.this.getDialog(),
                            "Minimum amount of RAM cannot be less than 512 MiB");
                    return;
                }

                instance.setJavaPath(javaPathTextField.getText());
                instance.setMinimumMemoryInMegabytes(minimumMemoryInMegabytes);
                instance.setMaximumMemoryInMegabytes(maximumMemoryInMegabytes);
            }
        });
    }

    @Override
    public void save() throws IOException {

    }
}
