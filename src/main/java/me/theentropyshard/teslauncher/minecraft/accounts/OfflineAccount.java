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

package me.theentropyshard.teslauncher.minecraft.accounts;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class OfflineAccount extends Account {
    public OfflineAccount(String username) {
        this.setUsername(username);
    }

    @Override
    public void authenticate() throws IOException {
        this.setUuid(UUID.nameUUIDFromBytes(("OfflineAccount:" + this.getUsername()).getBytes(StandardCharsets.UTF_8)));
        this.setAccessToken("-");
    }
}
