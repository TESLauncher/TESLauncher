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
import me.theentropyshard.teslauncher.gui.playview.PlayViewHeader;

import javax.swing.*;
import java.awt.*;

public class AccountsView extends View {
    public AccountsView() {
        JPanel root = this.getRoot();

        JLabel noticeLabel = new JLabel(
                // @formatter:off
                "<html>" +
                    "<strong>Notice</strong>: Only offline accounts supported for now" +
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
            PlayViewHeader.instance.getAccounts().addItem(nickname);
            TESLauncher.getInstance().getAccountsManager().saveAccount(nickname);
            usernameField.setText("");
        });
        centerPanel.add(addButton);

        root.add(centerPanel, BorderLayout.CENTER);
    }
}
