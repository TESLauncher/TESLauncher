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
