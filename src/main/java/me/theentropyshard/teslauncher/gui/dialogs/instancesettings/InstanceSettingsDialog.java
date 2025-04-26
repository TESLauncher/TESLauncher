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

package me.theentropyshard.teslauncher.gui.dialogs.instancesettings;

import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.gui.components.InstanceItem;
import me.theentropyshard.teslauncher.gui.components.MyTabbedPane;
import me.theentropyshard.teslauncher.gui.dialogs.AppDialog;
import me.theentropyshard.teslauncher.gui.dialogs.instancesettings.tab.JarModsTab;
import me.theentropyshard.teslauncher.gui.dialogs.instancesettings.tab.JavaTab;
import me.theentropyshard.teslauncher.gui.dialogs.instancesettings.tab.SettingsTab;
import me.theentropyshard.teslauncher.gui.view.playview.InstancesPanel;
import me.theentropyshard.teslauncher.minecraft.MinecraftInstance;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InstanceSettingsDialog extends AppDialog {

    private final JTabbedPane tabbedPane;
    private final List<SettingsTab> tabs;

    public InstanceSettingsDialog(MinecraftInstance instance) {
        super(TESLauncher.frame, "Instance Settings - " + instance.getName());

        this.tabbedPane = new MyTabbedPane(JTabbedPane.LEFT);
        this.tabbedPane.setPreferredSize(new Dimension(900, 480));

        InputMap inputMap = this.tabbedPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESCAPE");

        ActionMap actionMap = this.tabbedPane.getActionMap();
        actionMap.put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                InstanceSettingsDialog.this.getDialog().dispose();
            }
        });

        this.tabs = new ArrayList<>();

        this.addTab(new JavaTab("Java", instance, this.getDialog()));
        this.addTab(new JarModsTab(instance, this.getDialog()));

        this.getDialog().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                InstanceSettingsDialog.this.tabs.forEach(tab -> {
                    try {
                        tab.save();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });

                InstancesPanel instancesPanel = TESLauncher.getInstance().getGui().getPlayView().getCurrentInstancesPanel();
                JPanel itemsPanel = instancesPanel.getInstancesPanel();
                for (Component component : itemsPanel.getComponents()) {
                    MinecraftInstance associatedInstance = ((InstanceItem) component).getAssociatedInstance();
                    if (associatedInstance == instance) {
                        ((InstanceItem) component).getTextLabel().setText(instance.getName());
                        try {
                            instance.save();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });

        this.setContent(this.tabbedPane);
        this.center(0);
        this.setVisible(true);
    }

    public void addTab(SettingsTab tab) {
        this.tabs.add(tab);
        this.tabbedPane.addTab(tab.getName(), tab.getRoot());
    }
}
