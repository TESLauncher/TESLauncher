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

package me.theentropyshard.teslauncher.gui.action;

import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.gui.utils.MessageBox;
import me.theentropyshard.teslauncher.gui.utils.Worker;
import me.theentropyshard.teslauncher.language.LanguageSection;
import me.theentropyshard.teslauncher.logging.Log;
import me.theentropyshard.teslauncher.minecraft.MinecraftInstance;
import net.lingala.zip4j.ZipFile;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

public class InstanceExportAction extends AbstractAction {
    private final MinecraftInstance instance;

    public InstanceExportAction(MinecraftInstance instance) {
        super(TESLauncher.getInstance().getLanguage().getString("gui.instanceSettingsDialog.exportInstance.exportButton"));

        this.instance = instance;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        LanguageSection section = TESLauncher.getInstance().getLanguage().getSection("gui.instanceSettingsDialog.exportInstance");

        new Worker<Void, Void>("exporting instance") {
            @Override
            protected Void work() {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setFileFilter(new FileNameExtensionFilter(section.getString("tesliFiles"), "tesli"));

                File instanceDir = InstanceExportAction.this.instance.getWorkDir().toFile();
                File saveAs = new File(instanceDir, instanceDir.getName() + ".tesli");
                fileChooser.setSelectedFile(saveAs);

                int option = fileChooser.showSaveDialog(TESLauncher.frame);
                if (option != JFileChooser.APPROVE_OPTION) {
                    return null;
                }

                saveAs = fileChooser.getSelectedFile();

                if (saveAs == null) {
                    return null;
                }

                TESLauncher.getInstance().getSettings().lastDir = saveAs.toPath().toAbsolutePath().getParent().toString();

                if (!saveAs.getName().endsWith(".tesli")) {
                    MessageBox.showErrorMessage(TESLauncher.frame, section.getString("wrongExtension"));

                    return null;
                }

                try (ZipFile zipFile = new ZipFile(saveAs)) {
                    zipFile.addFolder(instanceDir);
                } catch (IOException e) {
                    MessageBox.showErrorMessage(TESLauncher.frame, section.getString("failure") + ": " + e.getMessage());
                    Log.error("Could not export instance " + InstanceExportAction.this.instance.getName(), e);

                    return null;
                }

                MessageBox.showPlainMessage(TESLauncher.frame, section.getString("title"), section.getString("success"));

                return null;
            }
        }.execute();
    }
}