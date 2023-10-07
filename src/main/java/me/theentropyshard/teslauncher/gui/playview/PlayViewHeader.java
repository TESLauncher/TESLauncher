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
import me.theentropyshard.teslauncher.gui.View;

import javax.swing.*;
import java.awt.*;

public class PlayViewHeader extends View {
    private final JComboBox<String> instanceGroups;
    private final JComboBox<String> accounts;

    public static PlayViewHeader instance;

    public PlayViewHeader() {
        PlayViewHeader.instance = this;
        JPanel root = this.getRoot();

        JPanel leftSide = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel instanceLabel = new JLabel("Instance group:");
        leftSide.add(instanceLabel);

        this.instanceGroups = new JComboBox<>();
        leftSide.add(this.instanceGroups);

        JPanel rightSide = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JLabel accountsLabel = new JLabel("Account:");
        rightSide.add(accountsLabel);

        this.accounts = new JComboBox<>(
                TESLauncher.getInstance().getAccountsManager().getAccounts()
                        .toArray(new String[0])
        );
        rightSide.add(this.accounts);

        root.add(leftSide, BorderLayout.WEST);
        root.add(rightSide, BorderLayout.EAST);
    }

    public JComboBox<String> getInstanceGroups() {
        return this.instanceGroups;
    }

    public JComboBox<String> getAccounts() {
        return this.accounts;
    }
}
