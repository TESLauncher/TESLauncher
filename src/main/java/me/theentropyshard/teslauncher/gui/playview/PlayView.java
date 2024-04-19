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
import me.theentropyshard.teslauncher.swing.MessageBox;
import me.theentropyshard.teslauncher.swing.MouseClickListener;
import me.theentropyshard.teslauncher.swing.MouseEnterExitListener;
import me.theentropyshard.teslauncher.utils.OperatingSystem;
import me.theentropyshard.teslauncher.utils.SwingUtils;
import me.theentropyshard.teslauncher.utils.TimeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class PlayView extends View {
    private static final Logger LOG = LogManager.getLogger(PlayView.class);

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
        defaultItem.addMouseListener(new MouseClickListener(e -> {
            new AddInstanceDialog(this, PlayView.DEFAULT_GROUP_NAME);
        }));
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
                        Icon icon = SwingUtils.getIcon("/assets/grass_icon.png");
                        InstanceItem item = new InstanceItem(icon, instance.getName());
                        PlayView.this.addInstanceItem(item, instance.getGroupName());
                    }

                    String group = TESLauncher.getInstance().getSettings().lastInstanceGroup;
                    if (group != null && !group.isEmpty()) {
                        PlayView.this.model.setSelectedItem(group);
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
            addInstanceItem.addMouseListener(new MouseClickListener(e -> {
                new AddInstanceDialog(this, groupName);
            }));
            panel = new InstancesPanel(addInstanceItem);
            this.groups.put(groupName, panel);
            this.model.addElement(groupName);
            this.instancesPanelView.add(panel.getRoot(), groupName);
        }
        panel.addInstanceItem(item);

        item.addMouseListener(new MouseClickListener(e -> {
            int mouseButton = e.getButton();
            if (mouseButton == MouseEvent.BUTTON1) { // left mouse button
                if (AccountsManager.getCurrentAccount() == null) {
                    MessageBox.showErrorMessage(TESLauncher.frame, "No account selected");
                } else {
                    new InstanceRunner(AccountsManager.getCurrentAccount(), item).start();
                }
            } else if (mouseButton == MouseEvent.BUTTON3) { // right mouse button
                JPopupMenu popupMenu = new JPopupMenu();

                JMenuItem editMenuItem = new JMenuItem("Edit");
                editMenuItem.addActionListener(edit -> {
                    new InstanceSettingsDialog(item.getAssociatedInstance());
                });
                popupMenu.add(editMenuItem);

                JMenuItem deleteMenuItem = new JMenuItem("Delete");
                deleteMenuItem.addActionListener(delete -> {
                    this.deleteInstance(item);
                });
                popupMenu.add(deleteMenuItem);

                popupMenu.addSeparator();

                JMenuItem openInstanceFolder = new JMenuItem("Open instance folder");
                openInstanceFolder.addActionListener(open -> {
                    OperatingSystem.open(item.getAssociatedInstance().getWorkDir());
                });
                popupMenu.add(openInstanceFolder);

                JMenuItem openMinecraftFolder = new JMenuItem("Open Minecraft folder");
                openMinecraftFolder.addActionListener(open -> {
                    OperatingSystem.open(item.getAssociatedInstance().getMinecraftDir());
                });
                popupMenu.add(openMinecraftFolder);

                popupMenu.show(item, e.getX(), e.getY());
            }
        }));

        item.addMouseListener(new MouseEnterExitListener(
                enter -> {
                    this.instanceInfoLabel.setVisible(true);

                    Instance instance = item.getAssociatedInstance();

                    if (instance == null) {
                        return;
                    }

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
                },

                exit -> {
                    this.instanceInfoLabel.setVisible(false);
                    this.instanceInfoLabel.setText("");
                }
        ));
    }

    public void deleteInstance(InstanceItem item) {
        boolean ok = MessageBox.showConfirmMessage(
                TESLauncher.frame,
                "Delete instance",
                "Are you sure you want to delete instance '" + item.getAssociatedInstance().getName() + "'?"
        );

        if (ok) {
            InstanceManager instanceManager = TESLauncher.getInstance().getInstanceManager();
            try {
                instanceManager.removeInstance(item.getAssociatedInstance().getName());
            } catch (IOException ex) {
                LOG.error(ex);
                return;
            }

            JPanel instancesPanel = this.currentPanel.getInstancesPanel();
            instancesPanel.remove(item);
            instancesPanel.revalidate();
        }
    }

    public DefaultComboBoxModel<String> getModel() {
        return this.model;
    }

    public InstancesPanel getCurrentInstancesPanel() {
        return this.currentPanel;
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
