/*
 * Copyright 2023 TheEntropyShard (https://github.com/TheEntropyShard)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.theentropyshard.teslauncher.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class PathUtils {
    public static Path createDirectoryIfNotExists(Path path) throws IOException {
        if (Files.exists(path)) {
            return path;
        }

        return Files.createDirectories(path);
    }

    public static Path deleteDirectoryRecursively(Path path) throws IOException {
        return Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);

                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static Path createFileIfNotExists(Path file) throws IOException {
        if (Files.exists(file)) {
            return file;
        }

        PathUtils.createDirectoryIfNotExists(file.getParent());
        return Files.createFile(file);
    }

    public static Path writeString(Path path, String s) throws IOException {
        return Files.write(path, s.getBytes(StandardCharsets.UTF_8));
    }

    public static Path appendString(Path path, String s) throws IOException {
        return Files.write(path, s.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
    }

    public static String sha1(Path path) throws IOException {
        try (InputStream inputStream = Files.newInputStream(path)) {
            MessageDigest md = MessageDigest.getInstance("SHA-1");

            byte[] dataBytes = new byte[1024];

            int numRead;
            while ((numRead = inputStream.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, numRead);
            }

            byte[] mdBytes = md.digest();

            StringBuilder sb = new StringBuilder();
            for (byte b : mdBytes) {
                sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IOException(ex);
        }
    }

    private PathUtils() {
        throw new UnsupportedOperationException();
    }
}
