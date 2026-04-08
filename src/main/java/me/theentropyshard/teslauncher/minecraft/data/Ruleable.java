/*
 * TESLauncher - https://github.com/TESLauncher/TESLauncher
 * Copyright (C) 2023-2026 TESLauncher
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

package me.theentropyshard.teslauncher.minecraft.data;

import java.util.List;
import java.util.regex.Pattern;

import me.theentropyshard.teslauncher.minecraft.data.rule.OperatingSystemFilter;
import me.theentropyshard.teslauncher.minecraft.data.rule.Rule;
import me.theentropyshard.teslauncher.minecraft.data.rule.VersionRangeFilter;
import me.theentropyshard.teslauncher.minecraft.download.MinecraftDownloader;
import me.theentropyshard.teslauncher.utils.OperatingSystem;
import me.theentropyshard.teslauncher.utils.SemanticVersion;

public interface Ruleable {
    List<Rule> getRules();

    default boolean applyOnThisPlatform() {
        List<Rule> rules = this.getRules();

        Rule.Action lastAction = Rule.Action.DISALLOW;
        if (rules == null || rules.isEmpty()) {
            lastAction = Rule.Action.ALLOW;
        } else {
            for (Rule rule : rules) {
                OperatingSystemFilter os = rule.getOperatingSystem();
                if (os == null) {
                    lastAction = rule.getAction();
                } else {
                    if (MinecraftDownloader.getMcName().equals(os.getName())) {
                        if (os.getVersion() == null && os.getArch() == null && os.getVersionRange() == null) {
                            lastAction = rule.getAction();

                            continue;
                        }

                        boolean versionMatches = os.getVersion() != null &&
                            Pattern.compile(os.getVersion()).matcher(OperatingSystem.getVersion()).matches();

                        boolean archMatches = os.getArch() != null &&
                            OperatingSystem.getArch().equals(os.getArch());

                        VersionRangeFilter versionRange = os.getVersionRange();
                        boolean windowsVersionMatches = false;

                        if (OperatingSystem.isWindows() && versionRange != null) {
                            // Assuming here that min is inclusive and max is exclusive, otherwise, for example,
                            // two garbage collectors would be selected which is incorrect

                            if (versionRange.getMin() != null) {
                                SemanticVersion minVersion = SemanticVersion.parse(versionRange.getMin());
                                if (OperatingSystem.windowsVersion.equals(minVersion) || OperatingSystem.windowsVersion.isHigherThan(minVersion)) {
                                    windowsVersionMatches = true;
                                }
                            } else if (versionRange.getMax() != null) {
                                SemanticVersion maxVersion = SemanticVersion.parse(versionRange.getMax());
                                if (OperatingSystem.windowsVersion.isLowerThan(maxVersion)) {
                                    windowsVersionMatches = true;
                                }
                            } else if (versionRange.getMin() != null && versionRange.getMax() != null) {
                                SemanticVersion minVersion = SemanticVersion.parse(versionRange.getMin());
                                SemanticVersion maxVersion = SemanticVersion.parse(versionRange.getMin());
                                if ((OperatingSystem.windowsVersion.equals(minVersion) || OperatingSystem.windowsVersion.isHigherThan(minVersion))
                                    && OperatingSystem.windowsVersion.isLowerThan(maxVersion)) {
                                    windowsVersionMatches = true;
                                }
                            }
                        }

                        if (versionMatches || archMatches || windowsVersionMatches) {
                            lastAction = rule.getAction();
                        }
                    }
                }
            }
        }

        return lastAction == Rule.Action.ALLOW;
    }
}
