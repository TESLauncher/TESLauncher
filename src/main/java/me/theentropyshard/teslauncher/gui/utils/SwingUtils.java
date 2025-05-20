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

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class SwingUtils {
    private static final Map<String, Icon> ICON_CACHE = new HashMap<>();

    public static Icon getIcon(String path) {
        if (SwingUtils.ICON_CACHE.containsKey(path)) {
            return SwingUtils.ICON_CACHE.get(path);
        }

        Icon icon = new ImageIcon(Objects.requireNonNull(SwingUtils.class.getResource(path)));
        SwingUtils.ICON_CACHE.put(path, icon);

        return icon;
    }

    public static void startWorker(Runnable runnable) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                runnable.run();

                return null;
            }
        }.execute();
    }

    public static Action newAction(ActionListener actionListener) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionListener.actionPerformed(e);
            }
        };
    }

    public static BufferedImage loadImageFromFile(Path file) throws IOException {
        return ImageIO.read(file.toFile());
    }

    public static BufferedImage loadImageFromBase64(String base64) {
        if (base64 == null) {
            return null;
        }

        byte[] decoded = Base64.getMimeDecoder().decode(base64);

        try {
            return ImageIO.read(new ByteArrayInputStream(decoded));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Icon loadIconFromBase64(String base64) {
        return new ImageIcon(SwingUtils.loadImageFromBase64(base64));
    }

    public static BufferedImage getImage(String path) {
        try (InputStream inputStream = Objects.requireNonNull(SwingUtils.class.getResourceAsStream(path))) {
            return ImageIO.read(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void centerWindow(Window window, int screen) {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] allDevices = env.getScreenDevices();

        if (screen < 0 || screen >= allDevices.length) {
            screen = 0;
        }

        Rectangle bounds = allDevices[screen].getDefaultConfiguration().getBounds();
        window.setLocation(
                ((bounds.width - window.getWidth()) / 2) + bounds.x,
                ((bounds.height - window.getHeight()) / 2) + bounds.y
        );
    }

    public static void removeActionListeners(AbstractButton button) {
        for (ActionListener listener : button.getActionListeners()) {
            button.removeActionListener(listener);
        }
    }

    public static void removeMouseListeners(Component component) {
        for (MouseListener listener : component.getMouseListeners()) {
            component.removeMouseListener(listener);
        }
    }

    public static void setJTableColumnsWidth(JTable table, double... percentages) {
        TableColumnModel columnModel = table.getColumnModel();
        int tablePreferredWidth = columnModel.getTotalColumnWidth();

        double total = 0;
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            total += percentages[i];
        }

        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            columnModel.getColumn(i).setPreferredWidth((int) (tablePreferredWidth * (percentages[i] / total)));
        }
    }
}
