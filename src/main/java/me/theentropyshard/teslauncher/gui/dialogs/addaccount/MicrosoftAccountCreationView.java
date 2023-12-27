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

package me.theentropyshard.teslauncher.gui.dialogs.addaccount;

import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.accounts.Account;
import me.theentropyshard.teslauncher.accounts.MicrosoftAccount;
import me.theentropyshard.teslauncher.gui.accountsview.AccountItem;
import me.theentropyshard.teslauncher.gui.accountsview.AccountsView;
import me.theentropyshard.teslauncher.minecraft.auth.microsoft.AuthListener;
import me.theentropyshard.teslauncher.minecraft.auth.microsoft.MicrosoftAuthenticator;
import me.theentropyshard.teslauncher.minecraft.auth.microsoft.MinecraftProfile;
import me.theentropyshard.teslauncher.minecraft.auth.microsoft.MinecraftSkin;
import me.theentropyshard.teslauncher.utils.SkinUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

public class MicrosoftAccountCreationView extends JPanel {
    private final AddAccountDialog dialog;
    private final AccountsView accountsView;

    private final JLabel textField;

    // TODO: we might already have an offline account added with the username of the paid account
    // what do we do in this case? we know the username of the paid account only after the authorization
    public MicrosoftAccountCreationView(AddAccountDialog dialog, AccountsView accountsView) {
        this.dialog = dialog;
        this.accountsView = accountsView;

        this.textField = new JLabel();

        this.setLayout(new BorderLayout());

        JButton addMicrosoft = new JButton("Add Microsoft");
        addMicrosoft.addActionListener(e -> {
            this.textField.setText("Working...");

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    AuthListener authListener = (userCode, verificationUri) -> {
                        MicrosoftAccountCreationView.this.textField.setText(
                                "A web page will open now. You will need to paste the\n" +
                                        " code that I already put in your clipboard."
                        );

                        if (!Desktop.isDesktopSupported()) {
                            JOptionPane.showMessageDialog(TESLauncher.getInstance().getGui().getAppWindow().getFrame(),
                                    "java.awt.Desktop is not supported", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        StringSelection selection = new StringSelection(userCode);
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);

                        Desktop desktop = Desktop.getDesktop();
                        try {
                            desktop.browse(new URI(verificationUri));
                        } catch (IOException | URISyntaxException ex) {
                            throw new RuntimeException(ex);
                        }
                    };

                    MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator(
                            TESLauncher.getInstance().getHttpClient(),
                            authListener, null, false
                    );

                    MinecraftProfile profile = authenticator.authenticate();
                    MicrosoftAccount microsoftAccount = new MicrosoftAccount();
                    microsoftAccount.setAccessToken(profile.accessToken);
                    microsoftAccount.setUuid(UUID.fromString(profile.id.replaceFirst(
                            "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"
                    )));
                    microsoftAccount.setUsername(profile.name);
                    microsoftAccount.setRefreshToken(authenticator.getRefreshToken());
                    microsoftAccount.setLoggedInAt(OffsetDateTime.now());
                    microsoftAccount.setExpiresIn(authenticator.getExpiresIn());

                    MicrosoftAccountCreationView.this.textField.setText("Getting skin...");

                    microsoftAccount.setHeadIcon(
                            MicrosoftAccountCreationView.getBase64SkinHead(profile)
                    );

                    TESLauncher.getInstance().getAccountsManager().saveAccount(microsoftAccount);

                    TESLauncher.getInstance().getGui().getAccountsView().addAccountItem(
                            new AccountItem(microsoftAccount)
                    );

                    MicrosoftAccountCreationView.this.textField.setText("Done");

                    dialog.getDialog().dispose();

                    return null;
                }
            }.execute();
        });

        this.add(addMicrosoft, BorderLayout.NORTH);

        this.add(this.textField, BorderLayout.CENTER);
    }

    public static String getBase64SkinHead(MinecraftProfile profile) throws IOException {
        MinecraftSkin skin = null;

        for (MinecraftSkin needle : profile.skins) {
            if ("ACTIVE".equalsIgnoreCase(needle.state)) {
                skin = needle;
            }
        }

        if (skin == null) {
            return Account.DEFAULT_HEAD;
        }

        OkHttpClient httpClient = TESLauncher.getInstance().getHttpClient();

        Request request = new Request.Builder()
                .url(skin.url)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            byte[] bytes = Objects.requireNonNull(response.body()).bytes();
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(bytes));
            BufferedImage skinHead = SkinUtils.getScaledSkinHead(bufferedImage);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(skinHead, "PNG", baos);
            byte[] encode = Base64.getMimeEncoder().encode(baos.toByteArray());

            return new String(encode, StandardCharsets.UTF_8);
        }
    }
}
