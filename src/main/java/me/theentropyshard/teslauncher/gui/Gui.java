/*
 * TESLauncher - https://github.com/TESLauncher/TESLauncher
 * Copyright (C) 2023-2024 TESLauncher
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
import me.theentropyshard.teslauncher.gui.components.InstanceItem;
import me.theentropyshard.teslauncher.gui.utils.SwingUtils;
import me.theentropyshard.teslauncher.gui.view.AboutView;
import me.theentropyshard.teslauncher.gui.view.SettingsView;
import me.theentropyshard.teslauncher.gui.view.accountsview.AccountItem;
import me.theentropyshard.teslauncher.gui.view.accountsview.AccountsView;
import me.theentropyshard.teslauncher.gui.view.accountsview.AddAccountItem;
import me.theentropyshard.teslauncher.gui.view.playview.InstancesPanel;
import me.theentropyshard.teslauncher.gui.view.playview.PlayView;
import me.theentropyshard.teslauncher.utils.OperatingSystem;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Gui {
    private final JTabbedPane viewSelector;
    private final JFrame frame;
    private final JPanel bottomPanel;

    private PlayView playView;
    private AccountsView accountsView;

    private boolean darkTheme;
    private boolean initialized;

    private boolean consoleOpen;

    public Gui(String title, boolean darkTheme) {
        this.darkTheme = darkTheme;

        this.switchTheme();

        JDialog.setDefaultLookAndFeelDecorated(true);
        JFrame.setDefaultLookAndFeelDecorated(true);

        this.viewSelector = new JTabbedPane(JTabbedPane.LEFT);

        TESLauncher.frame = this.frame = new JFrame(title);
        this.frame.add(this.viewSelector, BorderLayout.CENTER);

        this.bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 5));
        this.bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Component.borderColor")));

        JButton openFolderButton = new JButton("Open launcher folder");
        openFolderButton.addActionListener(e -> {
            OperatingSystem.open(TESLauncher.getInstance().getWorkDir());
        });
        this.bottomPanel.add(openFolderButton);

        LauncherConsole console = new LauncherConsole();
        LauncherConsole.instance = console;

        JButton consoleButton = new JButton(this.consoleOpen ? "Hide console" : "Show console");
        consoleButton.addActionListener(e -> {
            this.consoleOpen = !this.consoleOpen;
            consoleButton.setText(this.consoleOpen ? "Hide console" : "Show console");
            console.setVisible(this.consoleOpen);
        });

        console.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Gui.this.consoleOpen = !Gui.this.consoleOpen;
                consoleButton.setText(Gui.this.consoleOpen ? "Hide console" : "Show console");
            }
        });

        this.bottomPanel.add(consoleButton);

        this.frame.add(this.bottomPanel, BorderLayout.SOUTH);

        this.frame.getContentPane().setPreferredSize(new Dimension(TESLauncher.WIDTH, TESLauncher.HEIGHT));
        this.frame.pack();
        SwingUtils.centerWindow(this.frame, 0);
    }

    public void switchTheme() {
        if (this.isDarkTheme()) {
            UIManager.put("InstanceItem.defaultColor", new ColorUIResource(64, 75, 93));
            UIManager.put("InstanceItem.hoveredColor", new ColorUIResource(70, 80, 100));
            UIManager.put("InstanceItem.pressedColor", new ColorUIResource(60, 70, 86));

            UIManager.put("ProgressBar.selectionBackground", Color.WHITE);
            UIManager.put("ProgressBar.selectionForeground", Color.WHITE);
            ColorUIResource accentColor = new ColorUIResource(Color.decode("#4B6EAF"));
            UIManager.put("ProgressBar.foreground", accentColor);
            UIManager.put("ProgressBar.background", new ColorUIResource(64, 75, 93));

            UIManager.put("AccountItem.borderColor", accentColor);

            UIManager.put("LauncherConsole.infoColor", new ColorUIResource(Color.WHITE));
            UIManager.put("LauncherConsole.warnColor", new ColorUIResource(Color.YELLOW.darker()));
            UIManager.put("LauncherConsole.errorColor", new ColorUIResource(Color.RED.darker()));
            UIManager.put("LauncherConsole.debugColor", new ColorUIResource(Color.CYAN.darker()));

            FlatDarculaLaf.setup();
        } else {
            UIManager.put("InstanceItem.defaultColor", new ColorUIResource(222, 230, 237));
            UIManager.put("InstanceItem.hoveredColor", new ColorUIResource(224, 234, 244));
            UIManager.put("InstanceItem.pressedColor", new ColorUIResource(216, 224, 240));

            UIManager.put("ProgressBar.selectionBackground", Color.BLACK);
            UIManager.put("ProgressBar.selectionForeground", Color.WHITE);
            ColorUIResource accentColor = new ColorUIResource(Color.decode("#2675BF"));
            UIManager.put("ProgressBar.foreground", accentColor);
            UIManager.put("ProgressBar.background", new ColorUIResource(222, 230, 237));

            UIManager.put("AccountItem.borderColor", accentColor);

            UIManager.put("LauncherConsole.infoColor", new ColorUIResource(Color.BLACK));
            UIManager.put("LauncherConsole.warnColor", new ColorUIResource(Color.YELLOW.darker()));
            UIManager.put("LauncherConsole.errorColor", new ColorUIResource(Color.RED.darker()));
            UIManager.put("LauncherConsole.debugColor", new ColorUIResource(Color.CYAN.darker()));

            FlatIntelliJLaf.setup();
        }

        if (!this.initialized) {
            return;
        }

        this.bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Component.borderColor")));

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

        SwingUtilities.updateComponentTreeUI(this.frame);
        this.frame.pack();

        if (LauncherConsole.instance != null) {
            JFrame frame = LauncherConsole.instance.getFrame();
            SwingUtilities.updateComponentTreeUI(frame);
            frame.pack();
        }

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

            this.viewSelector.addTab("Play", this.playView);
            this.viewSelector.addTab("Accounts", this.accountsView);
            this.viewSelector.addTab("Settings", new SettingsView());
            this.viewSelector.addTab("About", new AboutView());

            this.frame.setVisible(true);

            this.initialized = true;
        });
    }

    public JFrame getFrame() {
        return this.frame;
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
