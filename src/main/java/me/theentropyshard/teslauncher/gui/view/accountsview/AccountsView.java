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

package me.theentropyshard.teslauncher.gui.view.accountsview;

import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.minecraft.accounts.Account;
import me.theentropyshard.teslauncher.minecraft.accounts.AccountManager;
import me.theentropyshard.teslauncher.gui.dialogs.addaccount.AddAccountDialog;
import me.theentropyshard.teslauncher.gui.view.playview.PlayViewHeader;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class AccountsView extends JPanel {
    private final JPanel panel;
    private final JScrollPane scrollPane;
    private final AccountItemGroup group;

    private final AddAccountItem addAccountItem;

    public AccountsView() {
        super(new BorderLayout());

        this.group = new AccountItemGroup();

        this.panel = new JPanel(new GridLayout(0, 1, 0, 1));
        this.panel.setBorder(new EmptyBorder(4, 4, 4, 4));
        JPanel borderPanel = new JPanel(new BorderLayout());
        borderPanel.add(this.panel, BorderLayout.PAGE_START);

        this.scrollPane = new JScrollPane(borderPanel);
        this.scrollPane.setBorder(null);
        this.add(this.scrollPane, BorderLayout.CENTER);

        this.addAccountItem = new AddAccountItem();
        this.addAccountItem.addMouseClickListener(e -> {
            new AddAccountDialog(this);
        });
        this.panel.add(this.addAccountItem);

        AccountManager accountManager = TESLauncher.getInstance().getAccountsManager();
        List<Account> accounts = accountManager.getAccounts();
        for (Account account : accounts) {
            if (account.getHeadIcon() == null) {
                account.setHeadIcon(Account.DEFAULT_HEAD);
            }
            AccountItem item = new AccountItem(account);
            this.addAccountItem(item);
            if (account.isSelected()) {
                this.group.makeItemSelected(item);
            }
        }
    }

    public AccountItem getByName(String name) {
        for (Component component : this.panel.getComponents()) {
            if (!(component instanceof AccountItem)) {
                continue;
            }

            if (((AccountItem) component).getAccount().getUsername().equals(name)) {
                return (AccountItem) component;
            }
        }

        return null;
    }

    public void addAccountItem(JComponent item) {
        if (!((item instanceof AccountItem) || (item instanceof AddAccountItem))) {
            throw new IllegalArgumentException(String.valueOf(item));
        }

        if (!(item instanceof AccountItem)) {
            return;
        }

        AccountItem accountItem = (AccountItem) item;

        accountItem.addMouseClickListener(e -> {
            AccountManager accountManager = TESLauncher.getInstance().getAccountsManager();
            accountManager.selectAccount(accountItem.getAccount());
            try {
                accountManager.save();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            PlayViewHeader header = TESLauncher.getInstance().getGui().getPlayView().getHeader();
            header.setCurrentAccount(accountItem.getAccount());
        });

        this.group.addAccountItem(accountItem);

        int count = this.panel.getComponentCount();
        this.panel.add(item, count - 1);
        this.panel.revalidate();
    }

    public void removeAccountItem(JComponent item) {
        if (!((item instanceof AccountItem) || (item instanceof AddAccountItem))) {
            throw new IllegalArgumentException(String.valueOf(item));
        }

        if (item instanceof AccountItem) {
            this.group.removeAccountItem((AccountItem) item);
        }

        this.panel.remove(item);
        this.panel.revalidate();
    }

    public JScrollPane getScrollPane() {
        return this.scrollPane;
    }

    public JPanel getPanel() {
        return this.panel;
    }

    public AccountItemGroup getGroup() {
        return this.group;
    }
}
