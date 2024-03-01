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

package me.theentropyshard.teslauncher.gui.dialogs;

import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.minecraft.MinecraftDownloader;
import me.theentropyshard.teslauncher.minecraft.VersionManifest;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class McVersionsTableModel extends DefaultTableModel {
    private final DateTimeFormatter formatter;

    public McVersionsTableModel(AddInstanceDialog dialog, JTable table) {
        super(
                new Object[][]{},
                new Object[]{"Version", "Date released", "Type"}
        );

        if (Locale.getDefault().getLanguage().equalsIgnoreCase("en")) {
            this.formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        } else /*if (Locale.getDefault().getLanguage().equalsIgnoreCase("ru"))*/ {
            this.formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        }

        new SwingWorker<VersionManifest, Void>() {
            @Override
            protected VersionManifest doInBackground() throws Exception {
                return MinecraftDownloader.getVersionManifest(TESLauncher.getInstance().getVersionsDir());
            }

            @Override
            protected void done() {
                try {
                    VersionManifest versionManifest = this.get();
                    for (VersionManifest.Version version : versionManifest.versions) {
                        McVersionsTableModel.this.addRow(new Object[]{
                                version.id,
                                McVersionsTableModel.this.formatter.format(OffsetDateTime.parse(version.releaseTime)),
                                version.type
                        });
                    }


                    TableRowSorter<McVersionsTableModel> rowSorter = new TableRowSorter<>(McVersionsTableModel.this);

                    JCheckBox releasesBox = dialog.getReleasesBox();
                    releasesBox.addActionListener(e -> rowSorter.sort());
                    JCheckBox snapshotsBox = dialog.getSnapshotsBox();
                    snapshotsBox.addActionListener(e -> rowSorter.sort());
                    JCheckBox betasBox = dialog.getBetasBox();
                    betasBox.addActionListener(e -> rowSorter.sort());
                    JCheckBox alphasBox = dialog.getAlphasBox();
                    alphasBox.addActionListener(e -> rowSorter.sort());
                    rowSorter.setRowFilter(RowFilter.orFilter(Arrays.asList(
                            new RowFilter<McVersionsTableModel, Integer>() {
                                @Override
                                public boolean include(Entry<? extends McVersionsTableModel, ? extends Integer> entry) {
                                    return releasesBox.isSelected() && entry.getStringValue(2).equals("release");
                                }
                            },
                            new RowFilter<McVersionsTableModel, Integer>() {
                                @Override
                                public boolean include(Entry<? extends McVersionsTableModel, ? extends Integer> entry) {
                                    return snapshotsBox.isSelected() && entry.getStringValue(2).equals("snapshot");
                                }
                            },
                            new RowFilter<McVersionsTableModel, Integer>() {
                                @Override
                                public boolean include(Entry<? extends McVersionsTableModel, ? extends Integer> entry) {
                                    return betasBox.isSelected() && entry.getStringValue(2).equals("old_beta");
                                }
                            },
                            new RowFilter<McVersionsTableModel, Integer>() {
                                @Override
                                public boolean include(Entry<? extends McVersionsTableModel, ? extends Integer> entry) {
                                    return alphasBox.isSelected() && entry.getStringValue(2).equals("old_alpha");
                                }
                            }
                    )));

                    table.setRowSorter(rowSorter);
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
                dialog.getAddButton().setEnabled(true);
            }
        }.execute();
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
