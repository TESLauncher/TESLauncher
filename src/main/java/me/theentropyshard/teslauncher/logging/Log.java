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

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Log {
    private static final BlockingQueue<LogEvent> EVENT_QUEUE = new ArrayBlockingQueue<>(128);

    private static final Pattern LOG4J_THREAD_REGEX = Pattern.compile("<log4j:Event.*?thread=\"(.*?)\".*?>");
    private static final Pattern LOG4J_LEVEL_REGEX = Pattern.compile("<log4j:Event.*?level=\"(.*?)\".*?>");
    private static final Pattern LOG4J_MESSAGE_REGEX = Pattern.compile("<log4j:Message><!\\[CDATA\\[(.*?)]]></log4j:Message>");

    public static void start() {
        new LogRunnable(Log.EVENT_QUEUE).start();

        //System.setOut(new SystemOutInterceptor(System.out, LogLevel.DEBUG));
        System.setErr(new SystemOutInterceptor(System.err, LogLevel.ERROR));

        //Log.info("Initialized logging");
    }

    public static void info(String message) {
        Log.EVENT_QUEUE.offer(new LogEvent(LogLevel.INFO, message));
    }

    public static void warn(String message) {
        Log.EVENT_QUEUE.offer(new LogEvent(LogLevel.WARN, message));
    }

    public static void error(String message) {
        Log.EVENT_QUEUE.offer(new LogEvent(LogLevel.ERROR, message));
    }

    public static void debug(String message) {
        Log.EVENT_QUEUE.offer(new LogEvent(LogLevel.DEBUG, message));
    }

    public static void error(String message, Throwable t) {
        Log.error(message);
        Log.error(t);
    }

    public static void error(Throwable t) {
        t.printStackTrace();

        CharArrayWriter writer = new CharArrayWriter();
        t.printStackTrace(new PrintWriter(writer));
        Log.error(writer.toString());
    }

    public static void minecraftLog4j(String string) {
        String thread = "";
        String message = "";
        String levelString = "";
        LogLevel level = LogLevel.INFO;

        Matcher threadMatcher = Log.LOG4J_THREAD_REGEX.matcher(string);
        if (threadMatcher.find()) {
            thread = threadMatcher.group(1);
        }

        Matcher levelMatcher = Log.LOG4J_LEVEL_REGEX.matcher(string);
        if (levelMatcher.find()) {
            levelString = levelMatcher.group(1);

            if (levelString.equalsIgnoreCase("INFO")) {
                level = LogLevel.INFO;
            } else if (levelString.equalsIgnoreCase("ERROR") || levelString.equalsIgnoreCase("SEVERE")) {
                level = LogLevel.ERROR;
            } else if (levelString.equalsIgnoreCase("WARN")) {
                level = LogLevel.WARN;
            }
        }

        Matcher messageMatcher = Log.LOG4J_MESSAGE_REGEX.matcher(string);
        if (messageMatcher.find()) {
            message = messageMatcher.group(1);
        }

        Log.EVENT_QUEUE.offer(new LogEvent(level, String.format("[%s/%s] %s", thread, levelString, message)));
    }

    public static void minecraft(String message) {
        Object[] value = Log.parseMinecraftLog(message);
        Log.EVENT_QUEUE.offer(new LogEvent((LogLevel) value[0], (String) value[1]));
    }

    private static Object[] parseMinecraftLog(String text) {
        LogLevel level;
        String message;

        if (text.contains("[INFO] [STDERR]")) {
            message = text.substring(text.indexOf("[INFO] [STDERR]"));
            level = LogLevel.WARN;
        } else if (text.contains("[INFO]")) {
            message = text.substring(text.indexOf("[INFO]"));

            if (message.contains("CONFLICT")) {
                level = LogLevel.ERROR;
            } else if (message.contains("overwriting existing item")) {
                level = LogLevel.WARN;
            } else {
                level = LogLevel.INFO;
            }
        } else if (text.contains("[WARNING]")) {
            message = text.substring(text.indexOf("[WARNING]"));
            level = LogLevel.WARN;
        } else if (text.contains("WARNING:")) {
            message = text.substring(text.indexOf("WARNING:"));
            level = LogLevel.WARN;
        } else if (text.contains("INFO:")) {
            message = text.substring(text.indexOf("INFO:"));
            level = LogLevel.INFO;
        } else if (text.contains("Exception")) {
            message = text;
            level = LogLevel.ERROR;
        } else if (text.contains("[SEVERE]")) {
            message = text.substring(text.indexOf("[SEVERE]"));
            level = LogLevel.ERROR;
        } else if (text.contains("[Sound Library Loader/ERROR]")) {
            message = text.substring(text.indexOf("[Sound Library Loader/ERROR]"));
            level = LogLevel.ERROR;
        } else if (text.contains("[Sound Library Loader/WARN]")) {
            message = text.substring(text.indexOf("[Sound Library Loader/WARN]"));
            level = LogLevel.WARN;
        } else if (text.contains("[Sound Library Loader/INFO]")) {
            message = text.substring(text.indexOf("[Sound Library Loader/INFO]"));
            level = LogLevel.INFO;
        } else if (text.contains("[MCO Availability Checker #1/ERROR]")) {
            message = text.substring(text.indexOf("[MCO Availability Checker #1/ERROR]"));
            level = LogLevel.ERROR;
        } else if (text.contains("[MCO Availability Checker #1/WARN]")) {
            message = text.substring(text.indexOf("[MCO Availability Checker #1/WARN]"));
            level = LogLevel.WARN;
        } else if (text.contains("[MCO Availability Checker #1/INFO]")) {
            message = text.substring(text.indexOf("[MCO Availability Checker #1/INFO]"));
            level = LogLevel.INFO;
        } else if (text.contains("[Client thread/ERROR]")) {
            message = text.substring(text.indexOf("[Client thread/ERROR]"));
            level = LogLevel.ERROR;
        } else if (text.contains("[Client thread/FATAL]")) {
            message = text.substring(text.indexOf("[Client thread/FATAL]"));
            level = LogLevel.ERROR;
        } else if (text.contains("[Client thread/WARN]")) {
            message = text.substring(text.indexOf("[Client thread/WARN]"));
            level = LogLevel.WARN;
        } else if (text.contains("[Client thread/INFO]")) {
            message = text.substring(text.indexOf("[Client thread/INFO]"));
            level = LogLevel.INFO;
        } else if (text.contains("[Server thread/ERROR]")) {
            message = text.substring(text.indexOf("[Server thread/ERROR]"));
            level = LogLevel.ERROR;
        } else if (text.contains("[Server thread/WARN]")) {
            message = text.substring(text.indexOf("[Server thread/WARN]"));
            level = LogLevel.WARN;
        } else if (text.contains("[Server thread/INFO]")) {
            message = text.substring(text.indexOf("[Server thread/INFO]"));
            level = LogLevel.INFO;
        } else if (text.contains("[main/ERROR]")) {
            message = text.substring(text.indexOf("[main/ERROR]"));
            level = LogLevel.ERROR;
        } else if (text.contains("[main/WARN]")) {
            message = text.substring(text.indexOf("[main/WARN]"));
            level = LogLevel.WARN;
        } else if (text.contains("[main/INFO]")) {
            message = text.substring(text.indexOf("[main/INFO]"));
            level = LogLevel.INFO;
        } else {
            message = text;
            level = LogLevel.INFO;
        }

        return new Object[]{level, message};
    }
}