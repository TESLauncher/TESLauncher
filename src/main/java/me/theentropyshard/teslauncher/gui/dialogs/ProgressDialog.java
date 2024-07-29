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

package me.theentropyshard.teslauncher.gui.dialogs;

import com.formdev.flatlaf.FlatClientProperties;
import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.minecraft.download.MinecraftDownloadListener;
import me.theentropyshard.teslauncher.utils.MathUtils;

import javax.swing.*;
import java.awt.*;

public class ProgressDialog extends AppDialog implements MinecraftDownloadListener {
    private final JLabel stageLabel;
    private final JProgressBar progressBar;

    public ProgressDialog(String title) {
        super(TESLauncher.frame, title);

        JPanel root = new JPanel(new BorderLayout());
        root.setPreferredSize(new Dimension(450, 270));

        JPanel centerPanel = new JPanel();
        this.stageLabel = new JLabel("Stage: ");
        centerPanel.add(this.stageLabel);
        root.add(centerPanel, BorderLayout.CENTER);

        this.progressBar = new JProgressBar(JProgressBar.HORIZONTAL);
        this.progressBar.putClientProperty(FlatClientProperties.PROGRESS_BAR_SQUARE, true);
        this.progressBar.setFont(this.progressBar.getFont().deriveFont(12.0f));
        this.progressBar.setStringPainted(true);
        root.add(this.progressBar, BorderLayout.SOUTH);

        this.setResizable(false);
        this.setContent(root);
        this.center(0);
    }

    @Override
    public void onStageChanged(String stage) {
        this.stageLabel.setText("Stage: " + stage);
    }

    @Override
    public void onProgress(long totalSize, long downloadedBytes) {
        this.progressBar.setMinimum(0);
        this.progressBar.setMaximum((int) totalSize);
        this.progressBar.setValue((int) downloadedBytes);
        this.progressBar.setString(MathUtils.round(downloadedBytes / 1024.0D / 1024.0D, 2) +
                " MiB / " + MathUtils.round(totalSize / 1024.0D / 1024.0D, 2) + " MiB");
    }

    @Override
    public void onFinish() {

    }

    public JLabel getStageLabel() {
        return this.stageLabel;
    }

    public JProgressBar getProgressBar() {
        return this.progressBar;
    }
}
