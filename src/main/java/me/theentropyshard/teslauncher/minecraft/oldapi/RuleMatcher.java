/*
 * TESLauncher - https://github.com/TESLauncher/TESLauncher
 * Copyright (C) 2023 TESLauncher
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

package me.theentropyshard.teslauncher.minecraft.oldapi;

import me.theentropyshard.teslauncher.utils.EnumOS;

import java.util.List;
import java.util.regex.Pattern;

public class RuleMatcher {
    public static boolean applyOnThisPlatform(Ruleable ruleable) {
        List<Rule> rules = ruleable.getRules();
        Rule.Action lastAction = Rule.Action.DISALLOW;
        if (rules == null || rules.isEmpty()) {
            lastAction = Rule.Action.ALLOW;
        } else {
            for (Rule rule : rules) {
                Os os = rule.os;
                if (os == null) {
                    lastAction = rule.action;
                } else {
                    boolean versionMatches = os.version != null &&
                            Pattern.compile(os.version).matcher(EnumOS.getVersion()).matches();
                    if (EnumOS.getOsName().equals(os.name) ||
                            versionMatches || EnumOS.getArch().equals(os.arch)) {
                        lastAction = rule.action;
                    }
                }
            }
        }

        return lastAction == Rule.Action.ALLOW;
    }
}
