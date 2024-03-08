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

import java.util.Map;

public class Rule {
    public Action action;
    public Map<String, Boolean> features;
    public Os os;

    public enum Action {
        ALLOW,
        DISALLOW;

        public static Action getByName(String name) {
            if (name.equals("allow")) {
                return Action.ALLOW;
            }

            return Action.DISALLOW;
        }

        public String getJsonName() {
            return this == Action.ALLOW ? "allow" : "disallow";
        }
    }

    @Override
    public String toString() {
        return "Rule{" +
                "action=" + this.action +
                ", features=" + this.features +
                ", os=" + this.os +
                '}';
    }
}
