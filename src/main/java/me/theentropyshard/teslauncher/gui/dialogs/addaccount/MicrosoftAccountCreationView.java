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

package me.theentropyshard.teslauncher.gui.dialogs.addaccount;

import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.accounts.Account;
import me.theentropyshard.teslauncher.accounts.MicrosoftAccount;
import me.theentropyshard.teslauncher.gui.view.accountsview.AccountItem;
import me.theentropyshard.teslauncher.gui.view.accountsview.AccountsView;
import me.theentropyshard.teslauncher.gui.dialogs.OpenBrowserDialog;
import me.theentropyshard.teslauncher.minecraft.auth.microsoft.AuthListener;
import me.theentropyshard.teslauncher.minecraft.auth.microsoft.MicrosoftAuthenticator;
import me.theentropyshard.teslauncher.minecraft.auth.microsoft.MinecraftProfile;
import me.theentropyshard.teslauncher.minecraft.auth.microsoft.MinecraftSkin;
import me.theentropyshard.teslauncher.swing.MessageBox;
import me.theentropyshard.teslauncher.utils.OperatingSystem;
import me.theentropyshard.teslauncher.utils.SkinUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
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

                        secondView.onGettingSkin();

                        microsoftAccount.setHeadIcon(
                                MicrosoftAccountCreationView.getBase64SkinHead(profile)
                        );

                        secondView.onFinish();

                        TESLauncher.getInstance().getAccountsManager().saveAccount(microsoftAccount);

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
                    MessageBox.showErrorMessage(TESLauncher.frame, "java.awt.Desktop is not supported");
                    return;
                }

                Desktop desktop = Desktop.getDesktop();
                try {
                    desktop.browse(new URI(verificationUri));
                } catch (IOException | URISyntaxException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                new OpenBrowserDialog(userCode, verificationUri);
            }
        }

        @Override
        public void onMinecraftAuth() {
            String text = this.textPane.getText();
            this.textPane.setText(text + "\n\n" + "Authenticating with Minecraft...");
        }

        @Override
        public void onCheckGameOwnership() {
            String text = this.textPane.getText();
            this.textPane.setText(text + "\n\n" + "Checking game ownership...");
        }

        @Override
        public void onGettingSkin() {
            String text = this.textPane.getText();
            this.textPane.setText(text + "\n\n" + "Getting skin...");
        }

        @Override
        public void onFinish() {
            String text = this.textPane.getText();
            this.textPane.setText(text + "\n\n" + "Finished");
        }

        public boolean isSelectedBox() {
            return this.selectedBox;
        }

        public void setSelectedBox(boolean selectedBox) {
            this.selectedBox = selectedBox;
        }
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
