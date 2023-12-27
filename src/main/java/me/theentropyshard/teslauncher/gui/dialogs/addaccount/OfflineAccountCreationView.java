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

package me.theentropyshard.teslauncher.gui.dialogs.addaccount;

import com.formdev.flatlaf.FlatClientProperties;
import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.accounts.Account;
import me.theentropyshard.teslauncher.accounts.AccountsManager;
import me.theentropyshard.teslauncher.accounts.OfflineAccount;
import me.theentropyshard.teslauncher.gui.accountsview.AccountItem;
import me.theentropyshard.teslauncher.gui.accountsview.AccountsView;

import javax.swing.*;

public class OfflineAccountCreationView extends JPanel {
    private final JTextField usernameField;

    public OfflineAccountCreationView(AddAccountDialog dialog, AccountsView accountsView) {
        this.usernameField = new JTextField();
        this.usernameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter desired username");

        this.add(this.usernameField);

        JButton button = new JButton("Add");
        button.addActionListener(e -> {
            String text = this.usernameField.getText();
            if (text.isEmpty()) {
                JOptionPane.showMessageDialog(
                        TESLauncher.window.getFrame(),
                        "Enter a username",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            AccountsManager accountsManager = TESLauncher.getInstance().getAccountsManager();

            Account account = new OfflineAccount(text);
            if (!accountsManager.canCreateAccount(account.getUsername())) {
                JOptionPane.showMessageDialog(
                        TESLauncher.window.getFrame(),
                        "Account with username '" + account.getUsername() + "' already exists",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            account.setHeadIcon(Account.DEFAULT_HEAD);
            accountsManager.saveAccount(account);
            accountsView.addAccountItem(new AccountItem(account));

            dialog.getDialog().dispose();
        });
        this.add(button);
    }
}
