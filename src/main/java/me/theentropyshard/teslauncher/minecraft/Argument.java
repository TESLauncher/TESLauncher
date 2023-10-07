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

package me.theentropyshard.teslauncher.minecraft;

import com.google.gson.annotations.JsonAdapter;
import me.theentropyshard.teslauncher.gson.AlwaysListTypeAdapterFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Argument implements Ruleable {
    public List<Rule> rules;
    @JsonAdapter(AlwaysListTypeAdapterFactory.class)
    public List<String> value;

    public static Argument withValues(String... values) {
        Argument argument = new Argument();
        argument.value = Arrays.asList(values);
        argument.rules = new ArrayList<>();
        return argument;
    }

    @Override
    public List<Rule> getRules() {
        return this.rules;
    }

    @Override
    public String toString() {
        return "Argument{" +
                "rules=" + this.rules +
                ", value=" + this.value +
                '}';
    }
}
