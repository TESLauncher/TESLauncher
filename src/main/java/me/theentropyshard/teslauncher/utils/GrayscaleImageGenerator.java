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

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class GrayscaleImageGenerator {
    public static void main(String[] args) {
        JPanel panel = new JPanel(new BorderLayout());

        int scale = 5;

        BufferedImage image = new BufferedImage(48, 16, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        Random random = new Random();

        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = getRandomGrayColorRGB(random, 75, 210);
        }

        JPanel drawPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(image, 0, 0,
                        image.getWidth() * scale,
                        image.getHeight() * scale,
                        null);
            }
        };

        drawPanel.setPreferredSize(new Dimension(
                image.getWidth() * scale,
                image.getHeight() * scale
        ));

        panel.add(drawPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel();

        JButton generateButtons = new JButton("Generate");
        generateButtons.addActionListener(e -> {
            for (int i = 0; i < pixels.length; i++) {
                pixels[i] = 0xFF << 24 | getRandomGrayColorRGB(random, 75, 210);
            }

            drawPanel.repaint();
        });

        buttonsPanel.add(generateButtons);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            try {
                String name = "image";
                int counter = 0;
                File output = new File(name + counter + ".png");
                while (output.exists()) {
                    output = new File(name + counter + ".png");
                    counter++;
                }
                ImageIO.write(image, "PNG", output);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        buttonsPanel.add(saveButton);

        panel.add(buttonsPanel, BorderLayout.SOUTH);

        JFrame frame = new JFrame("Test");
        frame.add(panel);
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static int getRandomGrayColorRGB(Random random, int min, int max) {
        int r = random.nextInt(max - min) + min;
        int g = random.nextInt(max - min) + min;
        int b = random.nextInt(max - min) + min;

        r = g = b = (r + g + b) / 3;

        return (0xFF << 24) | (r << 16 | g << 8 | b);
    }
}
