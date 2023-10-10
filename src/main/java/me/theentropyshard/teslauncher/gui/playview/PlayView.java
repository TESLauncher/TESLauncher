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

import com.formdev.flatlaf.FlatClientProperties;
import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.gui.View;
import me.theentropyshard.teslauncher.gui.components.AddInstanceItem;
import me.theentropyshard.teslauncher.gui.components.InstanceItem;
import me.theentropyshard.teslauncher.gui.dialogs.AddInstanceDialog;
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
    private final JProgressBar progressBar;
    private final JLabel instanceInfoLabel;

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
            }
        });

        this.progressBar = new JProgressBar(JProgressBar.HORIZONTAL);
        this.progressBar.putClientProperty(FlatClientProperties.PROGRESS_BAR_SQUARE, true);
        this.progressBar.setFont(this.progressBar.getFont().deriveFont(12.0f));
        this.progressBar.setStringPainted(true);
        this.progressBar.setVisible(false);

        this.instanceInfoLabel = new JLabel();
        this.instanceInfoLabel.setVisible(false);

        JPanel bottomPanel = new JPanel(new GridLayout(2, 1, 0, 0));
        bottomPanel.add(this.progressBar);
        bottomPanel.add(this.instanceInfoLabel);

        root.add(bottomPanel, BorderLayout.SOUTH);

        new SwingWorker<List<Instance>, Void>() {
            @Override
            protected List<Instance> doInBackground() throws Exception {
                InstanceManager instanceManager = TESLauncher.getInstance().getInstanceManager();
                instanceManager.reload();
                return instanceManager.getInstances();
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
                new InstanceRunner(item.getAssociatedInstance()).start();
            } else if (mouseButton == MouseEvent.BUTTON3) { // right mouse button
                new InstanceSettingsDialog(item.getAssociatedInstance());
            }
        }, true);

        item.addMouseEnteredListener(e -> {
            this.instanceInfoLabel.setVisible(true);

            Instance instance = item.getAssociatedInstance();

            String lastPlayedTime = TimeUtils.getHoursMinutesSeconds(instance.getLastPlayedForSeconds());
            String totalPlayedTime = TimeUtils.getHoursMinutesSeconds(instance.getTotalPlayedForSeconds());

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

    public InstancesPanel getDefaultInstancesPanel() {
        return this.defaultInstancesPanel;
    }

    public JProgressBar getProgressBar() {
        return this.progressBar;
    }
}
