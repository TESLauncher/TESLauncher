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

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.gui.playview.PlayView;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;

public class Gui {
    private JTabbedPane viewSelector;
    private AppWindow appWindow;
    private PlayView playView;
    private boolean darkTheme;

    public Gui(boolean darkTheme) {
        this.darkTheme = darkTheme;
        this.initGui();
    }

    public void initGui() {
        if (this.darkTheme) {
            UIManager.put("InstanceItem.defaultColor", new ColorUIResource(64, 75, 93));
            UIManager.put("InstanceItem.hoveredColor", new ColorUIResource(70, 80, 100));
            UIManager.put("InstanceItem.pressedColor", new ColorUIResource(60, 70, 86));

            UIManager.put("ProgressBar.selectionBackground", Color.WHITE);
            UIManager.put("ProgressBar.selectionForeground", Color.WHITE);

            FlatDarculaLaf.setup();
        } else {
            UIManager.put("InstanceItem.defaultColor", new ColorUIResource(222, 230, 237));
            UIManager.put("InstanceItem.hoveredColor", new ColorUIResource(224, 234, 244));
            UIManager.put("InstanceItem.pressedColor", new ColorUIResource(216, 224, 240));

            UIManager.put("ProgressBar.selectionBackground", Color.BLACK);
            UIManager.put("ProgressBar.selectionForeground", Color.BLACK);

            FlatIntelliJLaf.setup();
        }

        JDialog.setDefaultLookAndFeelDecorated(true);
        JFrame.setDefaultLookAndFeelDecorated(true);

        this.viewSelector = new JTabbedPane(JTabbedPane.LEFT);
        TESLauncher.window = this.appWindow = new AppWindow(TESLauncher.TITLE, TESLauncher.WIDTH, TESLauncher.HEIGHT, this.viewSelector);
    }

    public void showGui() {
        SwingUtilities.invokeLater(() -> {
            this.playView = new PlayView();
            if (this.darkTheme) {
                this.playView.getProgressBar().setForeground(new Color(64, 75, 93));
            } else {
                this.playView.getProgressBar().setForeground(new Color(222, 230, 237));
            }

            this.viewSelector.addTab("Play", this.playView.getRoot());
            this.viewSelector.addTab("Accounts", new AccountsView().getRoot());
            this.viewSelector.addTab("Settings", new SettingsView().getRoot());
            this.viewSelector.addTab("About", new AboutView().getRoot());

            this.appWindow.setVisible(true);
        });
    }

    public AppWindow getAppWindow() {
        return this.appWindow;
    }

    public boolean isDarkTheme() {
        return this.darkTheme;
    }

    public void setDarkTheme(boolean darkTheme) {
        this.darkTheme = darkTheme;
    }

    public PlayView getPlayView() {
        return this.playView;
    }
}
