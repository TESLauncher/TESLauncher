/*
 * TESLauncher - https://github.com/TESLauncher/TESLauncher
 * Copyright (C) 2023-2024 TESLauncher
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
import me.theentropyshard.teslauncher.utils.SwingUtils;
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
    private final boolean forceNetwork;

    private final DateTimeFormatter formatter;

    public LoadVersionsWorker(McVersionsTableModel model, AddInstanceDialog dialog, JTable table, boolean forceNetwork) {
        this.model = model;
        this.dialog = dialog;
        this.table = table;
        this.forceNetwork = forceNetwork;

        if (Locale.getDefault().getLanguage().equalsIgnoreCase("en")) {
            this.formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        } else /*if (Locale.getDefault().getLanguage().equalsIgnoreCase("ru"))*/ {
            this.formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        }
    }

    @Override
    protected VersionManifest doInBackground() throws Exception {
        return MinecraftDownloader.getVersionManifest(TESLauncher.getInstance().getVersionsDir(), this.forceNetwork);
    }

    @Override
    protected void done() {
        VersionManifest versionManifest;

        try {
            versionManifest = this.get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Unexpected error", e);

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

        JCheckBox releasesBox = this.dialog.getReleasesBox();
        SwingUtils.removeActionListeners(releasesBox);
        releasesBox.addActionListener(e -> rowSorter.sort());

        JCheckBox snapshotsBox = this.dialog.getSnapshotsBox();
        SwingUtils.removeActionListeners(snapshotsBox);
        snapshotsBox.addActionListener(e -> rowSorter.sort());

        JCheckBox betasBox = this.dialog.getBetasBox();
        SwingUtils.removeActionListeners(betasBox);
        betasBox.addActionListener(e -> rowSorter.sort());

        JCheckBox alphasBox = this.dialog.getAlphasBox();
        SwingUtils.removeActionListeners(alphasBox);
        alphasBox.addActionListener(e -> rowSorter.sort());

        rowSorter.setRowFilter(RowFilter.orFilter(Arrays.asList(
                new VersionTypeRowFilter(releasesBox, VersionType.RELEASE),
                new VersionTypeRowFilter(snapshotsBox, VersionType.SNAPSHOT),
                new VersionTypeRowFilter(betasBox, VersionType.OLD_BETA),
                new VersionTypeRowFilter(this.dialog.getAlphasBox(), VersionType.OLD_ALPHA)
        )));

        this.table.setRowSorter(rowSorter);

        this.dialog.getAddButton().setEnabled(true);

        SwingUtils.setJTableColumnsWidth(this.table, 70, 15, 15);
    }
}
