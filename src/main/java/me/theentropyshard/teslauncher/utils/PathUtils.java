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
import java.nio.file.Files;
import java.nio.file.Path;

public final class PathUtils {
    public static Path createDirectories(Path path) throws IOException {
        if (Files.exists(path)) {
            return path;
        }

        return Files.createDirectories(path);
    }

    public static Path deleteDirectoryRecursively(Path path) throws IOException {
        return Files.walkFileTree(path, new DeleteFileVisitor());
    }

    private PathUtils() {
        throw new UnsupportedOperationException();
    }

    public static Path createFile(Path file) throws IOException {
        if (Files.exists(file)) {
            return file;
        }

        PathUtils.createDirectories(file.getParent());
        return Files.createFile(file);
    }
}