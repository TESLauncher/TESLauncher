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

package me.theentropyshard.teslauncher.gui.dialogs.instanceSettings.tab.java;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.ui.FlatScrollPaneBorder;

import me.theentropyshard.teslauncher.gui.dialogs.instanceSettings.tab.SettingsTab;
import me.theentropyshard.teslauncher.gui.utils.IntegerDocumentFilter;
import me.theentropyshard.teslauncher.gui.utils.MessageBox;
import me.theentropyshard.teslauncher.minecraft.MinecraftInstance;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class JavaTab extends SettingsTab {
    private final JTextField javaPathTextField;
    private final JTextField minMemoryField;
    private final JTextField maxMemoryField;
    private final JTextField javaAgentPathTextField;
    private final JTextArea flagsArea;

    public JavaTab(MinecraftInstance instance, JDialog dialog) {
        super("Java", instance, dialog);

        JPanel root = this.getRoot();
        root.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;

        JPanel javaInstallation = new JPanel(new GridLayout(0, 1));
        this.javaPathTextField = new JTextField();
        this.javaPathTextField.setText(instance.getJavaPath());
        this.javaPathTextField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Path to java.exe");
        javaInstallation.add(this.javaPathTextField);
        javaInstallation.setBorder(new TitledBorder("Java Installation"));

        JPanel memorySettings = new JPanel(new GridLayout(2, 2));
        JLabel minMemoryLabel = new JLabel("Minimum memory (Megabytes):");
        JLabel maxMemoryLabel = new JLabel("Maximum memory (Megabytes):");
        this.minMemoryField = new JTextField();
        int minimumMemoryMegabytes = instance.getMinimumMemoryMegabytes();
        if (minimumMemoryMegabytes == 0) {
            minimumMemoryMegabytes = 512;
        }
        this.minMemoryField.setText(String.valueOf(minimumMemoryMegabytes));
        this.minMemoryField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "512");
        ((PlainDocument) this.minMemoryField.getDocument()).setDocumentFilter(new IntegerDocumentFilter((s) -> {
            MessageBox.showErrorMessage(JavaTab.this.getDialog(), "Not an integer: " + s);
        }));
        this.maxMemoryField = new JTextField();
        int maximumMemoryMegabytes = instance.getMaximumMemoryMegabytes();
        if (maximumMemoryMegabytes == 0) {
            maximumMemoryMegabytes = 2048;
        }
        this.maxMemoryField.setText(String.valueOf(maximumMemoryMegabytes));
        this.maxMemoryField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "2048");
        ((PlainDocument) this.maxMemoryField.getDocument()).setDocumentFilter(new IntegerDocumentFilter((s) -> {
            MessageBox.showErrorMessage(JavaTab.this.getDialog(), "Not an integer: " + s);
        }));
        memorySettings.add(minMemoryLabel);
        memorySettings.add(this.minMemoryField);
        memorySettings.add(maxMemoryLabel);
        memorySettings.add(this.maxMemoryField);
        memorySettings.setBorder(new TitledBorder("Memory Settings"));

        gbc.gridy++;
        root.add(javaInstallation, gbc);

        gbc.gridy++;
        root.add(memorySettings, gbc);

        JPanel javaAgentSettings = new JPanel(new GridLayout(1, 1));
        javaAgentSettings.setBorder(BorderFactory.createTitledBorder("Java Agent Path"));

        JPanel javaAgentBorderPanel = new JPanel(new BorderLayout());
        javaAgentSettings.add(javaAgentBorderPanel);

        this.javaAgentPathTextField = new JTextField();
        javaAgentBorderPanel.add(this.javaAgentPathTextField, BorderLayout.CENTER);
        javaAgentBorderPanel.add(new JButton("Browse..."), BorderLayout.EAST);

        gbc.gridy++;
        root.add(javaAgentSettings, gbc);

        JPanel otherSettings = new JPanel(new GridLayout(0, 1));
        otherSettings.setBorder(BorderFactory.createTitledBorder("JVM Arguments"));

        this.flagsArea = new JTextArea();
        this.flagsArea.setLineWrap(true);
        this.flagsArea.setWrapStyleWord(true);
        this.flagsArea.setPreferredSize(new Dimension(0, 250));
        this.flagsArea.setMaximumSize(new Dimension(1000, 250));

        Set<String> customJvmFlags = instance.getCustomJvmFlags();
        if (customJvmFlags != null && !customJvmFlags.isEmpty()) {
            this.flagsArea.setText(String.join(" ", customJvmFlags));
        }

        JScrollPane scrollPane = new JScrollPane(
            this.flagsArea,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        scrollPane.setBorder(new FlatScrollPaneBorder());
        otherSettings.add(scrollPane);

        gbc.gridy++;
        gbc.weighty = 1;
        root.add(otherSettings, gbc);

        this.getDialog().setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.getDialog().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                String minMemory = JavaTab.this.minMemoryField.getText();
                if (minMemory.isEmpty()) {
                    minMemory = "512";
                }

                String maxMemory = JavaTab.this.maxMemoryField.getText();
                if (maxMemory.isEmpty()) {
                    maxMemory = "2048";
                }

                int minimumMemoryInMegabytes = Integer.parseInt(minMemory);
                int maximumMemoryInMegabytes = Integer.parseInt(maxMemory);

                if (minimumMemoryInMegabytes > maximumMemoryInMegabytes) {
                    MessageBox.showErrorMessage(JavaTab.this.getDialog(),
                        "Minimum amount of RAM cannot be larger than maximum");
                    return;
                }

                if (minimumMemoryInMegabytes < 512) {
                    MessageBox.showErrorMessage(JavaTab.this.getDialog(),
                        "Minimum amount of RAM cannot be less than 512 MiB");
                    return;
                }

                JavaTab.this.getDialog().dispose();
            }
        });
    }

    @Override
    public void save() throws IOException {
        MinecraftInstance instance = this.getInstance();

        instance.setJavaPath(this.javaPathTextField.getText());
        instance.setJavaAgentPath(this.javaAgentPathTextField.getText());
        instance.setCustomJvmFlags(new LinkedHashSet<>(Arrays.asList(this.flagsArea.getText().split("\\s"))));
        instance.setMinimumMemoryMegabytes(Integer.parseInt(this.minMemoryField.getText()));
        instance.setMaximumMemoryMegabytes(Integer.parseInt(this.maxMemoryField.getText()));

        instance.save();
    }
}
