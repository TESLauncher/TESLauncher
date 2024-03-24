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

package me.theentropyshard.teslauncher.gui.playview;

import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.accounts.AccountsManager;
import me.theentropyshard.teslauncher.gui.View;
import me.theentropyshard.teslauncher.gui.components.AddInstanceItem;
import me.theentropyshard.teslauncher.gui.components.InstanceItem;
import me.theentropyshard.teslauncher.gui.dialogs.addinstance.AddInstanceDialog;
import me.theentropyshard.teslauncher.gui.dialogs.instancesettings.InstanceSettingsDialog;
import me.theentropyshard.teslauncher.instance.Instance;
import me.theentropyshard.teslauncher.instance.InstanceManager;
import me.theentropyshard.teslauncher.instance.InstanceRunner;
import me.theentropyshard.teslauncher.utils.SwingUtils;
import me.theentropyshard.teslauncher.utils.TimeUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class PlayView extends View {
    public static final String DEFAULT_GROUP_NAME = "<default>";

    private final PlayViewHeader header;
    private final JPanel instancesPanelView;
    private final InstancesPanel defaultInstancesPanel;
    private final Map<String, InstancesPanel> groups;
    private final CardLayout cardLayout;
    private final DefaultComboBoxModel<String> model;
    private final JLabel instanceInfoLabel;

    private InstancesPanel currentPanel;

    public PlayView() {
        JPanel root = this.getRoot();

        this.groups = new HashMap<>();
        this.cardLayout = new CardLayout();
        this.instancesPanelView = new JPanel(this.cardLayout);

        this.header = new PlayViewHeader();
        root.add(this.header.getRoot(), BorderLayout.NORTH);

        AddInstanceItem defaultItem = new AddInstanceItem();
        defaultItem.addListener(e -> {
            new AddInstanceDialog(this, PlayView.DEFAULT_GROUP_NAME);
        }, true);
        this.defaultInstancesPanel = new InstancesPanel(defaultItem);
        this.currentPanel = this.defaultInstancesPanel;
        this.groups.put(PlayView.DEFAULT_GROUP_NAME, this.defaultInstancesPanel);
        root.add(this.instancesPanelView, BorderLayout.CENTER);

        this.groups.forEach((name, panel) -> {
            this.instancesPanelView.add(panel.getRoot(), name);
        });

        JComboBox<String> instanceGroups = this.header.getInstanceGroups();
        String[] items = {PlayView.DEFAULT_GROUP_NAME};
        this.model = new DefaultComboBoxModel<>(items);

        instanceGroups.setModel(this.model);
        instanceGroups.addItemListener(e -> {
            int stateChange = e.getStateChange();
            if (stateChange == ItemEvent.SELECTED) {
                Object[] selectedObjects = e.getItemSelectable().getSelectedObjects();
                String groupName = String.valueOf(selectedObjects[0]);
                this.cardLayout.show(this.instancesPanelView, groupName);
                this.currentPanel = this.groups.get(groupName);
            }
        });

        this.instanceInfoLabel = new JLabel();
        this.instanceInfoLabel.setVisible(false);

        root.add(this.instanceInfoLabel, BorderLayout.SOUTH);

        new SwingWorker<List<Instance>, Void>() {
            @Override
            protected List<Instance> doInBackground() {
                InstanceManager instanceManager = TESLauncher.getInstance().getInstanceManager();

                List<Instance> instances = instanceManager.getInstances();
                instances.sort((instance1, instance2) -> {
                    LocalDateTime lastTimePlayed1 = instance1.getLastTimePlayed();
                    LocalDateTime lastTimePlayed2 = instance2.getLastTimePlayed();
                    return lastTimePlayed2.compareTo(lastTimePlayed1);
                });

                return instances;
            }

            @Override
            protected void done() {
                try {
                    List<Instance> instances = this.get();

                    for (Instance instance : instances) {
                        Icon icon = SwingUtils.getIcon("/grass_icon.png");
                        InstanceItem item = new InstanceItem(icon, instance.getName());
                        PlayView.this.addInstanceItem(item, instance.getGroupName());
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    public InstancesPanel getCurrentInstancesPanel() {
        return this.currentPanel;
    }

    public void addInstanceItem(InstanceItem item, String groupName) {
        if (item instanceof AddInstanceItem) {
            throw new IllegalArgumentException("Adding AddInstanceItem is not allowed");
        }

        InstancesPanel panel = this.groups.get(groupName);
        if (panel == null) {
            AddInstanceItem addInstanceItem = new AddInstanceItem();
            addInstanceItem.addListener(e -> {
                new AddInstanceDialog(this, groupName);
            }, true);

            panel = new InstancesPanel(addInstanceItem);
            this.groups.put(groupName, panel);
            this.model.addElement(groupName);
            this.instancesPanelView.add(panel.getRoot(), groupName);
        }
        panel.addInstanceItem(item);

        item.addListener(e -> {
            int mouseButton = Integer.parseInt(e.getActionCommand());
            if (mouseButton == MouseEvent.BUTTON1) { // left mouse button
                if (AccountsManager.getCurrentAccount() == null) {
                    JOptionPane.showMessageDialog(
                            TESLauncher.window.getFrame(),
                            "No account selected",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                } else {
                    new InstanceRunner(AccountsManager.getCurrentAccount(), item).start();
                }
            } else if (mouseButton == MouseEvent.BUTTON3) { // right mouse button
                new InstanceSettingsDialog(item.getAssociatedInstance());
            }
        }, true);

        item.addMouseEnteredListener(e -> {
            this.instanceInfoLabel.setVisible(true);

            Instance instance = item.getAssociatedInstance();

            String lastPlayedTime = TimeUtils.getHoursMinutesSeconds(instance.getLastPlaytime());
            String totalPlayedTime = TimeUtils.getHoursMinutesSeconds(instance.getTotalPlaytime());

            String timeString = "";

            if (!lastPlayedTime.isEmpty()) {
                timeString = " - Last played for " + lastPlayedTime;
            }

            if (!totalPlayedTime.isEmpty()) {
                if (lastPlayedTime.isEmpty()) {
                    timeString = " - Total played for " + totalPlayedTime;
                } else {
                    timeString = timeString + ", Total played for " + totalPlayedTime;
                }
            }

            this.instanceInfoLabel.setText(instance.getName() + timeString);
        });

        item.addMouseExitedListener(e -> {
            this.instanceInfoLabel.setVisible(false);
            this.instanceInfoLabel.setText("");
        });
    }

    public PlayViewHeader getHeader() {
        return this.header;
    }

    public Map<String, InstancesPanel> getGroups() {
        return this.groups;
    }

    public InstancesPanel getDefaultInstancesPanel() {
        return this.defaultInstancesPanel;
    }
}
