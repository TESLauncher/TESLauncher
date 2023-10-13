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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.theentropyshard.teslauncher.utils.Http;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class McVersionsTableModel extends DefaultTableModel {

    public static final String VM_V2 = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";

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

        new SwingWorker<List<List<String>>, Void>() {
            @Override
            protected List<List<String>> doInBackground() throws Exception {
                List<List<String>> data = new ArrayList<>();

                byte[] bytes = Http.get(McVersionsTableModel.VM_V2);
                Gson gson = new Gson();
                JsonObject json = gson.fromJson(new String(bytes, StandardCharsets.UTF_8), JsonObject.class);
                JsonArray jsonArray = json.getAsJsonArray("versions");
                for (JsonElement element : jsonArray) {
                    JsonObject versionObject = element.getAsJsonObject();
                    data.add(Arrays.asList(
                            versionObject.get("id").getAsString(),
                            versionObject.get("releaseTime").getAsString(),
                            versionObject.get("type").getAsString()
                    ));
                }

                return data;
            }

            @Override
            protected void done() {
                try {
                    List<List<String>> objects = this.get();
                    for (List<String> rowData : objects) {
                        McVersionsTableModel.this.addRow(new Object[]{
                                rowData.get(0),
                                McVersionsTableModel.this.formatter.format(OffsetDateTime.parse(rowData.get(1))),
                                rowData.get(2)
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
