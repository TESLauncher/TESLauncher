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

package me.theentropyshard.teslauncher.gui.accountsview;

import me.theentropyshard.teslauncher.utils.SwingUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

public class AccountItem extends JPanel {
    protected Color defaultColor;
    protected Color hoveredColor;
    protected Color pressedColor;
    private Color borderColor;

    private final Set<ActionListener> mouseClickListeners;

    protected boolean mouseOver;
    protected boolean mousePressed;

    private boolean selected;

    protected final int border = 12;

    public AccountItem() {
        super(new BorderLayout());

        this.mouseClickListeners = new HashSet<>();

        this.borderColor = UIManager.getColor("AccountItem.borderColor");

        this.setDefaultColor(UIManager.getColor("InstanceItem.defaultColor"));
        this.setHoveredColor(UIManager.getColor("InstanceItem.hoveredColor"));
        this.setPressedColor(UIManager.getColor("InstanceItem.pressedColor"));

        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel headIcon = new JLabel(SwingUtils.getIcon("/steve_head_32.png"));
        this.add(headIcon, BorderLayout.WEST);

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        centerPanel.setOpaque(false);
        this.add(centerPanel, BorderLayout.CENTER);

        JLabel nickLabel = new JLabel("TheEntropyShard");
        nickLabel.setOpaque(false);
        centerPanel.add(nickLabel);

        this.setOpaque(false);
        this.setBorder(new EmptyBorder(
                this.border,
                this.border,
                this.border,
                this.border
        ));

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                AccountItem.this.mouseOver = true;
                AccountItem.this.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                AccountItem.this.mouseOver = false;
                AccountItem.this.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                AccountItem.this.mousePressed = true;
                AccountItem.this.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                AccountItem.this.mousePressed = false;
                AccountItem.this.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                ActionEvent event = new ActionEvent(AccountItem.this, 1, String.valueOf(e.getButton()));
                AccountItem.this.mouseClickListeners.forEach(listener -> listener.actionPerformed(event));
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

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

        if (this.selected) {
            g.setColor(this.borderColor);
            ((Graphics2D) g).setStroke(new BasicStroke(2));
            g.drawRoundRect(this.border / 4,
                    this.border / 4,
                    this.getWidth() - this.border / 2,
                    this.getHeight() - this.border / 2,
                    10, 10);
        }

        super.paintComponent(g);
    }

    public void updateColors() {
        this.setDefaultColor(UIManager.getColor("InstanceItem.defaultColor"));
        this.setHoveredColor(UIManager.getColor("InstanceItem.hoveredColor"));
        this.setPressedColor(UIManager.getColor("InstanceItem.pressedColor"));
        this.borderColor = UIManager.getColor("AccountItem.borderColor");
    }

    public boolean isSelected() {
        return this.selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
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
