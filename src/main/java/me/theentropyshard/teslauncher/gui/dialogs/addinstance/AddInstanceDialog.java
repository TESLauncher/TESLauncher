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

package me.theentropyshard.teslauncher.gui.dialogs.addinstance;

import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.gui.components.InstanceItem;
import me.theentropyshard.teslauncher.gui.dialogs.AppDialog;
import me.theentropyshard.teslauncher.gui.playview.PlayView;
import me.theentropyshard.teslauncher.instance.InstanceManager;
import me.theentropyshard.teslauncher.utils.SwingUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.IOException;

public class AddInstanceDialog extends AppDialog {
    private final JTextField nameField;
    private final JTextField groupField;
    private final JButton addButton;
    private final JCheckBox releasesBox;
    private final JCheckBox snapshotsBox;
    private final JCheckBox betasBox;
    private final JCheckBox alphasBox;

    public AddInstanceDialog(PlayView playView, String groupName) {
        super(TESLauncher.window.getFrame(), "Add New Instance");

        JPanel root = new JPanel(new BorderLayout());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(10, 10, 5, 10));

        JPanel headerPanelLeftPanel = new JPanel();
        headerPanelLeftPanel.setLayout(new GridLayout(2, 1));
        headerPanelLeftPanel.add(new JLabel("Name:") {{
            this.setVerticalTextPosition(JLabel.CENTER);
            this.setBorder(new EmptyBorder(0, 0, 0, 10));
        }});
        headerPanelLeftPanel.add(new JLabel("Group:") {{
            this.setVerticalTextPosition(JLabel.CENTER);
            this.setBorder(new EmptyBorder(0, 0, 0, 10));
        }});

        JPanel headerPanelRightPanel = new JPanel();
        headerPanelRightPanel.setLayout(new GridLayout(2, 1));

        this.nameField = new JTextField();
        headerPanelRightPanel.add(this.nameField);

        this.groupField = new JTextField(groupName);
        headerPanelRightPanel.add(this.groupField);

        headerPanel.add(headerPanelLeftPanel, BorderLayout.WEST);
        headerPanel.add(headerPanelRightPanel, BorderLayout.CENTER);

        root.add(headerPanel, BorderLayout.NORTH);

        JTable versionsTable = new JTable();
        McVersionsTableModel tableModel = new McVersionsTableModel(this, versionsTable);
        versionsTable.setModel(tableModel);

        ListSelectionModel selectionModel = versionsTable.getSelectionModel();
        selectionModel.addListSelectionListener(e -> {
            int selectedRow = versionsTable.getSelectedRow();
            selectedRow = versionsTable.convertRowIndexToModel(selectedRow);
            String mcVersion = String.valueOf(versionsTable.getModel().getValueAt(selectedRow, 0));
            this.nameField.setText(mcVersion);
        });

        JScrollPane scrollPane = new JScrollPane(versionsTable);

        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridy = 0;

        JLabel filterLabel = new JLabel("Filter");
        filterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        filterPanel.add(filterLabel, gbc);

        gbc.anchor = GridBagConstraints.WEST;

        this.releasesBox = new JCheckBox("Releases", true);
        this.snapshotsBox = new JCheckBox("Snapshots");
        this.betasBox = new JCheckBox("Betas");
        this.alphasBox = new JCheckBox("Alphas");
        JCheckBox experimentsBox = new JCheckBox("Experiments");

        gbc.gridy = 1;
        filterPanel.add(this.releasesBox, gbc);
        gbc.gridy = 2;
        filterPanel.add(this.snapshotsBox, gbc);
        gbc.gridy = 3;
        filterPanel.add(this.betasBox, gbc);
        gbc.gridy = 4;
        filterPanel.add(this.alphasBox, gbc);

        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weighty = 1.0;
        filterPanel.add(experimentsBox, gbc);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(filterPanel, BorderLayout.EAST);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        this.addButton = new JButton("Add");
        this.addButton.setEnabled(false);
        this.addButton.addActionListener(e -> {
            String instanceName = this.nameField.getText();
            if (instanceName.trim().isEmpty()) {
                JOptionPane.showMessageDialog(
                        AddInstanceDialog.this.getDialog(),
                        "Instance name cannot be empty",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            if (versionsTable.getSelectedRow() == -1) {
                JOptionPane.showMessageDialog(
                        AddInstanceDialog.this.getDialog(),
                        "Minecraft version is not selected",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            String chosenGroupName = this.groupField.getText();
            playView.addInstanceItem(new InstanceItem(SwingUtils.getIcon("/grass_icon.png"), instanceName), chosenGroupName);
            this.getDialog().dispose();
            TableModel model = versionsTable.getModel();
            int selectedRow = versionsTable.getSelectedRow();
            selectedRow = versionsTable.convertRowIndexToModel(selectedRow);
            String mcVersion = String.valueOf(model.getValueAt(selectedRow, 0));
            TESLauncher.getInstance().doTask(() -> {
                InstanceManager instanceManager = TESLauncher.getInstance().getInstanceManager();
                try {
                    instanceManager.createInstance(instanceName, chosenGroupName, mcVersion);
                } catch (IOException ex) {
                    throw new RuntimeException("Unable to create new instance", ex);
                }
            });
        });
        buttonsPanel.add(this.addButton);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            this.getDialog().dispose();
        });
        buttonsPanel.add(cancelButton);
        buttonsPanel.setBorder(new EmptyBorder(0, 10, 6, 6));
        root.add(buttonsPanel, BorderLayout.SOUTH);

        root.add(centerPanel, BorderLayout.CENTER);
        root.setPreferredSize(new Dimension(800, 600));

        this.setResizable(false);
        this.setContent(root);
        this.center(0);
        this.setVisible(true);
    }

    public JButton getAddButton() {
        return this.addButton;
    }

    public JCheckBox getReleasesBox() {
        return this.releasesBox;
    }

    public JCheckBox getSnapshotsBox() {
        return this.snapshotsBox;
    }

    public JCheckBox getBetasBox() {
        return this.betasBox;
    }

    public JCheckBox getAlphasBox() {
        return this.alphasBox;
    }
}
