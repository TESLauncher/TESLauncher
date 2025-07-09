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

package me.theentropyshard.teslauncher.minecraft.download;

import me.theentropyshard.teslauncher.gui.dialogs.ProgressDialog;
import me.theentropyshard.teslauncher.minecraft.data.Version;
import me.theentropyshard.teslauncher.minecraft.mods.ModLoaderInfo;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;

public class GuiMinecraftDownloader extends MinecraftDownloader {
    private final ProgressDialog dialog;

    public GuiMinecraftDownloader(Path versionsDir, Path assetsDir, Path librariesDir, Path nativesDir, Path runtimesDir,
                                  Path instanceResourcesDir, ProgressDialog dialog, boolean downloadJava) {
        super(versionsDir, assetsDir, librariesDir, nativesDir, runtimesDir, instanceResourcesDir, dialog, downloadJava);
        this.dialog = dialog;
    }

    @Override
    public Version downloadMinecraft(String versionId, ModLoaderInfo loaderInfo) throws IOException {
        SwingUtilities.invokeLater(() -> this.dialog.setVisible(true));
        Version v = super.downloadMinecraft(versionId, loaderInfo);
        SwingUtilities.invokeLater(() -> this.dialog.getDialog().dispose());

        return v;
    }
}
