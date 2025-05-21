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

package me.theentropyshard.teslauncher.gui.utils;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.function.Consumer;

public class MouseListenerBuilder {
    private Consumer<MouseEvent> clickedConsumer;
    private Consumer<MouseEvent> pressedConsumer;
    private Consumer<MouseEvent> releasedConsumer;
    private Consumer<MouseEvent> enteredConsumer;
    private Consumer<MouseEvent> exitedConsumer;

    public MouseListenerBuilder() {

    }

    public MouseListener build() {
        return new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (MouseListenerBuilder.this.clickedConsumer != null) {
                    MouseListenerBuilder.this.clickedConsumer.accept(e);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (MouseListenerBuilder.this.pressedConsumer != null) {
                    MouseListenerBuilder.this.pressedConsumer.accept(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (MouseListenerBuilder.this.releasedConsumer != null) {
                    MouseListenerBuilder.this.releasedConsumer.accept(e);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (MouseListenerBuilder.this.enteredConsumer != null) {
                    MouseListenerBuilder.this.enteredConsumer.accept(e);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (MouseListenerBuilder.this.exitedConsumer != null) {
                    MouseListenerBuilder.this.exitedConsumer.accept(e);
                }
            }
        };
    }

    public MouseListenerBuilder mouseClicked(Consumer<MouseEvent> eventConsumer) {
        this.clickedConsumer = eventConsumer;

        return this;
    }

    public MouseListenerBuilder mousePressed(Consumer<MouseEvent> eventConsumer) {
        this.pressedConsumer = eventConsumer;

        return this;
    }

    public MouseListenerBuilder mouseReleased(Consumer<MouseEvent> eventConsumer) {
        this.releasedConsumer = eventConsumer;

        return this;
    }

    public MouseListenerBuilder mouseEntered(Consumer<MouseEvent> eventConsumer) {
        this.enteredConsumer = eventConsumer;

        return this;
    }

    public MouseListenerBuilder mouseExited(Consumer<MouseEvent> eventConsumer) {
        this.exitedConsumer = eventConsumer;

        return this;
    }
}