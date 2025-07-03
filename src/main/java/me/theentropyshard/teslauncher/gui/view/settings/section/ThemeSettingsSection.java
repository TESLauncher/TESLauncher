package me.theentropyshard.teslauncher.gui.view.settings.section;

import javax.swing.*;
import java.awt.*;

import me.theentropyshard.teslauncher.Settings;
import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.gui.Gui;
import me.theentropyshard.teslauncher.language.Language;

public class ThemeSettingsSection extends SettingsSection {
    private final JRadioButton darkThemeButton;
    private final JRadioButton lightThemeButton;

    public ThemeSettingsSection() {
        super("Theme", new GridLayout(0, 1));

        this.darkThemeButton = new JRadioButton("Dark");
        this.add(this.darkThemeButton);
        this.darkThemeButton.addActionListener(e -> {
            TESLauncher launcher = TESLauncher.getInstance();

            launcher.getSettings().darkTheme = true;

            Gui gui = launcher.getGui();
            gui.setDarkTheme(launcher.getSettings().darkTheme);
            gui.updateLookAndFeel();
        });

        this.lightThemeButton = new JRadioButton("Light");
        this.add(this.lightThemeButton);
        this.lightThemeButton.addActionListener(e -> {
            TESLauncher launcher = TESLauncher.getInstance();

            launcher.getSettings().darkTheme = false;

            Gui gui = launcher.getGui();
            gui.setDarkTheme(launcher.getSettings().darkTheme);
            gui.updateLookAndFeel();
        });

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(this.darkThemeButton);
        buttonGroup.add(this.lightThemeButton);

        Settings settings = TESLauncher.getInstance().getSettings();
        this.darkThemeButton.setSelected(settings.darkTheme);
        this.lightThemeButton.setSelected(!settings.darkTheme);
    }

    @Override
    public void updateLanguage(Language language) {

    }
}
