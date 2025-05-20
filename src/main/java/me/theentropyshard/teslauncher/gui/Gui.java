/*
 * TESLauncher - https://github.com/TESLauncher/TESLauncher
 * Copyright (C) 2023-2025 TESLauncher
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

import com.formdev.flatlaf.FlatLaf;
import me.theentropyshard.teslauncher.Settings;
import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.gui.console.LauncherConsole;
import me.theentropyshard.teslauncher.gui.laf.DarkLauncherLaf;
import me.theentropyshard.teslauncher.gui.laf.LightLauncherLaf;
import me.theentropyshard.teslauncher.gui.utils.SvgIcon;
import me.theentropyshard.teslauncher.gui.utils.SwingUtils;
import me.theentropyshard.teslauncher.gui.view.AboutView;
import me.theentropyshard.teslauncher.gui.view.SettingsView;
import me.theentropyshard.teslauncher.gui.view.accountsview.AccountItem;
import me.theentropyshard.teslauncher.gui.view.accountsview.AccountsView;
import me.theentropyshard.teslauncher.gui.view.accountsview.AddAccountItem;
import me.theentropyshard.teslauncher.gui.view.playview.InstancesPanel;
import me.theentropyshard.teslauncher.gui.view.playview.PlayView;
import me.theentropyshard.teslauncher.instance.InstanceManager;
import me.theentropyshard.teslauncher.language.LanguageSection;
import me.theentropyshard.teslauncher.logging.Log;
import me.theentropyshard.teslauncher.utils.OperatingSystem;
import me.theentropyshard.teslauncher.utils.TimeUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class Gui {
    public static final String OPEN_LAUNCHER_FOLDER = "gui.general.openLauncherFolder";
    public static final String CONSOLE_BUTTON_SHOW = "gui.general.consoleButton.show";
    public static final String CONSOLE_BUTTON_HIDE = "gui.general.consoleButton.hide";
    public static final String TAB_PLAY = "gui.general.tab.play";
    public static final String TAB_DEVLOG = "gui.general.tab.devlog";
    public static final String TAB_ACCOUNTS = "gui.general.tab.accounts";
    public static final String TAB_SETTINGS = "gui.general.tab.settings";
    public static final String TAB_ABOUT = "gui.general.tab.about";
    private final JTabbedPane viewSelector;
    private final JFrame frame;
    private final JButton openFolderButton;
    private final JButton consoleButton;
    private final JLabel statsLabel;

    private PlayView playView;
    private AccountsView accountsView;
    private SettingsView settingsView;
    private AboutView aboutView;

    private boolean darkTheme;
    private boolean initialized;
    private boolean consoleOpen;

    private String showConsoleText;
    private String hideConsoleText;

    public Gui(String title, boolean darkTheme) {
        this.darkTheme = darkTheme;

        JDialog.setDefaultLookAndFeelDecorated(true);
        JFrame.setDefaultLookAndFeelDecorated(true);

        FlatLaf.registerCustomDefaultsSource("themes");

        this.switchTheme();

        LauncherConsole console = new LauncherConsole();
        LauncherConsole.instance = console;

        Log.start();

        LanguageSection section = TESLauncher.getInstance().getLanguage().getSection("gui.general");

        this.viewSelector = new JTabbedPane(JTabbedPane.LEFT);

        InputMap inputMap = this.viewSelector.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "run_last_played_instance");

        ActionMap actionMap = this.viewSelector.getActionMap();
        actionMap.put("run_last_played_instance", SwingUtils.newAction(e -> {
            int selectedIndex = this.viewSelector.getSelectedIndex();

            if (selectedIndex != 0) {
                return;
            }

            if (this.playView == null) {
                return;
            }

            this.playView.playLastInstance();
        }));

        TESLauncher.frame = this.frame = new JFrame(title);
        /*this.frame.setIconImages(
            List.of(
                SwingUtils.getImage("/assets/images/icons/logo/icon_silver_16x.png"),
                SwingUtils.getImage("/assets/images/icons/logo/icon_silver_32x.png"),
                SwingUtils.getImage("/assets/images/icons/logo/icon_silver_64x.png"),
                SwingUtils.getImage("/assets/images/icons/logo/icon_silver_128x.png")
            )
        );*/
        console.getFrame().setIconImages(this.frame.getIconImages());
        this.frame.add(this.viewSelector, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new MigLayout("fillx, insets 0, gap 5 5", "[left][right]", "[center]")) {
            @Override
            public void updateUI() {
                super.updateUI();

                this.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Component.borderColor")));
            }
        };
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Component.borderColor")));
        this.openFolderButton = new JButton(section.getString("openLauncherFolder"));
        this.openFolderButton.addActionListener(e -> {
            OperatingSystem.open(TESLauncher.getInstance().getWorkDir());
        });

        JPanel leftBottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 5));
        bottomPanel.add(leftBottomPanel);

        JPanel rightBottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(rightBottomPanel);

        String instances = section.getString("statsLabel.instances");
        String time = section.getString("statsLabel.totalPlayedFor");

        int instancesCount = TESLauncher.getInstance().getInstanceManager().getInstancesCount();

        if (instancesCount == 0) {
            this.statsLabel = new JLabel(section.getString("statsLabel.tipIfEmpty"));
        } else {
            this.statsLabel = new JLabel(instances.replace("$$INSTANCES_COUNT$$", String.valueOf(instancesCount)) +
                ", " + time.replace("$$TOTAL_PLAYTIME$$", TimeUtils.getHoursMinutesSecondsLocalized(
                TESLauncher.getInstance().getInstanceManager().getTotalPlaytime())));
        }
        rightBottomPanel.add(this.statsLabel);

        leftBottomPanel.add(this.openFolderButton);

        Settings settings = TESLauncher.getInstance().getSettings();
        if (settings.showConsoleAtStartup) {
            console.setVisible(true);
            this.consoleOpen = true;
        }

        this.showConsoleText = section.getString("consoleButton.show");
        this.hideConsoleText = section.getString("consoleButton.hide");

        this.consoleButton = new JButton(this.consoleOpen ? this.hideConsoleText : this.showConsoleText);
        this.consoleButton.addActionListener(e -> {
            this.consoleOpen = !this.consoleOpen;
            this.consoleButton.setText(this.consoleOpen ? this.hideConsoleText : this.showConsoleText);
            console.setVisible(this.consoleOpen);
        });

        console.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Gui.this.consoleOpen = !Gui.this.consoleOpen;
                Gui.this.consoleButton.setText(Gui.this.consoleOpen ? Gui.this.hideConsoleText : Gui.this.showConsoleText);
            }
        });

        leftBottomPanel.add(this.consoleButton);

        this.frame.add(bottomPanel, BorderLayout.SOUTH);

        this.frame.getContentPane().setPreferredSize(new Dimension(TESLauncher.WIDTH, TESLauncher.HEIGHT));
        this.frame.pack();
        SwingUtils.centerWindow(this.frame, 0);
    }

    public void reloadLanguage() {
        LanguageSection language = TESLauncher.getInstance().getLanguage().getSection("gui.general");

        UIManager.put("OptionPane.yesButtonText", language.getString("yes"));
        UIManager.put("OptionPane.noButtonText", language.getString("no"));
        UIManager.put("OptionPane.okButtonText", language.getString("ok"));
        UIManager.put("OptionPane.cancelButtonText", language.getString("cancel"));

        this.viewSelector.setTitleAt(0, language.getString(Gui.TAB_PLAY));
        this.viewSelector.setTitleAt(1, language.getString(Gui.TAB_DEVLOG));
        this.viewSelector.setTitleAt(2, language.getString(Gui.TAB_ACCOUNTS));
        this.viewSelector.setTitleAt(3, language.getString(Gui.TAB_SETTINGS));
        this.viewSelector.setTitleAt(4, language.getString(Gui.TAB_ABOUT));

        this.openFolderButton.setText(language.getString(Gui.OPEN_LAUNCHER_FOLDER));

        this.showConsoleText = language.getString(Gui.CONSOLE_BUTTON_SHOW);
        this.hideConsoleText = language.getString(Gui.CONSOLE_BUTTON_HIDE);

        this.consoleButton.setText(this.consoleOpen ? this.hideConsoleText : this.showConsoleText);

        this.updateStats();

        LauncherConsole.instance.reloadLanguage();
        this.playView.reloadLanguage();
        this.accountsView.reloadLanguage();
        this.settingsView.reloadLanguage();
        this.aboutView.reloadLanguage();

        SwingUtilities.updateComponentTreeUI(this.frame);
        this.frame.pack();
    }

    public void updateStats() {
        LanguageSection section = TESLauncher.getInstance().getLanguage().getSection("statsLabel");

        InstanceManager instanceManager = TESLauncher.getInstance().getInstanceManager();
        int instancesCount = instanceManager.getInstancesCount();

        String instances = section
            .getString("instances")
            .replace("$$INSTANCES_COUNT$$", String.valueOf(instancesCount));

        String time = section
            .getString("totalPlayedFor")
            .replace("$$TOTAL_PLAYTIME$$", TimeUtils.getHoursMinutesSecondsLocalized(instanceManager.getTotalPlaytime()));


        if (instancesCount == 0) {
            this.statsLabel.setText(section.getString("tipIfEmpty"));
        } else {
            this.statsLabel.setText(instances + ", " + time);
        }
    }

    public void switchTheme() {
        SvgIcon.clear();

        if (this.isDarkTheme()) {
            DarkLauncherLaf.setup();
        } else {
            LightLauncherLaf.setup();
        }

        if (!this.initialized) {
            return;
        }

        InstancesPanel defaultInstancesPanel = this.playView.getDefaultInstancesPanel();

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
            this.settingsView = new SettingsView();
            this.aboutView = new AboutView();

            LanguageSection section = TESLauncher.getInstance().getLanguage().getSection("gui.general.tab");

            this.viewSelector.addTab(section.getString("play"), this.playView);
            this.viewSelector.addTab(section.getString("accounts"), this.accountsView);
            this.viewSelector.addTab(section.getString("settings"), this.settingsView);
            this.viewSelector.addTab(section.getString("about"), this.aboutView);

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
