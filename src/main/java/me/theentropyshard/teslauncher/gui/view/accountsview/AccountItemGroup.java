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

package me.theentropyshard.teslauncher.gui.view.accountsview;

import java.util.HashSet;
import java.util.Set;

public class AccountItemGroup {
    private final Set<AccountItem> items;

    public AccountItemGroup() {
        this.items = new HashSet<>();
    }

    public void addAccountItem(AccountItem accountItem) {
        this.items.add(accountItem);

        accountItem.addMouseClickListener(e -> {
            this.items.forEach(item -> {
                item.setSelected(false);
                item.repaint();
            });

            accountItem.setSelected(true);
        });
    }

    public void removeAccountItem(AccountItem accountItem) {
        this.items.remove(accountItem);
        accountItem.setSelected(false);
    }

    public void makeItemSelected(AccountItem accountItem) {
        this.items.forEach(item -> {
            item.setSelected(false);
            item.repaint();
        });

        accountItem.setSelected(true);
    }
}
