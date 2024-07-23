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

package me.theentropyshard.teslauncher.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.BlockingQueue;

public final class LogRunnable implements Runnable {
    private static final Logger LOG = LogManager.getLogger(LogRunnable.class);

    private final BlockingQueue<LogEvent> queue;

    public LogRunnable(BlockingQueue<LogEvent> queue) {
        this.queue = queue;
    }

    public void start() {
        new Thread(this, "TES-Log-Thread").start();
    }

    @Override
    public void run() {
        while (true) {
            LogEvent event;

            try {
                event = this.queue.take();
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();

                return;
            }

            event.post(LOG);
        }
    }
}
