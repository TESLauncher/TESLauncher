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

package me.theentropyshard.teslauncher.gui.components;

import com.formdev.flatlaf.ui.FlatTabbedPaneUI;

import javax.swing.*;
import java.awt.*;

public class MyTabbedPane extends JTabbedPane {
    public MyTabbedPane() {

    }

    public MyTabbedPane(int tabPlacement) {
        super(tabPlacement);
    }

    public MyTabbedPane(int tabPlacement, int tabLayoutPolicy) {
        super(tabPlacement, tabLayoutPolicy);
    }

    @Override
    public void updateUI() {
        super.updateUI();

        this.setUI(
            new FlatTabbedPaneUI() {
                @Override
                protected Color getTabBackground(int tabPlacement, int tabIndex, boolean isSelected) {
                    return isSelected ? this.focusColor : super.getTabBackground(tabPlacement, tabIndex, false);
                }
            }
        );
    }
}
