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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class HashUtils {
    public static byte[] hash(Path file, String algorithm) throws IOException {
        try (InputStream inputStream = Files.newInputStream(file)) {
            MessageDigest md = MessageDigest.getInstance(algorithm);

            byte[] buffer = new byte[4096];

            int numRead;
            while ((numRead = inputStream.read(buffer)) != -1) {
                md.update(buffer, 0, numRead);
            }

            return md.digest();
        } catch (NoSuchAlgorithmException ex) {
            throw new IOException(algorithm + " algorithm is not available in your JRE", ex);
        }
    }

    public static String sha1(Path file) throws IOException {
        byte[] mdBytes = HashUtils.hash(file, "SHA-1");

        StringBuilder sb = new StringBuilder();
        for (byte b : mdBytes) {
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

    private HashUtils() {
        throw new UnsupportedOperationException();
    }
}
