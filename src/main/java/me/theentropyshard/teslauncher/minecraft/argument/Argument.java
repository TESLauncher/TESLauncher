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

package me.theentropyshard.teslauncher.minecraft.argument;

import com.google.gson.annotations.JsonAdapter;
import me.theentropyshard.teslauncher.minecraft.Ruleable;
import me.theentropyshard.teslauncher.minecraft.rule.Rule;
import me.theentropyshard.teslauncher.utils.json.type.AlwaysListTypeAdapterFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Argument implements Ruleable {
    @JsonAdapter(AlwaysListTypeAdapterFactory.class)
    private List<String> value;
    private List<Rule> rules;

    public Argument() {

    }

    public static Argument withValues(String... values) {
        Argument argument = new Argument();
        argument.value = Arrays.asList(values);
        argument.rules = new ArrayList<>();
        return argument;
    }

    @Override
    public String toString() {
        return "Argument{" +
                "value=" + this.value +
                ", rules=" + this.rules +
                '}';
    }

    public List<String> getValue() {
        return this.value;
    }

    public List<Rule> getRules() {
        return this.rules;
    }
}
