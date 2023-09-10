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

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import me.theentropyshard.teslauncher.gui.AboutView;
import me.theentropyshard.teslauncher.gui.AccountsView;
import me.theentropyshard.teslauncher.gui.AppWindow;
import me.theentropyshard.teslauncher.gui.SettingsView;
import me.theentropyshard.teslauncher.gui.playview.PlayView;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;

public class TESLauncher {
    public static final String TITLE = "TESLauncher";
    public static final int WIDTH = 960;
    public static final int HEIGHT = 540;

    private boolean darkTheme;

    public static AppWindow window;

    public TESLauncher(String[] args) {
        this.darkTheme = false;

        SwingUtilities.invokeLater(() -> {
            if (this.darkTheme) {
                UIManager.put("InstanceItem.defaultColor", new ColorUIResource(64, 75, 93));
                UIManager.put("InstanceItem.hoveredColor", new ColorUIResource(70, 80, 100));
                UIManager.put("InstanceItem.pressedColor", new ColorUIResource(60, 70, 86));

                FlatDarculaLaf.setup();
            } else {
                UIManager.put("InstanceItem.defaultColor", new ColorUIResource(222, 230, 237));
                UIManager.put("InstanceItem.hoveredColor", new ColorUIResource(224, 234, 244));
                UIManager.put("InstanceItem.pressedColor", new ColorUIResource(216, 224, 240));

                FlatIntelliJLaf.setup();
            }

            JDialog.setDefaultLookAndFeelDecorated(true);
            JFrame.setDefaultLookAndFeelDecorated(true);

            JTabbedPane viewSelector = new JTabbedPane(JTabbedPane.LEFT);
            viewSelector.addTab("Play", new PlayView().getRoot());
            viewSelector.addTab("Accounts", new AccountsView().getRoot());
            viewSelector.addTab("Settings", new SettingsView().getRoot());
            viewSelector.addTab("About", new AboutView().getRoot());

            AppWindow appWindow = new AppWindow(TESLauncher.TITLE, TESLauncher.WIDTH, TESLauncher.HEIGHT, viewSelector);
            appWindow.setVisible(true);

            TESLauncher.window = appWindow;
        });
    }
}
