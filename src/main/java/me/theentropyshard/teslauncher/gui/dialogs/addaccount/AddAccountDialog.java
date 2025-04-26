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

package me.theentropyshard.teslauncher.gui.dialogs.addaccount;

import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.gui.components.MyTabbedPane;
import me.theentropyshard.teslauncher.gui.view.accountsview.AccountsView;
import me.theentropyshard.teslauncher.gui.dialogs.AppDialog;

import javax.swing.*;
import java.awt.*;

public class AddAccountDialog extends AppDialog {
    private final OfflineAccountCreationView offlineView;
    private final MicrosoftAccountCreationView microsoftView;

    public AddAccountDialog(AccountsView accountsView) {
        super(TESLauncher.frame, "Add new account");

        JPanel root = new JPanel(new BorderLayout());

        JTabbedPane viewSelector = new MyTabbedPane(JTabbedPane.TOP);
        viewSelector.putClientProperty("JTabbedPane.tabAreaAlignment", "fill");

        this.offlineView = new OfflineAccountCreationView(this, accountsView);
        viewSelector.addTab("Offline", this.offlineView);

        this.microsoftView = new MicrosoftAccountCreationView(this, accountsView);
        viewSelector.addTab("Microsoft", this.microsoftView);

        root.add(viewSelector, BorderLayout.CENTER);
        //root.setPreferredSize(new Dimension(450, 270));

        this.setResizable(false);
        this.setContent(root);
        this.center(0);
        this.setVisible(true);
    }
}
