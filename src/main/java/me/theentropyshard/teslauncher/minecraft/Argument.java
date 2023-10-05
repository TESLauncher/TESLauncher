/*
 * Copyright 2023 TheEntropyShard (https://github.com/TheEntropyShard)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.theentropyshard.teslauncher.minecraft;

import com.google.gson.annotations.JsonAdapter;
import me.theentropyshard.teslauncher.gson.AlwaysListTypeAdapterFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Argument {
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
    public String toString() {
        return "Argument{" +
                "rules=" + this.rules +
                ", value=" + this.value +
                '}';
    }
}
