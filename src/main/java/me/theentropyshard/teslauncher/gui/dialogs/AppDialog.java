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

package me.theentropyshard.teslauncher.gui.dialogs;

import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.gui.utils.SwingUtils;

import javax.swing.*;
import java.awt.*;

public abstract class AppDialog {
    private final JDialog dialog;

    public AppDialog(JFrame owner, String title) {
        this.dialog = new JDialog(owner, title, true);
        this.dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    public void center(int screen) {
        if (TESLauncher.getInstance().getSettings().dialogRelativeToParent) {
            this.dialog.setLocationRelativeTo(TESLauncher.frame);
        } else {
            SwingUtils.centerWindow(this.dialog, screen);
        }
    }

    public void setVisible(boolean visible) {
        this.dialog.setVisible(visible);
    }

    public void setResizable(boolean resizable) {
        this.dialog.setResizable(resizable);
    }

    public void setContent(Component content) {
        Container contentPane = this.dialog.getContentPane();
        contentPane.removeAll();
        contentPane.add(content, BorderLayout.CENTER);
        this.dialog.pack();
    }

    public JDialog getDialog() {
        return this.dialog;
    }
}
