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

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import me.theentropyshard.teslauncher.gui.views.about.AboutView;
import me.theentropyshard.teslauncher.gui.views.accounts.AccountsView;
import me.theentropyshard.teslauncher.gui.views.play.PlayView;
import me.theentropyshard.teslauncher.gui.views.settings.SettingsView;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Gui {
    private final String title;
    private final int windowWidth;
    private final int windowHeight;

    private final JTabbedPane viewSelector;
    private final PlayView playView;
    private final AccountsView accountsView;
    private final SettingsView settingsView;
    private final AboutView aboutView;

    private final JFrame frame;

    private boolean darkTheme;

    public Gui(String title, int windowWidth, int windowHeight, boolean darkTheme) {
        this.title = title;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;

        this.darkTheme = darkTheme;
        this.installLaF();

        this.viewSelector = new JTabbedPane(JTabbedPane.LEFT);

        this.playView = new PlayView();
        this.viewSelector.addTab("Play", this.playView.getRoot());

        this.accountsView = new AccountsView();
        this.viewSelector.addTab("Accounts", this.accountsView.getRoot());

        this.settingsView = new SettingsView();
        this.viewSelector.addTab("Setting", this.settingsView.getRoot());

        this.aboutView = new AboutView();
        this.viewSelector.add("About", this.aboutView.getRoot());

        this.viewSelector.setPreferredSize(new Dimension(windowWidth, windowHeight));

        this.frame = new JFrame(title);
        this.frame.add(this.viewSelector, BorderLayout.CENTER);
        this.frame.pack();
        this.addWindowClosingListener(() -> System.exit(0));
        this.centerJFrame();
    }

    private void installLaF() {
        JDialog.setDefaultLookAndFeelDecorated(true);
        JFrame.setDefaultLookAndFeelDecorated(true);
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
    }

    private void centerJFrame() {
        int screen = 0;

        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] allDevices = env.getScreenDevices();

        if (screen < 0 || screen >= allDevices.length) {
            screen = 0;
        }

        Rectangle bounds = allDevices[screen].getDefaultConfiguration().getBounds();
        this.frame.setLocation(
                ((bounds.width - this.frame.getWidth())  / 2) + bounds.x,
                ((bounds.height - this.frame.getHeight()) / 2) + bounds.y
        );
    }

    public void addWindowClosingListener(Runnable r) {
        this.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                r.run();
            }
        });
    }

    public void show() {
        this.frame.setVisible(true);
    }

    public void dispose() {
        this.frame.dispose();
    }

    public boolean isDarkTheme() {
        return this.darkTheme;
    }

    public void setDarkTheme(boolean darkTheme) {
        this.darkTheme = darkTheme;
    }

    public String getTitle() {
        return this.title;
    }

    public int getWindowWidth() {
        return this.windowWidth;
    }

    public int getWindowHeight() {
        return this.windowHeight;
    }

    public JTabbedPane getViewSelector() {
        return this.viewSelector;
    }

    public PlayView getPlayView() {
        return this.playView;
    }

    public AccountsView getAccountsView() {
        return this.accountsView;
    }

    public SettingsView getSettingsView() {
        return this.settingsView;
    }

    public AboutView getAboutView() {
        return this.aboutView;
    }

    public JFrame getFrame() {
        return this.frame;
    }
}
