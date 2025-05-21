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

package me.theentropyshard.teslauncher.utils;

import java.awt.*;
import java.awt.image.BufferedImage;

public final class ImageUtils {
    /**
     * Converts any <code>Image</code> to <code>BufferedImage</code>
     *
     * @param image any <code>Image</code> object
     * @return instance of <code>BufferedImage</code> with support for transparency
     */
    public static BufferedImage toBufferedImage(Image image) {
        BufferedImage resultImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = resultImage.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        return resultImage;
    }

    /**
     * Resizes an image keeping its aspect ratio and fitting it in the specified size
     *
     * @param image         image to resize
     * @param desiredWidth  width in which the image needs to be fit
     * @param desiredHeight height in which the image needs to be fit
     * @return resized image with transparent bars if it does not fully cover desired width and height
     */
    public static BufferedImage fitImageAndResize(BufferedImage image, int desiredWidth, int desiredHeight) {
        BufferedImage resizedImage = new BufferedImage(desiredWidth, desiredHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImage.createGraphics();

        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, desiredWidth, desiredHeight);
        g2d.setComposite(AlphaComposite.SrcOver);

        double scaleX = (double) desiredWidth / image.getWidth();
        double scaleY = (double) desiredHeight / image.getHeight();
        double scale = Math.min(scaleX, scaleY);

        int newWidth = (int) (image.getWidth() * scale);
        int newHeight = (int) (image.getHeight() * scale);

        int x = (desiredWidth - newWidth) / 2;
        int y = (desiredHeight - newHeight) / 2;

        g2d.drawImage(image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH), x, y, null);
        g2d.dispose();

        return resizedImage;
    }

    private ImageUtils() {
        throw new UnsupportedOperationException();
    }
}