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

package me.theentropyshard.teslauncher.gui.console;

import javax.swing.*;
import java.awt.*;

// https://stackoverflow.com/a/47820880/19857533
public class NoWrapJTextPane extends JTextPane {
    @Override
    public boolean getScrollableTracksViewportWidth() {
        // Only track viewport width when the viewport is wider than the preferred width

        return this.getUI().getPreferredSize(this).width <= this.getParent().getSize().width;
    }

    @Override
    public Dimension getPreferredSize() {
        // Avoid substituting the minimum width for the preferred width when the viewport is too narrow

        return this.getUI().getPreferredSize(this);
    }
}
