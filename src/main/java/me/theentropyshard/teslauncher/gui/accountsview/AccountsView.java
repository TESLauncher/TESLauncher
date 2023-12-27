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

package me.theentropyshard.teslauncher.gui.accountsview;

import me.theentropyshard.teslauncher.gui.View;
import me.theentropyshard.teslauncher.utils.ResourceUtils;
import me.theentropyshard.teslauncher.utils.SwingUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;

public class AccountsView extends View {
    private final JPanel panel;
    private final JScrollPane scrollPane;
    private final AccountItemGroup group;

    private final AddAccountItem addAccountItem;

    public AccountsView() {
        JPanel root = this.getRoot();

        this.group = new AccountItemGroup();

        this.panel = new JPanel(new GridLayout(0, 1, 0, 1));
        this.panel.setBorder(new EmptyBorder(4, 4, 4, 4));
        JPanel borderPanel = new JPanel(new BorderLayout());
        borderPanel.add(this.panel, BorderLayout.PAGE_START);

        this.scrollPane = new JScrollPane(borderPanel);
        this.scrollPane.setBorder(null);
        root.add(this.scrollPane, BorderLayout.CENTER);

        this.addAccountItem = new AddAccountItem();
        this.panel.add(this.addAccountItem);

        Icon icon = null;
        try {
            icon = SwingUtils.loadIconFromBase64(ResourceUtils.readToString("/steve_head_32_base64.txt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.addAccountItem(new AccountItem("petya", icon));

        /*JLabel noticeLabel = new JLabel(
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
                    AuthListener authListener = (userCode, verificationUri) -> {
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

                    TESLauncher.getInstance().getAccountsManager().saveAccount(microsoftAccount);

                    PlayViewHeader.instance.getAccounts().addItem(microsoftAccount);

                    return null;
                }
            }.execute();
        });
        centerPanel.add(addMicrosoft);

        root.add(centerPanel, BorderLayout.CENTER);*/
    }

    public void addAccountItem(JComponent item) {
        if (!((item instanceof AccountItem) || (item instanceof AddAccountItem))) {
            throw new IllegalArgumentException(String.valueOf(item));
        }

        if (item instanceof AccountItem) {
            this.group.addAccountItem((AccountItem) item);
        }

        int count = this.panel.getComponentCount();
        this.panel.add(item, count - 1);
        this.panel.revalidate();
    }

    public void removeAccountItem(JComponent item) {
        if (!((item instanceof AccountItem) || (item instanceof AddAccountItem))) {
            throw new IllegalArgumentException(String.valueOf(item));
        }

        if (item instanceof AccountItem) {
            this.group.removeAccountItem((AccountItem) item);
        }

        this.panel.remove(item);
        this.panel.revalidate();
    }

    public JScrollPane getScrollPane() {
        return this.scrollPane;
    }

    public JPanel getPanel() {
        return this.panel;
    }
}
