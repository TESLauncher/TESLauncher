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

import javax.swing.*;
import java.awt.*;

public class OpenBrowserDialog extends AppDialog {
    public OpenBrowserDialog(String userCode, String verificationUri) {
        super(TESLauncher.window.getFrame(), "Open your browser");

        JPanel root = new JPanel(new BorderLayout());

        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setEditable(false);
        textPane.setText("Please open your browser and go to <a href=\"" + verificationUri
                + "\">" + verificationUri + "</a>, <br> paste the code <b>" + userCode + "</b> and follow further instructions.");

        root.add(textPane, BorderLayout.CENTER);

        this.setResizable(false);
        this.setContent(root);
        this.center(0);
        this.setVisible(true);
    }
}
