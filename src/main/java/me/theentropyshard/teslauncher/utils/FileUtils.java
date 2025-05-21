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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class FileUtils {
    private static final FileVisitor<Path> DELETE_VISITOR = new SimpleFileVisitor<>() {
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

    private static final class CountFileVisitor extends SimpleFileVisitor<Path> {
        private int filesCount;

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            this.filesCount++;

            return FileVisitResult.CONTINUE;
        }

        public int getFilesCount() {
            return this.filesCount;
        }
    }

    public static void rename(Path path, String newName) throws IOException {
        Path parentDir = path.getParent();
        Path targetPath = parentDir.resolve(newName);

        Files.move(path, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    public static boolean isPathInvalid(String path) {
        try {
            Path file = Paths.get(path).toAbsolutePath();

            if (Files.exists(file)) {
                return false;
            }

            FileUtils.createFileIfNotExists(file);
            FileUtils.delete(file);
        } catch (InvalidPathException | IOException e) {
            return true;
        }

        return false;
    }

    public static int countFiles(Path dir) throws IOException {
        if (!Files.isDirectory(dir)) {
            throw new IOException(dir + " was expected to be a directory");
        }

        CountFileVisitor visitor = new CountFileVisitor();

        Files.walkFileTree(dir, visitor);

        return visitor.getFilesCount();
    }

    public static String sanitizeFileName(String dirtyName) {
        return dirtyName.replaceAll("[^a-zA-Z0-9_.]", "");
    }

    public static void delete(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }

        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, FileUtils.DELETE_VISITOR);
        } else {
            Files.delete(path);
        }
    }

    public static void copyDirectory(Path src, Path dest) throws IOException {
        List<Path> walked = FileUtils.walk(src);

        FileUtils.createDirectoryIfNotExists(dest);

        for (Path path : walked) {
            Files.copy(path, dest.resolve(src.relativize(path)), StandardCopyOption.REPLACE_EXISTING);
        }
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
        FileUtils.write(file, s, StandardCharsets.UTF_8);
    }

    public static void write(Path file, String s, Charset charset) throws IOException {
        if (FileUtils.existsButIsNotAFile(file)) {
            throw new IOException(file + " exists, but is not a file");
        }

        Files.createDirectories(file.getParent());
        Files.writeString(file, s, charset);
    }

    public static String readUtf8(Path file) throws IOException {
        return FileUtils.read(file, StandardCharsets.UTF_8);
    }

    public static String read(Path file, Charset charset) throws IOException {
        return Files.readString(file, charset);
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
