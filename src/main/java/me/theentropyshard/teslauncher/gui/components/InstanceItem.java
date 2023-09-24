/*
 *  Copyright 2023 TheEntropyShard (https://github.com/TheEntropyShard)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package me.theentropyshard.teslauncher.gui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

public class InstanceItem extends JPanel {
    private static final int SIDE_SIZE = 100;
    private static final Dimension PREFERRED_SIZE = new Dimension(InstanceItem.SIDE_SIZE, InstanceItem.SIDE_SIZE);

    private final Set<ActionListener> listeners;
    private final Set<ActionListener> mouseClickListeners;

    private final JLabel iconLabel;
    private final JLabel textLabel;

    private Color defaultColor;
    private Color hoveredColor;
    private Color pressedColor;

    private boolean mouseOver;
    private boolean mousePressed;

    public InstanceItem(Icon icon, String text) {
        super(new BorderLayout(), true);

        this.listeners = new HashSet<>();
        this.mouseClickListeners = new HashSet<>();

        this.iconLabel = new JLabel(icon);
        this.iconLabel.setHorizontalAlignment(JLabel.CENTER);
        this.add(this.iconLabel, BorderLayout.CENTER);

        this.textLabel = new JLabel(text);
        this.textLabel.setHorizontalAlignment(JLabel.CENTER);
        this.add(this.textLabel, BorderLayout.SOUTH);

        this.setDefaultColor(UIManager.getColor("InstanceItem.defaultColor"));
        this.setHoveredColor(UIManager.getColor("InstanceItem.hoveredColor"));
        this.setPressedColor(UIManager.getColor("InstanceItem.pressedColor"));

        this.setOpaque(false);
        this.setToolTipText(text);
        this.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                InstanceItem.this.mouseOver = true;
                InstanceItem.this.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                InstanceItem.this.mouseOver = false;
                InstanceItem.this.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                InstanceItem.this.mousePressed = true;
                InstanceItem.this.repaint();

                ActionEvent event = new ActionEvent(InstanceItem.this, 1, String.valueOf(e.getButton()));
                InstanceItem.this.listeners.forEach(listener -> listener.actionPerformed(event));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                InstanceItem.this.mousePressed = false;
                InstanceItem.this.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                ActionEvent event = new ActionEvent(InstanceItem.this, 1, String.valueOf(e.getButton()));
                InstanceItem.this.mouseClickListeners.forEach(listener -> listener.actionPerformed(event));
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Color color = this.defaultColor;

        if (this.mouseOver) {
            color = this.hoveredColor;
        }

        if (this.mousePressed) {
            color = this.pressedColor;
        }

        g.setColor(color);
        g.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 10, 10);

        super.paintComponent(g);
    }

    @Override
    public Dimension getPreferredSize() {
        return InstanceItem.PREFERRED_SIZE;
    }

    public void addListener(ActionListener listener, boolean onMouseClick) {
        if (onMouseClick) {
            this.mouseClickListeners.add(listener);
        } else {
            this.listeners.add(listener);
        }
    }

    public void removeListener(ActionListener listener, boolean onMouseClick) {
        if (onMouseClick) {
            this.mouseClickListeners.remove(listener);
        } else {
            this.listeners.remove(listener);
        }
    }

    public JLabel getIconLabel() {
        return this.iconLabel;
    }

    public JLabel getTextLabel() {
        return this.textLabel;
    }

    public void setDefaultColor(Color defaultColor) {
        this.defaultColor = defaultColor;
    }

    public void setHoveredColor(Color hoveredColor) {
        this.hoveredColor = hoveredColor;
    }

    public void setPressedColor(Color pressedColor) {
        this.pressedColor = pressedColor;
    }
}
