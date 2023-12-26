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

import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.minecraft.auth.microsoft.AuthException;
import me.theentropyshard.teslauncher.minecraft.auth.microsoft.AuthListener;
import me.theentropyshard.teslauncher.minecraft.auth.microsoft.MicrosoftAuthenticator;
import me.theentropyshard.teslauncher.minecraft.auth.microsoft.MinecraftProfile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class MicrosoftAccount extends Account {
    private String refreshToken;
    private int expiresIn;
    private OffsetDateTime loggedInAt;

    public MicrosoftAccount() {

    }

    public boolean needToRefresh() {
        long between = ChronoUnit.SECONDS.between(this.loggedInAt, OffsetDateTime.now());
        return between > this.expiresIn;
    }

    @Override
    public void authenticate() throws IOException, AuthException {
        if (this.needToRefresh()) {
            AuthListener authListener = (userCode, verificationUri) -> {

            };

            MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator(
                    TESLauncher.getInstance().getHttpClient(),
                    authListener, this.refreshToken, true
            );

            MinecraftProfile profile = authenticator.authenticate();
            this.setAccessToken(profile.accessToken);
            this.setUuid(UUID.fromString(profile.id.replaceFirst(
                    "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"
            )));
            this.setUsername(profile.name);
            this.setRefreshToken(authenticator.getRefreshToken());
            this.setLoggedInAt(OffsetDateTime.now());
            this.setExpiresIn(authenticator.getExpiresIn());

            TESLauncher.getInstance().getAccountsManager().saveAccount(this);
        }
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public int getExpiresIn() {
        return this.expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    public OffsetDateTime getLoggedInAt() {
        return this.loggedInAt;
    }

    public void setLoggedInAt(OffsetDateTime loggedInAt) {
        this.loggedInAt = loggedInAt;
    }
}
