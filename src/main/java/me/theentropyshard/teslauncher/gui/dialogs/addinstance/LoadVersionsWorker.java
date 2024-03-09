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

package me.theentropyshard.teslauncher.gui.dialogs.addinstance;

import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.minecraft.MinecraftDownloader;
import me.theentropyshard.teslauncher.minecraft.VersionManifest;
import me.theentropyshard.teslauncher.minecraft.VersionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class LoadVersionsWorker extends SwingWorker<VersionManifest, Void> {
    private static final Logger LOG = LogManager.getLogger(LoadVersionsWorker.class);

    private final McVersionsTableModel model;
    private final AddInstanceDialog dialog;
    private final JTable table;

    private final DateTimeFormatter formatter;

    public LoadVersionsWorker(McVersionsTableModel model, AddInstanceDialog dialog, JTable table) {
        this.model = model;
        this.dialog = dialog;
        this.table = table;

        if (Locale.getDefault().getLanguage().equalsIgnoreCase("en")) {
            this.formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        } else /*if (Locale.getDefault().getLanguage().equalsIgnoreCase("ru"))*/ {
            this.formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        }
    }

    @Override
    protected VersionManifest doInBackground() throws Exception {
        return MinecraftDownloader.getVersionManifest(TESLauncher.getInstance().getVersionsDir());
    }



    @Override
    protected void done() {
        VersionManifest versionManifest;

        try {
            versionManifest = this.get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error(e);

            return;
        }

        for (VersionManifest.Version version : versionManifest.getVersions()) {
            Object[] rowData = {
                    version.getId(),
                    this.formatter.format(version.getReleaseTime()),
                    version.getType()
            };

            this.model.addRow(rowData);
        }

        TableRowSorter<McVersionsTableModel> rowSorter = new TableRowSorter<>(this.model);

        this.dialog.getReleasesBox().addActionListener(e -> rowSorter.sort());
        this.dialog.getSnapshotsBox().addActionListener(e -> rowSorter.sort());
        this.dialog.getBetasBox().addActionListener(e -> rowSorter.sort());
        this.dialog.getAlphasBox().addActionListener(e -> rowSorter.sort());

        rowSorter.setRowFilter(RowFilter.orFilter(Arrays.asList(
                new VersionTypeRowFilter(this.dialog.getReleasesBox(), VersionType.RELEASE),
                new VersionTypeRowFilter(this.dialog.getSnapshotsBox(), VersionType.SNAPSHOT),
                new VersionTypeRowFilter(this.dialog.getBetasBox(), VersionType.OLD_BETA),
                new VersionTypeRowFilter(this.dialog.getAlphasBox(), VersionType.OLD_ALPHA)
        )));

        this.table.setRowSorter(rowSorter);

        this.dialog.getAddButton().setEnabled(true);
    }
}
