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

import me.theentropyshard.teslauncher.minecraft.rule.Rule;

import java.util.List;
import java.util.Map;

public class Library {
    private String name;
    private DownloadList downloads;
    private List<Rule> rules;
    private Map<String, String> natives;
    private ExtractRules extract;

    public Library() {

    }

    public boolean applies() {
        if (this.rules == null) {
            return true;
        }

        boolean result = true;

        for (Rule rule : this.rules) {
            result = rule.applies();
        }

        return result;
    }

    public String getName() {
        return this.name;
    }

    public DownloadList getDownloads() {
        return this.downloads;
    }

    public List<Rule> getRules() {
        return this.rules;
    }

    public Map<String, String> getNatives() {
        return this.natives;
    }

    public ExtractRules getExtract() {
        return this.extract;
    }

    public static final class Artifact {
        private String path;
        private String sha1;
        private long size;
        private String url;

        public Artifact() {

        }

        public String getPath() {
            return this.path;
        }

        public String getSha1() {
            return this.sha1;
        }

        public long getSize() {
            return this.size;
        }

        public String getUrl() {
            return this.url;
        }
    }

    public static final class DownloadList {
        private Artifact artifact;
        private Map<String, Artifact> classifiers;

        public DownloadList() {

        }

        public Artifact getArtifact() {
            return this.artifact;
        }

        public Map<String, Artifact> getClassifiers() {
            return this.classifiers;
        }
    }
}
