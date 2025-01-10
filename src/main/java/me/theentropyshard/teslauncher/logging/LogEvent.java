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

package me.theentropyshard.teslauncher.logging;

import me.theentropyshard.teslauncher.gui.console.LauncherConsole;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class LogEvent {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public static final int APP_CONSOLE = 0xA;
    public static final int FILE_LOG4J = 0xB;

    private final LogLevel level;
    private final String message;
    private final int flags;

    public LogEvent(LogLevel level, String message) {
        this.level = level;
        this.message = message.endsWith("\n") ? message : message + "\n";
        this.flags = LogEvent.APP_CONSOLE | LogEvent.FILE_LOG4J;
    }

    public void post(Logger log) {
        if ((this.flags & LogEvent.APP_CONSOLE) == LogEvent.APP_CONSOLE) {
            this.appConsole();
        }

        if ((this.flags & LogEvent.FILE_LOG4J) == LogEvent.FILE_LOG4J) {
            this.fileLog4j(log);
        }
    }

    private void appConsole() {
        if (LauncherConsole.instance == null) {
            return;
        }

        LauncherConsole c = LauncherConsole.instance;
        c.setColor(this.level.color()).setBold(true).write("[" + LogEvent.currentTime() + "]: ");
        c.setColor(this.level.color()).setBold(false).write(this.message);
    }

    private void fileLog4j(Logger log) {
        switch (this.level) {
            case WARN:
                log.warn(this.message);

                break;
            case ERROR:
                log.error(this.message);

                break;
            case DEBUG:
                log.debug(this.message);

                break;
            case INFO:
                log.info(this.message);

                break;
            default:
                throw new IllegalArgumentException("Unknown log type: " + this.level);
        }
    }

    @Override
    public String toString() {
        return "[" + LogEvent.currentTime() + "] [" + this.level.name() + "]: " + this.message;
    }

    private static String currentTime() {
        return LogEvent.FORMATTER.format(LocalDateTime.now());
    }
}
