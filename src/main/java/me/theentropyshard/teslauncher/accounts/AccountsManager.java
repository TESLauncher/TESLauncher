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

package me.theentropyshard.teslauncher.accounts;

import com.google.gson.reflect.TypeToken;
import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.utils.FileUtils;
import me.theentropyshard.teslauncher.utils.IOUtils;
import me.theentropyshard.teslauncher.utils.Json;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class AccountsManager {
    private final Path accountsFile;
    private final Map<String, Account> accounts;

    public AccountsManager(Path workDir) {
        this.accountsFile = workDir.resolve("accounts.json");

        try {
            FileUtils.createFileIfNotExists(this.accountsFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.accounts = new LinkedHashMap<>();
    }

    public static Account getCurrentAccount() {
        Map<String, Account> accountsMap = TESLauncher.getInstance().getAccountsManager().getAccountsMap();
        for (Map.Entry<String, Account> entry : accountsMap.entrySet()) {
            Account account = entry.getValue();
            if (account.isSelected()) {
                return account;
            }
        }

        return null;
    }

    public boolean canCreateAccount(String username) {
        return !this.accounts.containsKey(username);
    }

    public void loadAccounts() throws IOException {
        Map<String, Account> accounts = Json.parse(IOUtils.readUtf8String(this.accountsFile), new TypeToken<Map<String, Account>>() {}.getType());
        if (accounts != null) {
            this.accounts.putAll(accounts);
        }
    }

    public boolean saveAccount(Account account) {
        if (!this.canCreateAccount(account.getUsername())) {
            return false;
        }

        this.accounts.put(account.getUsername(), account);

        try {
            this.save();

            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean deleteAccount(String nickname) {
        if (this.accounts.containsKey(nickname)) {
            this.accounts.remove(nickname);

            try {
                this.save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return true;
        }

        return false;
    }

    public void selectAccount(Account account) {
        for (Map.Entry<String, Account> entry : this.accounts.entrySet()) {
            entry.getValue().setSelected(false);
        }

        account.setSelected(true);
    }

    public void save() throws IOException {
        String json = Json.write(this.accounts);
        Files.write(this.accountsFile, json.getBytes(StandardCharsets.UTF_8));
    }

    public List<Account> getAccounts() {
        return new ArrayList<>(this.accounts.values());
    }

    public Map<String, Account> getAccountsMap() {
        return this.accounts;
    }

    public void removeAccount(Account account) throws IOException {
        this.accounts.remove(account.getUsername());

        this.save();
    }
}
