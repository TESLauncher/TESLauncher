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

public final class ResourceUtils {
    public static byte[] readToByteArray(String path) throws IOException {
        return StreamUtils.readToByteArray(ResourceUtils.class.getResourceAsStream(path));
    }

    public static String readToString(String path) throws IOException {
        return StreamUtils.readToString(ResourceUtils.class.getResourceAsStream(path));
    }

    private ResourceUtils() {
        throw new UnsupportedOperationException();
    }
}
