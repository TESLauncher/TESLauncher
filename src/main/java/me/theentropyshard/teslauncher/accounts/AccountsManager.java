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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import me.theentropyshard.teslauncher.gui.playview.PlayViewHeader;
import me.theentropyshard.teslauncher.utils.PathUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AccountsManager {
    private final Path accountsFile;
    private final List<String> accounts;
    private final Gson gson;

    public AccountsManager(Path workDir) {
        this.accountsFile = workDir.resolve("accounts.json");

        try {
            PathUtils.createFileIfNotExists(this.accountsFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.accounts = new ArrayList<>();
        this.gson = new Gson();
    }

    public void loadAccounts() throws IOException {
        InputStream inputStream = Files.newInputStream(this.accountsFile);
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        JsonArray accountsArray = this.gson.fromJson(reader, JsonArray.class);
        if (accountsArray == null) {
            return;
        }
        for (JsonElement element : accountsArray) {
            String nickname = element.getAsString();
            this.accounts.add(nickname);
        }
    }

    public boolean saveAccount(String nickname) {
        if (this.accounts.contains(nickname)) {
            return false;
        }

        this.accounts.add(nickname);

        try {
            this.save();

            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean deleteAccount(String nickname) {
        if (this.accounts.contains(nickname)) {
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

    public void save() throws IOException {
        String json = this.gson.toJson(this.accounts);
        Files.write(this.accountsFile, json.getBytes(StandardCharsets.UTF_8));
    }

    public static String getCurrentUsername() {
        return String.valueOf(PlayViewHeader.instance.getAccounts().getSelectedItem());
    }

    public List<String> getAccounts() {
        return new ArrayList<>(this.accounts);
    }
}
