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

import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.gui.dialogs.ProgressDialog;
import me.theentropyshard.teslauncher.gui.dialogs.addaccount.MicrosoftAccountCreationView;
import me.theentropyshard.teslauncher.gui.utils.MessageBox;
import me.theentropyshard.teslauncher.gui.view.accountsview.AccountItem;
import me.theentropyshard.teslauncher.logging.Log;
import me.theentropyshard.teslauncher.minecraft.account.Account;
import me.theentropyshard.teslauncher.minecraft.auth.microsoft.AuthAdapter;
import me.theentropyshard.teslauncher.minecraft.auth.microsoft.AuthException;
import me.theentropyshard.teslauncher.minecraft.auth.microsoft.MicrosoftAuthenticator;
import me.theentropyshard.teslauncher.minecraft.auth.microsoft.data.MinecraftProfile;
import me.theentropyshard.teslauncher.utils.UndashedUUID;

import javax.swing.*;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

public class MicrosoftAccount extends Account {
    private MicrosoftAuthInfo microsoftAuthInfo;

    public MicrosoftAccount() {
        this.microsoftAuthInfo = new MicrosoftAuthInfo();
    }

    public boolean stillValid() {
        long timeLeft = ChronoUnit.SECONDS.between(this.microsoftAuthInfo.getLoggedInAt(), OffsetDateTime.now());
        long expiresIn = this.microsoftAuthInfo.getExpiresIn();

        return timeLeft < expiresIn;
    }

    @Override
    public void authenticate() throws IOException, AuthException {
        if (this.microsoftAuthInfo == null) {
            throw new AuthException("Saved auth info is null");
        }

        if (this.stillValid()) {
            Log.info("Account is valid, no need to authenticate");

            return;
        }

        Log.info("Refreshing auth token...");
        this.refresh();
    }

    private void refresh() throws IOException, AuthException {
        ProgressDialog dialog = new ProgressDialog("Authenticating...");
        dialog.onStageChanged("Refreshing auth token...");

        SwingUtilities.invokeLater(() -> dialog.setVisible(true));

        MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator(
                TESLauncher.getInstance().getHttpClient(), new AuthAdapter(),
                this.microsoftAuthInfo.getRefreshToken(), true
        );

        MinecraftProfile profile;
        try {
            profile = authenticator.authenticate();
        } catch (IOException e) {
            MessageBox.showErrorMessage(TESLauncher.frame, e.getMessage());
            SwingUtilities.invokeLater(() -> dialog.getDialog().dispose());

            return;
        }

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
        Log.info("Getting skin...");
        this.setHeadIcon(MicrosoftAccountCreationView.getBase64SkinHead(profile));

        TESLauncher.getInstance().getAccountManager().save();

        SwingUtilities.invokeLater(() -> dialog.getDialog().dispose());
    }

    public void setRefreshToken(String refreshToken) {
        this.microsoftAuthInfo.setRefreshToken(refreshToken);
    }

    public void setExpiresIn(int expiresIn) {
        this.microsoftAuthInfo.setExpiresIn(expiresIn);
    }

    public void setLoggedInAt(OffsetDateTime loggedInAt) {
        this.microsoftAuthInfo.setLoggedInAt(loggedInAt);
    }
}
