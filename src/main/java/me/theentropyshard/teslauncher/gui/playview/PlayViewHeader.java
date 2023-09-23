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

package me.theentropyshard.teslauncher.gui.playview;

import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.gui.View;

import javax.swing.*;
import java.awt.*;

public class PlayViewHeader extends View {
    private final JComboBox<String> instanceGroups;
    private final JComboBox<String> accounts;

    public static PlayViewHeader instance;

    public PlayViewHeader() {
        PlayViewHeader.instance = this;
        JPanel root = this.getRoot();

        JPanel leftSide = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel instanceLabel = new JLabel("Instance group:");
        leftSide.add(instanceLabel);

        this.instanceGroups = new JComboBox<>();
        leftSide.add(this.instanceGroups);

        JPanel rightSide = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JLabel accountsLabel = new JLabel("Account:");
        rightSide.add(accountsLabel);

        this.accounts = new JComboBox<>(
                new String[]{"Player"}
        );
        rightSide.add(this.accounts);

        root.add(leftSide, BorderLayout.WEST);
        root.add(rightSide, BorderLayout.EAST);
    }

    public JComboBox<String> getInstanceGroups() {
        return this.instanceGroups;
    }

    public JComboBox<String> getAccounts() {
        return this.accounts;
    }
}
