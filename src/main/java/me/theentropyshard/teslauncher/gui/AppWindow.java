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

package me.theentropyshard.teslauncher.gui;

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
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] allDevices = env.getScreenDevices();

        if (screen < 0 || screen >= allDevices.length) {
            screen = 0;
        }

        Rectangle bounds = allDevices[screen].getDefaultConfiguration().getBounds();
        this.frame.setLocation(
                ((bounds.width - this.frame.getWidth()) / 2) + bounds.x,
                ((bounds.height - this.frame.getHeight()) / 2) + bounds.y
        );
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
