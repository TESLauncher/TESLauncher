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

package me.theentropyshard.teslauncher.gui.view.playview;

import me.theentropyshard.teslauncher.gui.components.AddInstanceItem;
import me.theentropyshard.teslauncher.gui.components.InstanceItem;
import me.theentropyshard.teslauncher.gui.layouts.WrapLayout;

import javax.swing.*;
import java.awt.*;

public class InstancesPanel extends JPanel {
    private final AddInstanceItem addInstanceItem;
    private final JPanel instancesPanel;
    private final JScrollPane scrollPane;

    public InstancesPanel(AddInstanceItem addInstanceItem) {
        super(new BorderLayout());

        this.addInstanceItem = addInstanceItem;
        this.instancesPanel = new JPanel(new WrapLayout(WrapLayout.LEFT, 8, 8));
        this.instancesPanel.add(this.addInstanceItem);

        JPanel borderPanel = new JPanel(new BorderLayout());
        borderPanel.add(this.instancesPanel, BorderLayout.CENTER);

        this.scrollPane = new JScrollPane(
                borderPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        this.scrollPane.setBorder(null);
        this.scrollPane.getVerticalScrollBar().setUnitIncrement(8);

        this.add(this.scrollPane, BorderLayout.CENTER);
    }

    public void addInstanceItem(InstanceItem item, boolean sort) {
        if (sort) {
            boolean added = false;
            int index = 0;

            for (Component component : this.instancesPanel.getComponents()) {
                if (component.getClass() == AddInstanceItem.class) {
                    continue;
                }

                InstanceItem cItem = (InstanceItem) component;
                if (cItem.getAssociatedInstance().getLastTimePlayed().isBefore(item.getAssociatedInstance().getLastTimePlayed())) {
                    this.instancesPanel.add(item, index);

                    added = true;
                    break;
                }

                index++;
            }

            if (!added) {
                this.addInstanceItemToEnd(item);
            }
        } else {
            this.addInstanceItemToEnd(item);
        }
    }

    public void addInstanceItemToEnd(InstanceItem item) {
        int count = this.instancesPanel.getComponentCount();
        this.instancesPanel.add(item, count - 1);
    }

    public void removeInstanceItem(InstanceItem item) {
        this.instancesPanel.remove(item);
    }

    public void makeItemFirst(InstanceItem item) {
        int count = this.instancesPanel.getComponentCount();

        for (int i = 0; i < count; i++) {
            Component component = this.instancesPanel.getComponent(i);
            if (component.equals(item)) {
                this.instancesPanel.remove(component);

                break;
            }
        }

        this.instancesPanel.add(item, 0);
    }

    public AddInstanceItem getAddInstanceItem() {
        return this.addInstanceItem;
    }

    public JScrollPane getScrollPane() {
        return this.scrollPane;
    }

    public JPanel getInstancesPanel() {
        return this.instancesPanel;
    }
}
