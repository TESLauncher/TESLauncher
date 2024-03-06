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
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public static void delete(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }

        if (Files.isRegularFile(path)) {
            Files.delete(path);
        }

        Files.walkFileTree(path, FileUtils.DELETE_VISITOR);
    }

    public static void createDirectoryIfNotExists(Path dir) throws IOException {
        if (FileUtils.existsButIsNotADirectory(dir)) {
            throw new IOException(dir + " exists, but is not a directory");
        }

        Files.createDirectories(dir);
    }

    public static void createFileIfNotExists(Path file) throws IOException {
        if (FileUtils.existsButIsNotAFile(file)) {
            throw new IOException(file + " exists, but is not a file");
        }

        if (Files.exists(file)) {
            return;
        }

        Files.createDirectories(file.getParent());
        Files.createFile(file);
    }

    public static void writeUtf8(Path file, String s) throws IOException {
        if (FileUtils.existsButIsNotAFile(file)) {
            throw new IOException(file + " exists, but is not a file");
        }

        Files.write(file, s.getBytes(StandardCharsets.UTF_8));
    }

    public static String readUtf8(Path file) throws IOException {
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    }

    public static List<Path> list(Path dir) throws IOException {
        if (FileUtils.existsButIsNotADirectory(dir)) {
            throw new IOException(dir + " exists, but is not a directory");
        }

        try (Stream<Path> pathStream = Files.list(dir)) {
            return pathStream.collect(Collectors.toList());
        }
    }

    public static List<Path> walk(Path dir) throws IOException {
        if (FileUtils.existsButIsNotADirectory(dir)) {
            throw new IOException(dir + " exists, but is not a directory");
        }

        try (Stream<Path> walked = Files.walk(dir)) {
            return walked.collect(Collectors.toList());
        }
    }

    private static boolean existsButIsNotAFile(Path path) {
        return Files.exists(path) && !Files.isRegularFile(path);
    }

    private static boolean existsButIsNotADirectory(Path path) {
        return Files.exists(path) && !Files.isDirectory(path);
    }

    private FileUtils() {
        throw new UnsupportedOperationException();
    }
}
