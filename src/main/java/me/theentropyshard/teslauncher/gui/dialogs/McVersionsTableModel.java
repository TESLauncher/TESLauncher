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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class McVersionsTableModel extends DefaultTableModel {

    public static final String VM_V2 = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";

    public McVersionsTableModel(AddInstanceDialog dialog, JTable table) {
        super(
                new Object[][]{},
                new Object[]{"Version", "Date released", "Type"}
        );

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
                                rowData.get(1),
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
