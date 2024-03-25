/*
 * TESLauncher - https://github.com/TESLauncher/TESLauncher
 * Copyright (C) 2023-2024 TESLauncher
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

public final class SkinUtils {
    public static BufferedImage getScaledSkinHead(BufferedImage skin) {
        BufferedImage head = skin.getSubimage(8, 8, 8, 8);
        Image scaledHead = head.getScaledInstance(32, 32, BufferedImage.SCALE_FAST);

        BufferedImage result = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = result.getGraphics();
        graphics.drawImage(scaledHead, 0, 0, null);
        graphics.dispose();

        return result;
    }

    private SkinUtils() {
        throw new UnsupportedOperationException();
    }
}
