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

package me.theentropyshard.teslauncher.gui.utils;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import me.theentropyshard.teslauncher.TESLauncher;

import java.util.HashMap;
import java.util.Map;

public final class SvgIcon {
    private static final Map<String, FlatSVGIcon> icons = new HashMap<>();

    public static FlatSVGIcon get(String name) {
        if (SvgIcon.icons.containsKey(name)) {
            return SvgIcon.icons.get(name);
        }

        String filePath = "/assets/images/" + name + (TESLauncher.getInstance().getSettings().darkTheme ? "_dark" : "") + ".svg";

        FlatSVGIcon icon = new FlatSVGIcon(SvgIcon.class.getResource(filePath));
        SvgIcon.icons.put(name, icon);

        return icon;
    }

    public static void clear() {
        SvgIcon.icons.clear();
    }
}