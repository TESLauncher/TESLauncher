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

package me.theentropyshard.teslauncher.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class ProcessReader {
    private final Process process;
    private final Charset charset;

    public ProcessReader(Process process) {
        this(process, StandardCharsets.UTF_8);
    }

    public ProcessReader(Process process, Charset charset) {
        this.process = process;
        this.charset = charset;
    }

    public void read(Consumer<String> log) throws IOException {
        InputStream inputStream = this.process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, this.charset));
        String line;
        while ((line = reader.readLine()) != null) {
            log.accept(line);
        }
    }
}