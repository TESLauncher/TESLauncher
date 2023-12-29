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

package me.theentropyshard.teslauncher.gui.components;

import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.instance.Instance;
import me.theentropyshard.teslauncher.instance.InstanceManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.HashSet;
import java.util.Set;

public class InstanceItem extends JPanel {
    private static final int SIDE_SIZE = 100;
    private static final Dimension PREFERRED_SIZE = new Dimension(InstanceItem.SIDE_SIZE, InstanceItem.SIDE_SIZE);

    private final Set<ActionListener> listeners;
    private final Set<ActionListener> mouseClickListeners;
    private final Set<ActionListener> mouseEnteredListeners;
    private final Set<ActionListener> mouseExitedListeners;

    private final JLabel iconLabel;
    private final JLabel textLabel;

    private Color defaultColor;
    private Color hoveredColor;
    private Color pressedColor;
    private Color arcColor;

    private boolean mouseOver;
    private boolean mousePressed;

    private double percentComplete;

    public InstanceItem(Icon icon, String text) {
        super(new BorderLayout(), true);

        this.listeners = new HashSet<>();
        this.mouseClickListeners = new HashSet<>();
        this.mouseEnteredListeners = new HashSet<>();
        this.mouseExitedListeners = new HashSet<>();

        this.iconLabel = new JLabel(icon);
        this.iconLabel.setHorizontalAlignment(JLabel.CENTER);
        this.add(this.iconLabel, BorderLayout.CENTER);

        this.textLabel = new JLabel(text);
        this.textLabel.setHorizontalAlignment(JLabel.CENTER);
        this.add(this.textLabel, BorderLayout.SOUTH);

        this.setDefaultColor(UIManager.getColor("InstanceItem.defaultColor"));
        this.setHoveredColor(UIManager.getColor("InstanceItem.hoveredColor"));
        this.setPressedColor(UIManager.getColor("InstanceItem.pressedColor"));
        this.arcColor = UIManager.getColor("AccountItem.borderColor");

        this.setOpaque(false);
        this.setToolTipText(text);
        this.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                InstanceItem.this.mouseOver = true;
                InstanceItem.this.repaint();

                ActionEvent event = new ActionEvent(InstanceItem.this, 1, String.valueOf(e.getButton()));
                InstanceItem.this.mouseEnteredListeners.forEach(listener -> listener.actionPerformed(event));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                InstanceItem.this.mouseOver = false;
                InstanceItem.this.repaint();

                ActionEvent event = new ActionEvent(InstanceItem.this, 1, String.valueOf(e.getButton()));
                InstanceItem.this.mouseExitedListeners.forEach(listener -> listener.actionPerformed(event));
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

    public Instance getAssociatedInstance() {
        InstanceManager instanceManager = TESLauncher.getInstance().getInstanceManager();
        return instanceManager.getInstanceByName(this.getTextLabel().getText());
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

        if (this instanceof AddInstanceItem) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(this.arcColor);

        Dimension size = this.getSize();

        double degree = 360 * this.percentComplete;

        int arcSize = 48;
        Shape arc = new Arc2D.Double(
                (double) size.width / 2 - (double) arcSize / 2,
                (double) size.height / 2 - (double) arcSize / 2 - 8,
                arcSize,
                arcSize,
                90 - degree,
                degree,
                Arc2D.PIE
        );

        int innerSize = 42;
        Shape inner = new Ellipse2D.Double(
                (double) size.width / 2 - (double) innerSize / 2,
                (double) size.height / 2 - (double) innerSize / 2 - 8,
                innerSize,
                innerSize
        );

        Area area = new Area(arc);
        area.subtract(new Area(inner));

        g2.fill(area);
    }

    public void updateColors() {
        this.setDefaultColor(UIManager.getColor("InstanceItem.defaultColor"));
        this.setHoveredColor(UIManager.getColor("InstanceItem.hoveredColor"));
        this.setPressedColor(UIManager.getColor("InstanceItem.pressedColor"));
        this.arcColor = UIManager.getColor("AccountItem.borderColor");
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

    public void addMouseExitedListener(ActionListener listener) {
        this.mouseExitedListeners.add(listener);
    }

    public void removeMouseExitedListener(ActionListener listener) {
        this.mouseExitedListeners.remove(listener);
    }

    public void addMouseEnteredListener(ActionListener listener) {
        this.mouseEnteredListeners.add(listener);
    }

    public void removeMouseEnteredListener(ActionListener listener) {
        this.mouseEnteredListeners.remove(listener);
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

    public double getPercentComplete() {
        return this.percentComplete;
    }

    public void setPercentComplete(double percentComplete) {
        this.percentComplete = percentComplete;
    }
}
