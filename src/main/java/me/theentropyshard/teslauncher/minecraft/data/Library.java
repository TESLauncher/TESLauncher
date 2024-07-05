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

package me.theentropyshard.teslauncher.minecraft.data;

import me.theentropyshard.teslauncher.minecraft.data.rule.Rule;

import java.util.List;
import java.util.Map;

public class Library implements Ruleable {
    private String name;
    private DownloadList downloads;
    private List<Rule> rules;
    private Map<String, String> natives;
    private ExtractRules extract;

    public Library() {

    }

    @Override
    public String toString() {
        return "Library{" +
                "name='" + this.name + '\'' +
                ", downloads=" + this.downloads +
                ", rules=" + this.rules +
                ", natives=" + this.natives +
                ", extract=" + this.extract +
                '}';
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

        @Override
        public String toString() {
            return "Artifact{" +
                    "path='" + this.path + '\'' +
                    ", sha1='" + this.sha1 + '\'' +
                    ", size=" + this.size +
                    ", url='" + this.url + '\'' +
                    '}';
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

        @Override
        public String toString() {
            return "DownloadList{" +
                    "artifact=" + this.artifact +
                    ", classifiers=" + this.classifiers +
                    '}';
        }

        public Artifact getArtifact() {
            return this.artifact;
        }

        public Map<String, Artifact> getClassifiers() {
            return this.classifiers;
        }
    }
}
