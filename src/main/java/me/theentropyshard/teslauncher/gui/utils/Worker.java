package me.theentropyshard.teslauncher.gui.utils;

import me.theentropyshard.teslauncher.logging.Log;

import javax.swing.*;

public abstract class Worker<T, V> extends SwingWorker<T, V> {
    private final String name;

    public Worker(String name) {
        this.name = name;
    }

    @Override
    protected final T doInBackground() throws Exception {
        try {
            return this.work();
        } catch (Exception e) {
            Log.error("Exception while " + this.name, e);
        }

        return null;
    }

    protected abstract T work() throws Exception;
}