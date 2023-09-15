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
            PathUtils.createFile(this.accountsFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.accounts = new ArrayList<>();
        this.gson = new Gson();
    }

    public void loadAccounts() {
        try {
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
        } catch (IOException e) {
            throw new RuntimeException(e);
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
