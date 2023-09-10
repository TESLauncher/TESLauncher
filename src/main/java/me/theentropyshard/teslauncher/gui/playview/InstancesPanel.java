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

import me.theentropyshard.teslauncher.gui.View;
import me.theentropyshard.teslauncher.gui.components.AddInstanceItem;
import me.theentropyshard.teslauncher.gui.components.InstanceItem;
import me.theentropyshard.teslauncher.gui.layouts.WrapLayout;

import javax.swing.*;
import java.awt.*;

public class InstancesPanel extends View {
    private final AddInstanceItem addInstanceItem;
    private final JPanel instancesPanel;

    public InstancesPanel() {
        JPanel root = this.getRoot();

        this.addInstanceItem = new AddInstanceItem();
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
