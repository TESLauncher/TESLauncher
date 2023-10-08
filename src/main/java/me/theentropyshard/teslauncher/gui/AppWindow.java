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

package me.theentropyshard.teslauncher.gui;

import me.theentropyshard.teslauncher.utils.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class AppWindow {
    private final JFrame frame;

    public AppWindow(String title, int width, int height, Component content) {
        this.frame = new JFrame(title);

        Container contentPane = this.frame.getContentPane();
        contentPane.removeAll();
        contentPane.setLayout(new BorderLayout());
        contentPane.setPreferredSize(new Dimension(width, height));
        contentPane.add(content, BorderLayout.CENTER);

        this.frame.pack();
        this.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        this.center(0);
    }

    public void center(int screen) {
        SwingUtils.centerWindow(this.frame, screen);
    }

    public void setVisible(boolean visible) {
        this.frame.setVisible(visible);
    }

    public void dispose() {
        this.frame.dispose();
    }

    public void addWindowListener(WindowListener listener) {
        this.frame.addWindowListener(listener);
    }

    public void removeWindowListener(WindowListener listener) {
        this.frame.removeWindowListener(listener);
    }

    public JFrame getFrame() {
        return this.frame;
    }
}
