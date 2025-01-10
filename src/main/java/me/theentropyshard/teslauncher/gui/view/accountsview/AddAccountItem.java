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

package me.theentropyshard.teslauncher.gui.view.accountsview;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

public class AddAccountItem extends JPanel {
    protected Color defaultColor;
    protected Color hoveredColor;
    protected Color pressedColor;

    protected boolean mouseOver;
    protected boolean mousePressed;

    private final Set<ActionListener> mouseClickListeners;

    protected final int border = 12;

    public AddAccountItem() {
        super(new BorderLayout());

        this.setDefaultColor(UIManager.getColor("InstanceItem.defaultColor"));
        this.setHoveredColor(UIManager.getColor("InstanceItem.hoveredColor"));
        this.setPressedColor(UIManager.getColor("InstanceItem.pressedColor"));

        this.mouseClickListeners = new HashSet<>();

        this.setOpaque(false);
        this.setBorder(new EmptyBorder(
                this.border,
                this.border,
                this.border,
                this.border
        ));

        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        this.setPreferredSize(new Dimension(1, 56));

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                AddAccountItem.this.mouseOver = true;
                AddAccountItem.this.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                AddAccountItem.this.mouseOver = false;
                AddAccountItem.this.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                AddAccountItem.this.mousePressed = true;
                AddAccountItem.this.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                AddAccountItem.this.mousePressed = false;
                AddAccountItem.this.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                ActionEvent event = new ActionEvent(AddAccountItem.this, 1, String.valueOf(e.getButton()));
                AddAccountItem.this.mouseClickListeners.forEach(listener -> listener.actionPerformed(event));
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Color color = this.defaultColor;

        if (this.mouseOver) {
            color = this.hoveredColor;
        }

        if (this.mousePressed) {
            color = this.pressedColor;
        }

        g.setColor(color);
        g.fillRoundRect(
                this.border / 4,
                this.border / 4,
                this.getWidth() - this.border / 2,
                this.getHeight() - this.border / 2,
                10, 10
        );

        Font font = new Font("Arial", Font.BOLD, 20);
        g.setFont(font);

        String text = "Click to add new account";
        FontMetrics m = g.getFontMetrics();
        int width = m.stringWidth(text);

        Dimension size = this.getSize();

        g.setColor(Color.decode("#95A5A6"));
        g.drawString(text, size.width / 2 - width / 2, (int) (size.height - m.getStringBounds(text, g2).getHeight()));

        super.paintComponent(g);
    }

    public void updateColors() {
        this.setDefaultColor(UIManager.getColor("InstanceItem.defaultColor"));
        this.setHoveredColor(UIManager.getColor("InstanceItem.hoveredColor"));
        this.setPressedColor(UIManager.getColor("InstanceItem.pressedColor"));
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

    public void addMouseClickListener(ActionListener listener) {
        this.mouseClickListeners.add(listener);
    }

    public void removeMouseClickListener(ActionListener listener) {
        this.mouseClickListeners.remove(listener);
    }
}
