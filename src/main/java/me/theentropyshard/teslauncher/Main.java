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

package me.theentropyshard.teslauncher;

import com.beust.jcommander.JCommander;
import org.apache.logging.log4j.LogManager;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        Args theArgs = Main.parseArgs(args);
        Path workDir = Main.resolveWorkDir(theArgs);

        System.setProperty("teslauncher.logsDir", workDir.resolve("logs").toString());

        try {
            new TESLauncher(theArgs, workDir);
        } catch (Throwable t) {
            LogManager.getLogger(Main.class).error("Unable to start the launcher", t);
            System.exit(1);
        }
    }

    private static Args parseArgs(String[] rawArgs) {
        Args args = new Args();
        JCommander.newBuilder().addObject(args).build().parse(rawArgs);

        return args;
    }

    private static Path resolveWorkDir(Args args) {
        String workDirPath = args.getWorkDirPath();

        return (workDirPath == null || workDirPath.isEmpty() ?
                Paths.get(System.getProperty("user.dir")) :
                Paths.get(workDirPath)
        ).normalize().toAbsolutePath();
    }
}
