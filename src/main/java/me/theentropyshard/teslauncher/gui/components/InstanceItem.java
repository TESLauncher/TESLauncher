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

import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.minecraft.MinecraftInstance;
import me.theentropyshard.teslauncher.instance.InstanceManager;
import me.theentropyshard.teslauncher.minecraft.download.MinecraftDownloadListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

public class InstanceItem extends JPanel implements MinecraftDownloadListener {
    private static final int SIDE_SIZE = 100;
    private static final Dimension PREFERRED_SIZE = new Dimension(InstanceItem.SIDE_SIZE, InstanceItem.SIDE_SIZE);

    private final JLabel iconLabel;
    private final JLabel textLabel;

    private Color defaultColor;
    private Color hoveredColor;
    private Color pressedColor;
    private Color arcColor;

    private boolean mouseOver;
    private boolean mousePressed;

    private boolean mouseEnabled;

    private double percentComplete;

    public InstanceItem(Icon icon, String text) {
        super(new BorderLayout());

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

        this.mouseEnabled = true;

        this.setOpaque(false);
        this.setToolTipText(text);
        this.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!InstanceItem.this.mouseEnabled) {
                    return;
                }

                InstanceItem.this.mouseOver = true;
                InstanceItem.this.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!InstanceItem.this.mouseEnabled) {
                    return;
                }

                InstanceItem.this.mouseOver = false;
                InstanceItem.this.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (!InstanceItem.this.mouseEnabled) {
                    return;
                }

                InstanceItem.this.mousePressed = true;
                InstanceItem.this.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!InstanceItem.this.mouseEnabled) {
                    return;
                }

                InstanceItem.this.mousePressed = false;
                InstanceItem.this.repaint();
            }
        });
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (SwingUtilities.isEventDispatchThread()) {
            this.mouseEnabled = enabled;

            if (!enabled) {
                this.mouseOver = false;
                this.mousePressed = false;
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            } else {
                this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

        } else {
            SwingUtilities.invokeLater(() -> this.setEnabled(enabled));
        }
    }

    public MinecraftInstance getAssociatedInstance() {
        InstanceManager instanceManager = TESLauncher.getInstance().getInstanceManager();
        return instanceManager.getInstanceByName(this.getTextLabel().getText());
    }

    protected void paintBackground(Graphics g) {
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

    private Area getArc(double progress) {
        Dimension size = this.getSize();

        double degree = 360 * progress;

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

        return area;
    }

    protected void paintArc(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (this.percentComplete > 0.0D) {
            g2.setColor(Color.LIGHT_GRAY);
            g2.fill(this.getArc(1.0D));
        }

        g2.setColor(this.arcColor);
        g2.fill(this.getArc(this.percentComplete));
    }

    @Override
    protected void paintComponent(Graphics g) {
        this.paintBackground(g);

        super.paintComponent(g);

        this.paintArc((Graphics2D) g);
    }

    public void updateColors() {
        this.setDefaultColor(UIManager.getColor("InstanceItem.defaultColor"));
        this.setHoveredColor(UIManager.getColor("InstanceItem.hoveredColor"));
        this.setPressedColor(UIManager.getColor("InstanceItem.pressedColor"));
        this.arcColor = UIManager.getColor("AccountItem.borderColor");
    }

    public void instanceChanged(MinecraftInstance instance) {
        this.textLabel.setText(instance.getName());
        this.setToolTipText(instance.getName());
    }

    @Override
    public void onStageChanged(String stage) {

    }

    @Override
    public void onProgress(long totalSize, long downloadedBytes) {
        SwingUtilities.invokeLater(() -> {
            this.setPercentComplete((double) downloadedBytes / (double) totalSize);
            this.repaint();
        });
    }

    @Override
    public void onFinish() {
        SwingUtilities.invokeLater(() -> {
            this.setPercentComplete(0.0D);
            this.repaint();
        });
    }

    @Override
    public Dimension getPreferredSize() {
        return InstanceItem.PREFERRED_SIZE;
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
