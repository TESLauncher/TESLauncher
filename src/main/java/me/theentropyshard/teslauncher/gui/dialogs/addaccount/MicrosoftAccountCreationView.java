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

package me.theentropyshard.teslauncher.gui.dialogs.addaccount;

import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.gui.dialogs.OpenBrowserDialog;
import me.theentropyshard.teslauncher.gui.utils.MessageBox;
import me.theentropyshard.teslauncher.gui.view.accountsview.AccountItem;
import me.theentropyshard.teslauncher.gui.view.accountsview.AccountsView;
import me.theentropyshard.teslauncher.minecraft.account.Account;
import me.theentropyshard.teslauncher.minecraft.account.microsoft.MicrosoftAccount;
import me.theentropyshard.teslauncher.minecraft.auth.microsoft.AuthException;
import me.theentropyshard.teslauncher.minecraft.auth.microsoft.AuthListener;
import me.theentropyshard.teslauncher.minecraft.auth.microsoft.MicrosoftAuthenticator;
import me.theentropyshard.teslauncher.minecraft.auth.microsoft.data.MinecraftProfile;
import me.theentropyshard.teslauncher.minecraft.auth.microsoft.data.MinecraftSkin;
import me.theentropyshard.teslauncher.utils.ListUtils;
import me.theentropyshard.teslauncher.utils.OperatingSystem;
import me.theentropyshard.teslauncher.utils.SkinUtils;
import me.theentropyshard.teslauncher.utils.UndashedUUID;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import me.theentropyshard.teslauncher.logging.Log;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Objects;

//TODO: this is still big shit. fix it
public class MicrosoftAccountCreationView extends JPanel {
    

    private final CardLayout layout;
    private final AddAccountDialog dialog;

    public MicrosoftAccountCreationView(AddAccountDialog dialog, AccountsView accountsView) {
        this.dialog = dialog;
        this.layout = new CardLayout();
        this.setLayout(this.layout);

        SecondView secondView = new SecondView(this);
        FirstView firstView = new FirstView(this, secondView);
        this.add(firstView, FirstView.class.getName());
        this.add(secondView, SecondView.class.getName());

        this.layout.show(this, "first");
    }

    private static final class FirstView extends JPanel {
        public FirstView(MicrosoftAccountCreationView v, SecondView secondView) {
            super(new BorderLayout());

            JPanel topPanel = new JPanel(new BorderLayout());

            JTextPane textPane = new JTextPane();
            textPane.setEditable(false);
            textPane.setText("You are about to add Microsoft account. When you will press the Proceed button, a web page\n" +
                    "will open, where you will need to paste a code that I will put in your clipboard.");

            topPanel.add(textPane, BorderLayout.CENTER);
            this.add(topPanel, BorderLayout.NORTH);

            JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JCheckBox checkBox = new JCheckBox("I want to open browser myself");
            checkBox.addActionListener(e -> {
                secondView.setSelectedBox(!secondView.isSelectedBox());
            });
            centerPanel.add(checkBox);
            this.add(centerPanel, BorderLayout.CENTER);

            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton proceed = new JButton("Proceed");
            proceed.addActionListener(e -> {
                v.layout.show(v, SecondView.class.getName());

                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator(
                                TESLauncher.getInstance().getHttpClient(),
                                secondView, null, false
                        );

                        MinecraftProfile profile = null;
                        try {
                            profile = authenticator.authenticate();
                        } catch (AuthException e) {
                            Log.error("Could not authenticate", e);
                            MessageBox.showErrorMessage(TESLauncher.frame, e.getMessage());
                        }

                        if (profile == null) {
                            Log.error("Profile is null");

                            return null;
                        }

                        MicrosoftAccount microsoftAccount = new MicrosoftAccount();
                        microsoftAccount.setAccessToken(profile.accessToken);
                        microsoftAccount.setUuid(UndashedUUID.fromString(profile.id));
                        microsoftAccount.setUsername(profile.name);
                        microsoftAccount.setRefreshToken(authenticator.getRefreshToken());
                        microsoftAccount.setLoggedInAt(OffsetDateTime.now());
                        microsoftAccount.setExpiresIn(authenticator.getExpiresIn());

                        secondView.onGettingSkin();

                        microsoftAccount.setHeadIcon(
                                MicrosoftAccountCreationView.getBase64SkinHead(profile)
                        );

                        secondView.onFinish();

                        TESLauncher.getInstance().getAccountManager().saveAccount(microsoftAccount);

                        TESLauncher.getInstance().getGui().getAccountsView().addAccountItem(
                                new AccountItem(microsoftAccount)
                        );

                        v.dialog.getDialog().dispose();

                        return null;
                    }
                }.execute();
            });
            bottomPanel.add(proceed);
            this.add(bottomPanel, BorderLayout.SOUTH);
        }
    }

    private static final class SecondView extends JPanel implements AuthListener {
        private final JTextPane textPane;
        private boolean selectedBox;

        public SecondView(MicrosoftAccountCreationView v) {
            super(new BorderLayout());

            JPanel centerPanel = new JPanel(new BorderLayout());

            this.textPane = new JTextPane();
            this.textPane.setEditable(false);
            this.textPane.setText("Waiting for the user to enter account credentials...");

            centerPanel.add(this.textPane, BorderLayout.CENTER);
            this.add(centerPanel, BorderLayout.CENTER);

            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton finish = new JButton("Finish");
            finish.addActionListener(e -> {
                v.dialog.getDialog().dispose();
            });
            bottomPanel.add(finish);
            this.add(bottomPanel, BorderLayout.SOUTH);
        }

        @Override
        public void onUserCodeReceived(String userCode, String verificationUri) {
            OperatingSystem.copyToClipboard(userCode);

            if (!this.isSelectedBox()) {
                if (!Desktop.isDesktopSupported()) {
                    MessageBox.showErrorMessage(TESLauncher.frame, "java.awt.Desktop is not supported, try opening the browser yourself");
                    Log.warn("java.awt.Desktop is not supported, try opening the browser yourself");

                    return;
                }

                OperatingSystem.browse(verificationUri);
            } else {
                new OpenBrowserDialog(userCode, verificationUri);
            }
        }

        @Override
        public void onMinecraftAuth() {
            SwingUtilities.invokeLater(() -> {
                String text = this.textPane.getText();
                this.textPane.setText(text + "\n" + "Authenticating with Minecraft...");
            });
        }

        @Override
        public void onCheckGameOwnership() {
            SwingUtilities.invokeLater(() -> {
                String text = this.textPane.getText();
                this.textPane.setText(text + "\n" + "Checking game ownership...");
            });
        }

        @Override
        public void onGettingSkin() {
            SwingUtilities.invokeLater(() -> {
                String text = this.textPane.getText();
                this.textPane.setText(text + "\n" + "Getting skin...");
            });
        }

        @Override
        public void onFinish() {
            SwingUtilities.invokeLater(() -> {
                String text = this.textPane.getText();
                this.textPane.setText(text + "\n" + "Finished");
            });
        }

        public boolean isSelectedBox() {
            return this.selectedBox;
        }

        public void setSelectedBox(boolean selectedBox) {
            this.selectedBox = selectedBox;
        }
    }

    public static String getBase64SkinHead(MinecraftProfile profile) throws IOException {
        MinecraftSkin skin = ListUtils.search(profile.skins, s -> "ACTIVE".equalsIgnoreCase(s.state));

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
