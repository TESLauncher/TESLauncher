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
import me.theentropyshard.teslauncher.gui.accountsview.AccountItem;
import me.theentropyshard.teslauncher.gui.accountsview.AccountsView;
import me.theentropyshard.teslauncher.gui.accountsview.AddAccountItem;
import me.theentropyshard.teslauncher.gui.components.InstanceItem;
import me.theentropyshard.teslauncher.gui.playview.InstancesPanel;
import me.theentropyshard.teslauncher.gui.playview.PlayView;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;

public class Gui {
    private JTabbedPane viewSelector;
    private AppWindow appWindow;
    private PlayView playView;
    private AccountsView accountsView;

    private boolean darkTheme;
    private boolean initialized;

    public Gui(boolean darkTheme) {
        this.darkTheme = darkTheme;
        this.initGui();
    }

    public void initGui() {
        this.switchTheme();

        JDialog.setDefaultLookAndFeelDecorated(true);
        JFrame.setDefaultLookAndFeelDecorated(true);

        this.viewSelector = new JTabbedPane(JTabbedPane.LEFT);
        TESLauncher.window = this.appWindow = new AppWindow(TESLauncher.TITLE, TESLauncher.WIDTH, TESLauncher.HEIGHT, this.viewSelector);
    }

    public void switchTheme() {
        if (this.isDarkTheme()) {
            UIManager.put("InstanceItem.defaultColor", new ColorUIResource(64, 75, 93));
            UIManager.put("InstanceItem.hoveredColor", new ColorUIResource(70, 80, 100));
            UIManager.put("InstanceItem.pressedColor", new ColorUIResource(60, 70, 86));

            UIManager.put("ProgressBar.selectionBackground", Color.WHITE);
            UIManager.put("ProgressBar.selectionForeground", Color.WHITE);
            UIManager.put("ProgressBar.foreground", new ColorUIResource(64, 75, 93));

            UIManager.put("AccountItem.borderColor", new ColorUIResource(Color.decode("#4B6EAF")));

            FlatDarculaLaf.setup();
        } else {
            UIManager.put("InstanceItem.defaultColor", new ColorUIResource(222, 230, 237));
            UIManager.put("InstanceItem.hoveredColor", new ColorUIResource(224, 234, 244));
            UIManager.put("InstanceItem.pressedColor", new ColorUIResource(216, 224, 240));

            UIManager.put("ProgressBar.selectionBackground", Color.BLACK);
            UIManager.put("ProgressBar.selectionForeground", Color.BLACK);
            UIManager.put("ProgressBar.foreground", new ColorUIResource(222, 230, 237));

            UIManager.put("AccountItem.borderColor", new ColorUIResource(Color.decode("#2675BF")));

            FlatIntelliJLaf.setup();
        }

        if (!this.initialized) {
            return;
        }

        InstancesPanel defaultInstancesPanel = this.playView.getDefaultInstancesPanel();

        for (Component component : defaultInstancesPanel.getInstancesPanel().getComponents()) {
            ((InstanceItem) component).updateColors();
        }

        for (Component component : this.accountsView.getPanel().getComponents()) {
            if (component instanceof AccountItem) {
                ((AccountItem) component).updateColors();
            }

            if (component instanceof AddAccountItem) {
                ((AddAccountItem) component).updateColors();
            }
        }

        defaultInstancesPanel.getScrollPane().setBorder(null);
        this.playView.getGroups().values().forEach(instancesPanel -> {

            for (Component component : instancesPanel.getInstancesPanel().getComponents()) {
                ((InstanceItem) component).updateColors();
            }
            instancesPanel.getScrollPane().setBorder(null);
        });
    }

    public void updateLookAndFeel() {
        this.switchTheme();
        JFrame frame = TESLauncher.window.getFrame();
        SwingUtilities.updateComponentTreeUI(frame);
        frame.pack();

        InstancesPanel defaultInstancesPanel = this.playView.getDefaultInstancesPanel();
        defaultInstancesPanel.getScrollPane().setBorder(null);

        this.playView.getGroups().values().forEach(instancesPanel -> {
            instancesPanel.getScrollPane().setBorder(null);
        });

        this.accountsView.getScrollPane().setBorder(null);
    }

    public void showGui() {
        SwingUtilities.invokeLater(() -> {
            this.playView = new PlayView();
            this.accountsView = new AccountsView();

            this.viewSelector.addTab("Play", this.playView.getRoot());
            this.viewSelector.addTab("Accounts", this.accountsView.getRoot());
            this.viewSelector.addTab("Settings", new SettingsView().getRoot());
            this.viewSelector.addTab("About", new AboutView().getRoot());

            this.appWindow.setVisible(true);

            this.initialized = true;
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

    public AccountsView getAccountsView() {
        return this.accountsView;
    }
}
