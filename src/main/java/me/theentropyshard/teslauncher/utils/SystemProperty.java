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

public class SystemProperty {
    private final String property;
    private final Object value;

    public SystemProperty(String property) {
        this(property, null);
    }

    public SystemProperty(String property, Object value) {
        this.property = property;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        SystemProperty that = (SystemProperty) o;
        return Objects.equals(this.property, that.property) && Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.property, this.value);
    }

    public SystemProperty copy(Object newValue) {
        return new SystemProperty(this.property, newValue);
    }

    public void install() {
        if (this.value == null) {
            throw new NullPointerException("value must not be null");
        }

        System.setProperty(this.property, String.valueOf(this.value));
    }

    public void install(Object value) {
        System.setProperty(this.property, String.valueOf(value));
    }

    public String get() {
        return this.get(null);
    }

    public String get(String def) {
        String p = System.getProperty(this.property);

        if (p == null) {
            return def;
        }

        return p;
    }

    public String asJvmArg() {
        if (this.value == null) {
            throw new NullPointerException("value must not be null");
        }

        return "-D" + this.property + "=" + this.value;
    }

    public String asJvmArg(Object value) {
        return "-D" + this.property + "=" + value;
    }
}