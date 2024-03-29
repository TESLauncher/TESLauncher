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

package me.theentropyshard.teslauncher.accounts;

import com.google.gson.JsonObject;
import me.theentropyshard.teslauncher.gson.AbstractJsonDeserializer;
import me.theentropyshard.teslauncher.utils.json.Json;

public class AccountDeserializer extends AbstractJsonDeserializer<Account> {
    @Override
    public Account deserialize(JsonObject root) {
        boolean noHead = !root.has("headIcon");

        if (!root.has("accessToken") || root.get("accessToken").getAsString().equals("-")) {
            OfflineAccount offlineAccount = new OfflineAccount(root.get("username").getAsString());

            if (noHead) {
                offlineAccount.setHeadIcon(Account.DEFAULT_HEAD);
            }

            offlineAccount.setSelected(root.get("selected").getAsBoolean());

            return offlineAccount;
        }

        Account account = Json.parse(root, MicrosoftAccount.class);

        if (noHead) {
            account.setHeadIcon(Account.DEFAULT_HEAD);
        }

        return account;
    }
}
