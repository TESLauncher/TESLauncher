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

package me.theentropyshard.teslauncher.minecraft.account.gson;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import me.theentropyshard.teslauncher.minecraft.account.Account;
import me.theentropyshard.teslauncher.minecraft.account.AccountStorage;

import java.lang.reflect.Type;
import java.util.Map;

public class AccountStorageDeserializer implements JsonDeserializer<AccountStorage> {
    public AccountStorageDeserializer() {

    }

    @Override
    public AccountStorage deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        JsonObject root = json.getAsJsonObject();

        AccountStorage accountStorage = new AccountStorage();

        JsonElement selectedElement = root.get("selected");
        if (selectedElement != null && !selectedElement.isJsonNull()) {
            accountStorage.setSelected(selectedElement.getAsString());
        }

        Map<String, Account> accounts = ctx.deserialize(root.get("accounts"), new TypeToken<Map<String, Account>>() {}.getType());
        accounts.forEach(accountStorage::addAccount);

        return accountStorage;
    }
}
