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

package me.theentropyshard.teslauncher.minecraft.account.microsoft;

import java.time.OffsetDateTime;

public class MicrosoftAuthInfo {
    private String refreshToken;
    private long expiresIn;
    private OffsetDateTime loggedInAt;

    public MicrosoftAuthInfo() {

    }

    public MicrosoftAuthInfo(String refreshToken, long expiresIn, OffsetDateTime loggedInAt) {
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.loggedInAt = loggedInAt;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public long getExpiresIn() {
        return this.expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public OffsetDateTime getLoggedInAt() {
        return this.loggedInAt;
    }

    public void setLoggedInAt(OffsetDateTime loggedInAt) {
        this.loggedInAt = loggedInAt;
    }
}
