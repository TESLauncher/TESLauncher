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

package me.theentropyshard.teslauncher.gui.views.play;

import me.theentropyshard.teslauncher.gui.View;

import javax.swing.*;
import java.awt.*;

public class PlayView extends View {
    private final JComboBox<String> groups;
    private final JComboBox<String> accounts;
    private final JPanel instancePanelsView;
    private final CardLayout cardLayout;
    private final JProgressBar progressBar;

    public PlayView() {
        this.groups = new JComboBox<>();
        this.accounts = new JComboBox<>();
        this.instancePanelsView = new JPanel();
        this.cardLayout = new CardLayout();
        this.instancePanelsView.setLayout(this.cardLayout);
        this.progressBar = new JProgressBar();
        this.progressBar.setStringPainted(true);
        this.progressBar.setVisible(false);

        JPanel leftSide = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftSide.add(this.groups);

        JPanel rightSide = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightSide.add(this.accounts);

        JPanel header = new JPanel(new BorderLayout());
        header.add(leftSide, BorderLayout.WEST);
        header.add(rightSide, BorderLayout.EAST);

        JPanel root = this.getRoot();
        root.add(header, BorderLayout.NORTH);
        root.add(this.instancePanelsView, BorderLayout.CENTER);
        root.add(this.progressBar, BorderLayout.SOUTH);
    }

    public JComboBox<String> getGroups() {
        return this.groups;
    }

    public JComboBox<String> getAccounts() {
        return this.accounts;
    }

    public JPanel getInstancePanelsView() {
        return this.instancePanelsView;
    }

    public CardLayout getCardLayout() {
        return this.cardLayout;
    }

    public JProgressBar getProgressBar() {
        return this.progressBar;
    }
}
