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

package me.theentropyshard.teslauncher.gui.dialogs.instancesettings;

import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.instance.Instance;
import me.theentropyshard.teslauncher.instance.JarMod;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JarModsTab extends Tab {
    public JarModsTab(Instance instance, JDialog dialog) {
        super("Jar Mods", instance, dialog);

        JPanel root = this.getRoot();
        JButton addJarMod = new JButton("Add jar mod");
        root.add(addJarMod);

        addJarMod.addActionListener(e -> {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setFileFilter(new FileNameExtensionFilter("Archives", "zip", "jar"));

                    int option = fileChooser.showOpenDialog(TESLauncher.window.getFrame());
                    if (option == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = fileChooser.getSelectedFile();
                        if (selectedFile == null) {
                            return null;
                        }

                        List<JarMod> jarMods = instance.getJarMods();
                        if (jarMods == null) {
                            jarMods = new ArrayList<>();
                            instance.setJarMods(jarMods);
                        }

                        Path jarModPath = selectedFile.toPath().toAbsolutePath().normalize();
                        jarMods.add(new JarMod(
                                true,
                                jarModPath.toString(),
                                UUID.randomUUID(),
                                jarModPath.getFileName().toString()
                        ));
                    }

                    return null;
                }
            }.execute();
        });
    }

    @Override
    public void save() throws IOException {

    }
}
