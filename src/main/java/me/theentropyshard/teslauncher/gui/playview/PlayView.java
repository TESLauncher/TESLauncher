/*
 * Copyright 2023 TheEntropyShard (https://github.com/TheEntropyShard)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.theentropyshard.teslauncher.gui.playview;

import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.gui.View;
import me.theentropyshard.teslauncher.gui.components.AddInstanceItem;
import me.theentropyshard.teslauncher.gui.components.InstanceItem;
import me.theentropyshard.teslauncher.gui.dialogs.AddInstanceDialog;
import me.theentropyshard.teslauncher.instance.Instance;
import me.theentropyshard.teslauncher.instance.InstanceManager;
import me.theentropyshard.teslauncher.instance.InstanceRunner;
import me.theentropyshard.teslauncher.utils.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
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
        this.progressBar.setStringPainted(true);
        this.progressBar.setVisible(false);
        root.add(this.progressBar, BorderLayout.SOUTH);

        new SwingWorker<List<Instance>, Void>() {
            @Override
            protected List<Instance> doInBackground() throws Exception {
                return TESLauncher.getInstance().getInstanceManager().getInstances();
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
                    throw new RuntimeException(e);
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
            InstanceItem clickedItem = (InstanceItem) e.getSource();
            InstanceManager manager = TESLauncher.getInstance().getInstanceManager();

            new InstanceRunner(manager.getInstanceByName(clickedItem.getTextLabel().getText())).start();
            // TODO manager.runInstance(clickedItem.getTextLabel().getText());
        }, true);
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
