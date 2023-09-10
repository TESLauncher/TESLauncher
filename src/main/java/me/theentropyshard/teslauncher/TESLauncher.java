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

package me.theentropyshard.teslauncher;

import com.formdev.flatlaf.FlatIntelliJLaf;
import me.theentropyshard.teslauncher.gui.*;

import javax.swing.*;

public class TESLauncher {
    public static final String TITLE = "TESLauncher";
    public static final int WIDTH = 960;
    public static final int HEIGHT = 540;

    public TESLauncher(String[] args) {

        SwingUtilities.invokeLater(() -> {
            JDialog.setDefaultLookAndFeelDecorated(true);
            JFrame.setDefaultLookAndFeelDecorated(true);
            FlatIntelliJLaf.setup();

            JTabbedPane viewSelector = new JTabbedPane(JTabbedPane.LEFT);
            viewSelector.addTab("Play", new PlayView().getRoot());
            viewSelector.addTab("Accounts", new AccountsView().getRoot());
            viewSelector.addTab("Settings", new SettingsView().getRoot());
            viewSelector.addTab("About", new AboutView().getRoot());

            AppWindow appWindow = new AppWindow(TESLauncher.TITLE, TESLauncher.WIDTH, TESLauncher.HEIGHT, viewSelector);
            appWindow.setVisible(true);
        });
    }
}
