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

package me.theentropyshard.teslauncher.instance;

import me.theentropyshard.teslauncher.logging.Log;
import me.theentropyshard.teslauncher.utils.FileUtils;
import me.theentropyshard.teslauncher.utils.ListUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IconManager {
    private final Path workDir;
    private final List<InstanceIcon> icons;

    public IconManager(Path workDir) {
        this.workDir = workDir;
        this.icons = new ArrayList<>();
    }

    public void saveBuiltinIcons() throws IOException {
        String grassBlock = "grass_icon.png";
        if (Files.exists(this.workDir.resolve(grassBlock))) {
            return;
        }

        Path tempDir = this.workDir.resolve("temp_" + System.currentTimeMillis());

        if (Files.exists(tempDir)) {
            FileUtils.delete(tempDir);
        }

        Path tempFile = tempDir.resolve(grassBlock);
        FileUtils.createDirectoryIfNotExists(tempFile.getParent());

        try (InputStream resource = IconManager.class.getResourceAsStream("/assets/" + grassBlock)) {
            Files.write(tempFile, Objects.requireNonNull(resource).readAllBytes());
        }

        this.saveIcon(tempFile);

        FileUtils.delete(tempDir);
    }

    public void loadIcons() throws IOException {
        if (!this.icons.isEmpty()) {
            Log.warn("Tried to load icons, but they are already loaded");

            return;
        }

        for (Path iconPath : FileUtils.list(this.workDir)) {
            if (!Files.isRegularFile(iconPath)) {
                continue;
            }

            try {
                this.loadIcon(iconPath);
            } catch (IOException e) {
                Log.warn("Could not load icon from " + iconPath + ": " + e.getMessage());
            }
        }
    }

    public List<InstanceIcon> getIcons() {
        return this.icons;
    }

    public InstanceIcon getIcon(String fileName) {
        return ListUtils.search(this.icons, ico -> ico.getFileName().equals(fileName));
    }

    public InstanceIcon loadIcon(Path path) throws IOException {
        BufferedImage bufferedImage;
        try (InputStream input = Files.newInputStream(path)) {
            bufferedImage = ImageIO.read(input);
        }

        if (bufferedImage == null) {
            throw new IOException("Could not read image: " + path);
        }

        if (bufferedImage.getWidth() != 32 || bufferedImage.getHeight() != 32) {
            BufferedImage scaledImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = scaledImage.createGraphics();
            g2d.drawImage(bufferedImage.getScaledInstance(32, 32, BufferedImage.SCALE_FAST), 0, 0, null);
            g2d.dispose();

            try (OutputStream output = Files.newOutputStream(path)) {
                ImageIO.write(scaledImage, "PNG", output);
            }

            InstanceIcon icon = new InstanceIcon(path.getFileName().toString(), new ImageIcon(scaledImage));
            this.icons.add(icon);
            return icon;
        } else {
            InstanceIcon icon = new InstanceIcon(path.getFileName().toString(), new ImageIcon(bufferedImage));
            this.icons.add(icon);
            return icon;
        }
    }

    public InstanceIcon saveIcon(Path iconPath) throws IOException {
        Path copiedIcon = this.workDir.resolve(iconPath.getFileName());
        if (Files.exists(copiedIcon)) {
            return ListUtils.search(this.icons, ico -> ico.getFileName().equals(copiedIcon.getFileName().toString()));
        }
        Files.copy(iconPath, copiedIcon);
        return this.loadIcon(copiedIcon);
    }

    public void deleteIcon(String fileName) throws IOException {
        FileUtils.delete(this.workDir.resolve(fileName));
    }
}