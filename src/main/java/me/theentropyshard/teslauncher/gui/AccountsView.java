/*
 * Copyright 2023 TheEntropyShard (https://github.com/TheEntropyShard)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
            //TESLauncher.getInstance().getAccountsManager().saveAccount(nickname);
            usernameField.setText("");
        });
        centerPanel.add(addButton);

        root.add(centerPanel, BorderLayout.CENTER);
    }
}
