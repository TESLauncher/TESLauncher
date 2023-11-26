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

package me.theentropyshard.teslauncher.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class FileUtils {
    private static final FileVisitor<Path> DELETE_VISITOR = new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(file);

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
            Files.delete(dir);

            return FileVisitResult.CONTINUE;
        }
    };

    public static void deleteDirectoryRecursively(Path path) throws IOException {
        Files.walkFileTree(path, FileUtils.DELETE_VISITOR);
    }

    public static void createDirectoryIfNotExists(Path path) throws IOException {
        if (Files.exists(path)) {
            return;
        }

        Files.createDirectories(path);
    }

    public static void createFileIfNotExists(Path file) throws IOException {
        if (Files.exists(file)) {
            return;
        }

        Files.createDirectories(file.getParent());
        Files.createFile(file);
    }

    private FileUtils() {
        throw new UnsupportedOperationException();
    }
}
