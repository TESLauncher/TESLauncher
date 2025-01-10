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

package me.theentropyshard.teslauncher.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class SmoothScrollMouseWheelListener implements MouseWheelListener, ActionListener {
    private static final int TIMER_DELAY = 15;
    private static final float ADDITION = 15.0f;
    private static final float DECELERATION = 0.08f;

    private final JScrollBar scrollBar;
    private final Timer timer;

    private int wheelRotation;
    private float velocity;

    public SmoothScrollMouseWheelListener(JScrollBar scrollBar) {
        this.scrollBar = scrollBar;
        this.timer = new Timer(SmoothScrollMouseWheelListener.TIMER_DELAY, this);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        this.wheelRotation = e.getWheelRotation();
        this.velocity = 1;

        this.timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (this.velocity < 0f) {
            this.timer.stop();

            return;
        }

        int value = this.scrollBar.getValue();
        int newValue = (int) (value + this.wheelRotation *
                SmoothScrollMouseWheelListener.ADDITION *
                SmoothScrollMouseWheelListener.easeInOutQuad(this.velocity));
        this.scrollBar.setValue(newValue);

        this.velocity -= SmoothScrollMouseWheelListener.DECELERATION;
    }

    private static float easeInOutQuad(float x) {
        return x < 0.5 ? 2 * x * x : (float) (1 - Math.pow(-2 * x + 2, 2) / 2);
    }
}