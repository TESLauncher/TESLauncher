/*
 * TESLauncher - https://github.com/TESLauncher/TESLauncher
 * Copyright (C) 2023-2024 TESLauncher
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

package me.theentropyshard.teslauncher;

public final class AppVersion {
    private static final String VERSION = AppVersion.class.getPackage().getImplementationVersion();
    private static final String FALLBACK_VERSION = "0.0.0";

    public static String getVersion() {
        return AppVersion.VERSION == null ? AppVersion.FALLBACK_VERSION : AppVersion.VERSION;
    }

    private AppVersion() {
        throw new UnsupportedOperationException();
    }
}
