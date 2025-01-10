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

package me.theentropyshard.teslauncher.gui.dialogs.instancesettings.tab;

import me.theentropyshard.teslauncher.Settings;
import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.instance.JarMod;
import me.theentropyshard.teslauncher.minecraft.MinecraftInstance;
import me.theentropyshard.teslauncher.logging.Log;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class JarModsTab extends SettingsTab {
    private final JarModsTableModel jarModsTableModel;
    private final JButton deleteModButton;

    public JarModsTab(MinecraftInstance instance, JDialog dialog) {
        super("Jar Mods", instance, dialog);

        JPanel root = this.getRoot();
        root.setLayout(new BorderLayout());

        JButton addJarMod = new JButton("Add jar mod");
        root.add(addJarMod, BorderLayout.NORTH);

        this.jarModsTableModel = new JarModsTableModel(instance);

        addJarMod.addActionListener(e -> {
            new SwingWorker<JarMod, Void>() {
                @Override
                protected JarMod doInBackground() throws Exception {
                    UIManager.put("FileChooser.readOnly", Boolean.TRUE);
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setFileFilter(new FileNameExtensionFilter("Archives (*.zip, *.jar)", "zip", "jar"));

                    Settings settings = TESLauncher.getInstance().getSettings();
                    if (settings.lastDir != null && !settings.lastDir.isEmpty()) {
                        fileChooser.setCurrentDirectory(new File(settings.lastDir));
                    }

                    int option = fileChooser.showOpenDialog(TESLauncher.frame);
                    if (option == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = fileChooser.getSelectedFile();
                        if (selectedFile == null) {
                            return null;
                        }

                        settings.lastDir = fileChooser.getCurrentDirectory().getAbsolutePath();

                        JarMod jarMod;
                        try {
                            jarMod = instance.addJarMod(selectedFile.toPath());
                        } catch (IOException ex) {
                            Log.error(ex);
                            return null;
                        }

                        return jarMod;
                    }

                    UIManager.put("FileChooser.readOnly", Boolean.FALSE);
                    return null;
                }

                @Override
                protected void done() {
                    JarMod jarMod = null;
                    try {
                        jarMod = this.get();
                    } catch (InterruptedException | ExecutionException ex) {
                        Log.error(ex);
                    }
                    JarModsTab.this.jarModsTableModel.add(jarMod);
                }
            }.execute();
        });

        this.deleteModButton = new JButton("Delete jar mod");

        JTable jarModsTable = new JTable(this.jarModsTableModel);
        jarModsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = jarModsTable.getSelectedRow();
                if (selectedRow == -1) {
                    return;
                }

                JarModsTab.this.deleteModButton.setEnabled(true);
            }
        });

        JScrollPane scrollPane = new JScrollPane(jarModsTable);
        scrollPane.setBorder(null);
        root.add(scrollPane, BorderLayout.CENTER);


        this.deleteModButton.setEnabled(false);
        this.deleteModButton.addActionListener(e -> {
            int selectedRow = jarModsTable.getSelectedRow();
            if (selectedRow == -1) {
                return;
            }
            JarMod jarMod = this.jarModsTableModel.jarModAt(selectedRow);

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        instance.removeJarMod(jarMod.getUuid().toString());
                    } catch (IOException e) {
                        Log.error(e);
                    }

                    return null;
                }

                @Override
                protected void done() {
                    JarModsTab.this.jarModsTableModel.removeRow(selectedRow);
                }
            }.execute();
        });

        root.add(this.deleteModButton, BorderLayout.SOUTH);
    }

    @Override
    public void save() throws IOException {

    }
}
