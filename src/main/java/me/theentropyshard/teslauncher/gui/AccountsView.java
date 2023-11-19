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

package me.theentropyshard.teslauncher.gui;

import com.formdev.flatlaf.FlatClientProperties;
import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.accounts.AccountsManager;
import me.theentropyshard.teslauncher.accounts.MicrosoftAccount;
import me.theentropyshard.teslauncher.accounts.OfflineAccount;
import me.theentropyshard.teslauncher.gui.playview.PlayViewHeader;
import me.theentropyshard.teslauncher.minecraft.auth.microsoft.AuthListener;
import me.theentropyshard.teslauncher.minecraft.auth.microsoft.MicrosoftAuthenticator;
import me.theentropyshard.teslauncher.minecraft.auth.microsoft.MinecraftProfile;
import me.theentropyshard.teslauncher.utils.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

public class AccountsView extends View {
    public AccountsView() {
        JPanel root = this.getRoot();

        JLabel noticeLabel = new JLabel(
                // @formatter:off
                "<html>" +
                    "<s><strong>Notice</strong>: Only offline accounts supported for now</s>" +
                "</html>"
                // @formatter:on
        );
        noticeLabel.setHorizontalAlignment(JLabel.CENTER);
        noticeLabel.setFont(noticeLabel.getFont().deriveFont(14.0f));
        root.add(noticeLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();

        JTextField usernameField = new JTextField();
        Dimension size = usernameField.getPreferredSize();
        usernameField.setPreferredSize(new Dimension(250, size.height));
        usernameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Your nickname. Default is Player");
        centerPanel.add(usernameField);

        JButton addButton = new JButton("Add");
        addButton.addActionListener(e -> {
            String nickname = usernameField.getText();
            PlayViewHeader.instance.getAccounts().addItem(new OfflineAccount(nickname));
            TESLauncher.getInstance().getAccountsManager().saveAccount(new OfflineAccount(nickname));
            usernameField.setText("");
        });
        centerPanel.add(addButton);

        JButton addMicrosoft = new JButton("Add Microsoft");
        addMicrosoft.addActionListener(e -> {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    AuthListener authListener = new AuthListener() {
                        @Override
                        public void onUserCodeReceived(String userCode, String verificationUri) {
                            JOptionPane.showMessageDialog(TESLauncher.getInstance().getGui().getAppWindow().getFrame(),
                                    "A web page will be opened now. You will need to paste the\n" +
                                    " code that I already put in your clipboard.", "Info", JOptionPane.INFORMATION_MESSAGE);
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
                        }
                    };

                    MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator(
                            TESLauncher.getInstance().getHttpClient(),
                            authListener
                    );

                    MinecraftProfile profile = authenticator.authenticate();
                    MicrosoftAccount microsoftAccount = new MicrosoftAccount();
                    microsoftAccount.setAccessToken(profile.accessToken);
                    microsoftAccount.setUuid(UUID.fromString(profile.id.replaceFirst(
                            "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"
                    )));
                    microsoftAccount.setUsername(profile.name);
                    TESLauncher.getInstance().getAccountsManager().saveAccount(microsoftAccount);

                    PlayViewHeader.instance.getAccounts().addItem(microsoftAccount);

                    return null;
                }
            }.execute();
        });
        centerPanel.add(addMicrosoft);

        root.add(centerPanel, BorderLayout.CENTER);
    }
}
