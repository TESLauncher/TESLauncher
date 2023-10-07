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

import me.theentropyshard.teslauncher.gui.View;
import me.theentropyshard.teslauncher.gui.components.AddInstanceItem;
import me.theentropyshard.teslauncher.gui.components.InstanceItem;
import me.theentropyshard.teslauncher.gui.layouts.WrapLayout;

import javax.swing.*;
import java.awt.*;

public class InstancesPanel extends View {
    private final AddInstanceItem addInstanceItem;
    private final JPanel instancesPanel;

    public InstancesPanel(AddInstanceItem addInstanceItem) {
        JPanel root = this.getRoot();

        this.addInstanceItem = addInstanceItem;
        this.instancesPanel = new JPanel(new WrapLayout(WrapLayout.LEFT, 8, 8));
        this.instancesPanel.add(this.addInstanceItem);

        JPanel borderPanel = new JPanel(new BorderLayout());
        borderPanel.add(this.instancesPanel, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(
                borderPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(8);

        root.add(scrollPane, BorderLayout.CENTER);
    }

    public void addInstanceItem(InstanceItem item) {
        if (item instanceof AddInstanceItem) {
            throw new IllegalArgumentException("Adding AddInstanceItem is not allowed");
        }

        int count = this.instancesPanel.getComponentCount();
        this.instancesPanel.add(item, count - 1);
    }

    public AddInstanceItem getAddInstanceItem() {
        return this.addInstanceItem;
    }

    public JPanel getInstancesPanel() {
        return this.instancesPanel;
    }
}
