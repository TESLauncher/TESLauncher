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

import com.madgag.gif.fmsware.GifDecoder;
import me.theentropyshard.teslauncher.utils.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class GifIcon implements Icon, Runnable {
    private final List<GifFrame> frames;
    private final int width;
    private final int height;
    private final Component component;

    private boolean running;
    private int currentFrame;

    public GifIcon(InputStream inputStream, int width, int height, Component component) throws IOException {
        this.frames = new ArrayList<>();
        this.width = width;
        this.height = height;
        this.component = component;

        GifDecoder decoder = new GifDecoder();
        int result = decoder.read(inputStream);

        if (result == GifDecoder.STATUS_FORMAT_ERROR) {
            throw new IOException("Could not read GIF: format error");
        } else if (result == GifDecoder.STATUS_OPEN_ERROR) {
            throw new IOException("Could not read GIF: could not read stream");
        } else if (result != GifDecoder.STATUS_OK) {
            throw new IOException("Could not read GIF: unknown error");
        }

        for (int i = 0; i < decoder.getFrameCount(); i++) {
            BufferedImage frame = decoder.getFrame(i);

            BufferedImage scaledImage = ImageUtils.toBufferedImage(frame.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH));
            BufferedImage clippedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2d = clippedImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            TexturePaint paint = new TexturePaint(scaledImage, new Rectangle(width, height));
            g2d.setPaint(paint);
            g2d.fill(new RoundRectangle2D.Double(0, 0, width, height, 10, 10));

            g2d.dispose();

            this.frames.add(new GifFrame(decoder.getDelay(i), clippedImage));
        }

        this.running = true;

        Thread animationThread = new Thread(this);
        animationThread.setName("AnimatedGifThread");
        animationThread.setDaemon(true);
        animationThread.start();
    }

    @Override
    public void run() {
        while (this.running) {
            this.currentFrame = (this.currentFrame + 1) % this.frames.size();
            this.component.repaint();

            try {
                Thread.sleep(this.frames.get(this.currentFrame).getDurationMilliseconds());
            } catch (InterruptedException ignored) {

            }
        }
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.drawImage(this.frames.get(this.currentFrame).getImage(), x, y, null);
    }

    public void stop() {
        this.running = false;
    }

    @Override
    public int getIconWidth() {
        return this.width;
    }

    @Override
    public int getIconHeight() {
        return this.height;
    }

    private static final class GifFrame {
        private final int durationMilliseconds;
        private final Image image;

        public GifFrame(int durationMilliseconds, Image image) {
            this.durationMilliseconds = durationMilliseconds;
            this.image = image;
        }

        public int getDurationMilliseconds() {
            return this.durationMilliseconds;
        }

        public Image getImage() {
            return this.image;
        }
    }
}