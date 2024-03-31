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

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class McVersionsTableModel extends DefaultTableModel {
    private final AddInstanceDialog dialog;
    private final JTable table;

    public McVersionsTableModel(AddInstanceDialog dialog, JTable table) {
        super(new Object[][]{}, new Object[]{"Version", "Date released", "Type"});

        this.dialog = dialog;
        this.table = table;

        this.load(false);
    }

    public void load(boolean forceNetwork) {
        new LoadVersionsWorker(this, this.dialog, this.table, forceNetwork).execute();
    }

    public void reload(boolean forceNetwork) {
        int rowCount = this.getRowCount();

        this.dataVector.clear();
        this.fireTableRowsDeleted(0, rowCount - 1);

        this.load(forceNetwork);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
