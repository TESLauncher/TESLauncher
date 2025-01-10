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

package me.theentropyshard.teslauncher.minecraft.data.rule;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class Rule {
    @SerializedName("action")
    private Action action;

    @SerializedName("os")
    private OperatingSystemFilter operatingSystem;

    @SerializedName("features")
    private FeaturesFilter features;

    public Rule() {

    }

    @Override
    public String toString() {
        return "Rule{" +
                "action=" + this.action +
                ", os=" + this.operatingSystem +
                ", features=" + this.features +
                '}';
    }

    public Action getAction() {
        return this.action;
    }

    public OperatingSystemFilter getOperatingSystem() {
        return this.operatingSystem;
    }

    public FeaturesFilter getFeatures() {
        return this.features;
    }

    public enum Action {
        ALLOW("allow"),
        DISALLOW("disallow");

        private static final Map<String, Action> lookup = new HashMap<>();

        static {
            for (Action type : Action.values()) {
                lookup.put(type.getJsonName(), type);
            }
        }

        public static Action getByName(String jsonName) {
            Action type = lookup.get(jsonName);

            if (type == null) {
                throw new IllegalArgumentException("jsonName: " + jsonName);
            }

            return type;
        }

        private final String jsonName;

        Action(String jsonName) {
            this.jsonName = jsonName;
        }

        public String getJsonName() {
            return this.jsonName;
        }
    }
}
