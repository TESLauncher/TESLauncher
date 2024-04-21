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

import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.gui.dialogs.MinecraftDownloadDialog;
import me.theentropyshard.teslauncher.gui.view.accountsview.AccountItem;
import me.theentropyshard.teslauncher.gui.dialogs.addaccount.MicrosoftAccountCreationView;
import me.theentropyshard.teslauncher.minecraft.auth.microsoft.AuthException;
import me.theentropyshard.teslauncher.minecraft.auth.microsoft.AuthListener;
import me.theentropyshard.teslauncher.minecraft.auth.microsoft.MicrosoftAuthenticator;
import me.theentropyshard.teslauncher.minecraft.auth.microsoft.MinecraftProfile;
import me.theentropyshard.teslauncher.utils.SwingUtils;
import me.theentropyshard.teslauncher.utils.UndashedUUID;

import javax.swing.*;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

public class MicrosoftAccount extends Account {
    private String refreshToken;
    private int expiresIn;
    private OffsetDateTime loggedInAt;

    public MicrosoftAccount() {

    }

    public boolean needToRefresh() {
        long between = ChronoUnit.SECONDS.between(this.loggedInAt, OffsetDateTime.now());
        return between >= this.expiresIn;
    }

    @Override
    public void authenticate() throws IOException, AuthException {
        if (this.needToRefresh()) {
            MinecraftDownloadDialog dialog = new MinecraftDownloadDialog();
            dialog.getDialog().setTitle("Authenticating...");
            dialog.onStageChanged("Refreshing auth token...");

            SwingUtilities.invokeLater(() -> dialog.setVisible(true));

            AuthListener authListener = new AuthListener() {
                @Override
                public void onUserCodeReceived(String userCode, String verificationUri) {

                }

                @Override
                public void onMinecraftAuth() {

                }

                @Override
                public void onCheckGameOwnership() {

                }

                @Override
                public void onGettingSkin() {

                }

                @Override
                public void onFinish() {

                }
            };

            MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator(
                    TESLauncher.getInstance().getHttpClient(),
                    authListener, this.refreshToken, true
            );

            MinecraftProfile profile = authenticator.authenticate();
            this.setAccessToken(profile.accessToken);
            this.setUuid(UndashedUUID.fromString(profile.id));

            String oldUsername = this.getUsername();

            AccountItem byName = TESLauncher.getInstance().getGui().getAccountsView().getByName(oldUsername);
            if (byName != null) {
                byName.getNickLabel().setText(profile.name);
            }

            this.setUsername(profile.name);
            this.setRefreshToken(authenticator.getRefreshToken());
            this.setLoggedInAt(OffsetDateTime.now());
            this.setExpiresIn(authenticator.getExpiresIn());

            dialog.onStageChanged("Getting skin...");
            this.setHeadIcon(
                    MicrosoftAccountCreationView.getBase64SkinHead(profile)
            );

            TESLauncher.getInstance().getAccountsManager().save();

            SwingUtilities.invokeLater(() -> dialog.getDialog().dispose());
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
