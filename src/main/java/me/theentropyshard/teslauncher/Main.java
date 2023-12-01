/*
 * TESLauncher - https://github.com/TESLauncher/TESLauncher
 * Copyright (C) 2023 TESLauncher
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
import me.theentropyshard.teslauncher.log4j.Log4jConfigurator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] rawArgs) {
        Args args = new Args();
        JCommander.newBuilder().addObject(args).build().parse(rawArgs);

        String workDirPath = args.getWorkDirPath();
        Path workDir = (workDirPath == null || workDirPath.isEmpty() ?
                Paths.get(System.getProperty("user.dir", ".")) :
                Paths.get(workDirPath)).normalize().toAbsolutePath();

        System.setProperty("teslauncher.workDir", workDir.toString());

        Log4jConfigurator.configure();
        Logger logger = LogManager.getLogger(TESLauncher.class);

        new TESLauncher(args, logger, workDir);
    }
}
