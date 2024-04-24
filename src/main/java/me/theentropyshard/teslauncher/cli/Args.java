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

package me.theentropyshard.teslauncher.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Args {
    @Parameter(names = {"--workDir"})
    private String workDirPath;

    private final List<String> unknownOptions;

    private Args() {
        this.unknownOptions = new ArrayList<>();
    }

    public static Args parse(String[] rawArgs) {
        Args args = new Args();

        JCommander commander = JCommander.newBuilder()
                .acceptUnknownOptions(true)
                .programName("TESLauncher")
                .addObject(args)
                .build();

        commander.parse(rawArgs);

        args.getUnknownOptions().addAll(commander.getUnknownOptions());

        return args;
    }

    public boolean hasUnknownOptions() {
        return this.unknownOptions.size() > 0;
    }

    public List<String> getUnknownOptions() {
        return this.unknownOptions;
    }

    public Path getWorkDir() {
        return (this.workDirPath == null || this.workDirPath.isEmpty() ?
                Paths.get(System.getProperty("user.dir")) :
                Paths.get(this.workDirPath)
        ).normalize().toAbsolutePath();
    }
}
