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

import me.theentropyshard.teslauncher.gui.utils.MouseListenerBuilder;
import me.theentropyshard.teslauncher.gui.utils.SwingUtils;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class AddInstanceItem extends JPanel {
    private static final int SIDE_SIZE = 100;
    private static final Dimension PREFERRED_SIZE = new Dimension(AddInstanceItem.SIDE_SIZE, AddInstanceItem.SIDE_SIZE);

    private Color defaultColor;
    private Color hoveredColor;
    private Color pressedColor;

    private boolean mouseOver;
    private boolean mousePressed;

    public AddInstanceItem() {
        super(new BorderLayout());

        this.add(new JLabel(SwingUtils.getIcon("/assets/cross.png")), BorderLayout.CENTER);

        this.setDefaultColor(UIManager.getColor("InstanceItem.defaultColor"));
        this.setHoveredColor(UIManager.getColor("InstanceItem.hoveredColor"));
        this.setPressedColor(UIManager.getColor("InstanceItem.pressedColor"));

        this.setOpaque(false);
        this.setToolTipText("Add new Cosmic instance");
        this.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        MouseListener listener = new MouseListenerBuilder()
            .mouseEntered(e -> {
                this.mouseOver = true;
                this.repaint();
            })
            .mouseExited(e -> {
                this.mouseOver = false;
                this.repaint();
            })
            .mousePressed(e -> {
                this.mousePressed = true;
                this.repaint();
            })
            .mouseReleased(e -> {
                this.mousePressed = false;
                this.repaint();
            })
            .build();

        this.addMouseListener(listener);
    }

    public void onClick(MouseClickListener listener) {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                listener.onMouseClick(e);
            }
        });
    }

    public interface MouseClickListener {
        void onMouseClick(MouseEvent event);
    }

    @Override
    public void updateUI() {
        super.updateUI();

        this.updateColors();
    }

    private void paintBackground(Graphics g) {
        Color color = this.defaultColor;

        if (this.mouseOver) {
            color = this.hoveredColor;
        }

        if (this.mousePressed) {
            color = this.pressedColor;
        }

        g.setColor(color);
        g.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 10, 10);
    }


    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = ((Graphics2D) g);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        this.paintBackground(g2d);

        super.paintComponent(g2d);
    }

    public void updateColors() {
        this.setDefaultColor(UIManager.getColor("InstanceItem.defaultColor"));
        this.setHoveredColor(UIManager.getColor("InstanceItem.hoveredColor"));
        this.setPressedColor(UIManager.getColor("InstanceItem.pressedColor"));
    }

    @Override
    public Dimension getPreferredSize() {
        return AddInstanceItem.PREFERRED_SIZE;
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
