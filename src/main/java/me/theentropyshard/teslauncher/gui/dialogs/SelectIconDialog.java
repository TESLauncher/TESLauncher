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

package me.theentropyshard.teslauncher.gui.dialogs;

import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.gui.components.InstanceItem;
import me.theentropyshard.teslauncher.gui.layouts.WrapLayout;
import me.theentropyshard.teslauncher.instance.Instance;
import me.theentropyshard.teslauncher.gui.utils.SwingUtils;

import javax.swing.*;
import java.awt.*;

public class SelectIconDialog extends AppDialog {
    public SelectIconDialog(InstanceItem item, Instance instance) {
        super(TESLauncher.frame, "Select an Icon - " + instance.getName());

        JPanel root = new JPanel(new WrapLayout(WrapLayout.LEFT, 8, 8));

        root.setPreferredSize(new Dimension(280, 180));

        String grassPath = "/assets/icons/grass_icon.png";
        JButton grassButton = new JButton(SwingUtils.getIcon(grassPath));
        grassButton.addActionListener(e -> {
            instance.setIconPath(grassPath);
            item.getIconLabel().setIcon(SwingUtils.getIcon(grassPath));

            this.getDialog().dispose();
        });
        root.add(grassButton);

        String craftingTablePath = "/assets/icons/crafting_table_icon.png";
        JButton craftingTableButton = new JButton(SwingUtils.getIcon(craftingTablePath));
        craftingTableButton.addActionListener(e -> {
            instance.setIconPath(craftingTablePath);
            item.getIconLabel().setIcon(SwingUtils.getIcon(craftingTablePath));

            this.getDialog().dispose();
        });
        root.add(craftingTableButton);

        this.setResizable(false);
        this.setContent(root);
        this.center(0);
        this.setVisible(true);
    }
}
