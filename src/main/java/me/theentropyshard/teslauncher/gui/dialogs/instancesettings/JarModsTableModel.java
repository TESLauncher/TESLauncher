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

import me.theentropyshard.teslauncher.instance.Instance;
import me.theentropyshard.teslauncher.instance.JarMod;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class JarModsTableModel extends AbstractTableModel {
    private static final String[] COLUMN_NAMES = {"Name", "Active"};
    private static final Class<?>[] COLUMN_CLASSES = {String.class, Boolean.class};

    private final List<JarMod> jarMods;

    public JarModsTableModel(Instance instance) {
        if (instance.getJarMods() == null) {
            this.jarMods = new ArrayList<>();
        } else {
            this.jarMods = instance.getJarMods();
        }
    }

    @Override
    public int getRowCount() {
        return this.jarMods.size();
    }

    @Override
    public int getColumnCount() {
        return JarModsTableModel.COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int column) {
        return JarModsTableModel.COLUMN_NAMES[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return JarModsTableModel.COLUMN_CLASSES[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        JarMod jarMod = this.jarMods.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return jarMod.getName();
            case 1:
                return jarMod.isActive();
        }
        return null;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex != 1) {
            return;
        }

        if (!(aValue instanceof Boolean)) {
            return;
        }

        boolean isSelected = (Boolean) aValue;
        this.jarModAt(rowIndex).setActive(isSelected);

        this.fireTableCellUpdated(rowIndex, columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1;
    }

    public void add(JarMod jarMod) {
        int index = this.jarMods.size();
        this.jarMods.add(jarMod);
        this.fireTableRowsInserted(index, index);
    }

    public JarMod jarModAt(int rowIndex) {
        return this.jarMods.get(rowIndex);
    }

    public void removeRow(int rowIndex) {
        this.jarMods.remove(rowIndex);
        this.fireTableStructureChanged();
    }
}
