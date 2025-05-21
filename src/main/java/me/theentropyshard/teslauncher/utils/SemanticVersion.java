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

import java.util.Objects;

public class SemanticVersion implements Comparable<SemanticVersion> {
    private final int major;
    private final int minor;
    private final int patch;

    public SemanticVersion(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public static SemanticVersion parse(String s) {
        try {
            return SemanticVersion.tryParse(s);
        } catch (ParseException e) {
            return null;
        }
    }

    public static SemanticVersion tryParse(String version) throws ParseException {
        if (version == null) {
            throw new NullPointerException("version == null");
        }

        String[] parts = version.split("\\.");

        if (parts.length != 3) {
            throw new ParseException("Semantic version must have 3 numbers separated with a dot");
        }

        int major;
        try {
            major = Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            throw new ParseException("Major must be an integer: " + version);
        }

        int minor;
        try {
            minor = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            throw new ParseException("Minor must be an integer: " + version);
        }

        int patch;
        try {
            patch = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            throw new ParseException("Patch must be an integer: " + version);
        }

        return new SemanticVersion(major, minor, patch);
    }

    public boolean isHigherThan(SemanticVersion other) {
        return this.compareTo(other) > 0;
    }

    public boolean isLowerThan(SemanticVersion other) {
        return this.compareTo(other) < 0;
    }

    public static class ParseException extends Exception {
        public ParseException(String message) {
            super(message);
        }
    }

    @Override
    public int compareTo(SemanticVersion version) {
        int majorCompare = Integer.compare(this.major, version.getMajor());
        int minorCompare = Integer.compare(this.minor, version.getMinor());
        int patchCompare = Integer.compare(this.patch, version.getPatch());

        if (majorCompare == 0) {
            if (minorCompare == 0) {
                return patchCompare;
            } else {
                return minorCompare;
            }
        } else {
            return majorCompare;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        SemanticVersion version = (SemanticVersion) o;
        return this.major == version.major && this.minor == version.minor && this.patch == version.patch;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.major, this.minor, this.patch);
    }

    public String toVersionString() {
        return this.major + "." + this.minor + "." + this.patch;
    }

    @Override
    public String toString() {
        return "SemanticVersion{" +
            "major=" + this.major +
            ", minor=" + this.minor +
            ", patch=" + this.patch +
            '}';
    }

    public int getMajor() {
        return this.major;
    }

    public int getMinor() {
        return this.minor;
    }

    public int getPatch() {
        return this.patch;
    }
}