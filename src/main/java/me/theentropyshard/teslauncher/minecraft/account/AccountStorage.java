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

package me.theentropyshard.teslauncher.minecraft.account;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AccountStorage {
    private String selected;
    private final Map<String, Account> accounts;

    public AccountStorage() {
        this.accounts = new LinkedHashMap<>();
    }

    public void addAccount(Account account) {
        this.addAccount(account.getUsername(), account);
    }

    public void addAccount(String username, Account account) {
        this.accounts.put(username, account);
    }

    public Account getSelectedAccount() {
        return this.accounts.get(this.selected);
    }

    public boolean accountExists(String username) {
        return this.accounts.containsKey(username);
    }

    public void removeAccount(Account account) {
        this.accounts.remove(account.getUsername());

        if (this.selected != null && this.selected.equals(account.getUsername())) {
            if (this.accounts.size() == 0) {
                this.setSelected(null);
            } else {
                this.setSelected(this.getAccountList().get(0).getUsername());
            }
        }
    }

    public List<Account> getAccountList() {
        return new ArrayList<>(this.accounts.values());
    }

    public String getSelected() {
        return this.selected;
    }

    public void setSelected(String selected) {
        this.selected = selected;
    }

    public Map<String, Account> getAccounts() {
        return this.accounts;
    }
}
