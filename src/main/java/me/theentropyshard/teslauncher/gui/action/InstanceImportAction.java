package me.theentropyshard.teslauncher.gui.action;

import me.theentropyshard.teslauncher.TESLauncher;
import me.theentropyshard.teslauncher.language.Language;
import me.theentropyshard.teslauncher.Settings;
import me.theentropyshard.teslauncher.gui.utils.MessageBox;
import me.theentropyshard.teslauncher.gui.utils.Worker;
import me.theentropyshard.teslauncher.minecraft.MinecraftInstance;
import me.theentropyshard.teslauncher.instance.InstanceManager;
import me.theentropyshard.teslauncher.logging.Log;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class InstanceImportAction extends AbstractAction {
    private final JDialog dialog;

    public InstanceImportAction(JDialog dialog) {
        super(TESLauncher.getInstance().getLanguage().getString("gui.addInstanceDialog.importInstance.importButton"));

        this.dialog = dialog;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Language language = TESLauncher.getInstance().getLanguage();

        new Worker<InstanceManager.InstanceImportResult, Void>("importing instance") {
            @Override
            protected InstanceManager.InstanceImportResult work() {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setFileFilter(new FileNameExtensionFilter(
                    language.getString("gui.addInstanceDialog.importInstance.crliFiles"),
                    "crli"
                ));

                Settings settings = TESLauncher.getInstance().getSettings();
                if (settings.lastDir != null && !settings.lastDir.isEmpty()) {
                    fileChooser.setCurrentDirectory(new File(settings.lastDir));
                }

                int option = fileChooser.showOpenDialog(TESLauncher.frame);
                if (option != JFileChooser.APPROVE_OPTION) {
                    return null;
                }

                File selectedFile = fileChooser.getSelectedFile();
                if (selectedFile == null) {
                    return null;
                }

                settings.lastDir = selectedFile.toPath().toAbsolutePath().getParent().toString();

                if (!selectedFile.getName().endsWith(".crli")) {
                    MessageBox.showErrorMessage(
                        TESLauncher.frame,
                        language.getString("gui.addInstanceDialog.importInstance.wrongExtension")
                    );

                    return null;
                }

                InstanceManager instanceManager = TESLauncher.getInstance().getInstanceManager();
                try {
                    InstanceManager.InstanceImportResult result = instanceManager.importInstance(selectedFile.toPath());

                    switch (result.getStatus()) {
                        case SUCCESS -> MessageBox.showPlainMessage(
                            TESLauncher.frame,
                            language.getString("gui.addInstanceDialog.importInstance.title"),
                            language.getString("gui.addInstanceDialog.importInstance.success")
                        );
                        case BAD_FILE -> MessageBox.showErrorMessage(
                            TESLauncher.frame,
                            language.getString("gui.addInstanceDialog.importInstance.badFile") +
                                ": " + result.getMessage()
                        );
                        case INSTANCE_EXISTS -> MessageBox.showErrorMessage(
                            TESLauncher.frame,
                            language.getString("gui.addInstanceDialog.importInstance.duplicate")
                                .replace("$$INSTANCE_FOLDER$$", String.valueOf(result.getMessage()))
                        );
                    }

                    return result;
                } catch (IOException ex) {
                    MessageBox.showErrorMessage(
                        TESLauncher.frame,
                        language.getString("gui.addInstanceDialog.importInstance.failure")
                    );

                    Log.error("Could not import instance", ex);

                    return null;
                }
            }

            @Override
            protected void done() {
                InstanceImportAction.this.dialog.dispose();

                InstanceManager.InstanceImportResult result;
                try {
                    result = this.get();
                } catch (InterruptedException | ExecutionException ex) {
                    Log.error(ex);

                    return;
                }

                if (result == null || result.getStatus() != InstanceManager.InstanceImportStatus.SUCCESS) {
                    return;
                }

                TESLauncher.getInstance().getGui().getPlayView().loadInstance(
                    ((MinecraftInstance) result.getMessage()), true
                );
            }
        }.execute();
    }
}