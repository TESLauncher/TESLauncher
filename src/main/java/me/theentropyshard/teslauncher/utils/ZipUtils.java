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

import net.lingala.zip4j.model.FileHeader;

import java.util.List;

public final class ZipUtils {
    public static String findTopLevelDirectory(List<FileHeader> fileHeaders) {
        String topLevelDir = null;

        for (FileHeader fileHeader : fileHeaders) {
            String fileName = fileHeader.getFileName();
            if (!fileName.substring(0, fileName.length() - 1).contains("/")) {
                topLevelDir = fileName;

                break;
            }
        }

        return topLevelDir;
    }

    private ZipUtils() {
        throw new UnsupportedOperationException();
    }
}