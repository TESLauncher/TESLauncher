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

package me.theentropyshard.teslauncher.minecraft.mods.fabric;

public class FabricLibrary {
    private String name;
    private String url;
    private String md5;
    private String sha1;
    private String sha256;
    private String sha512;
    private int size;

    public FabricLibrary() {

    }

    @Override
    public String toString() {
        return "FabricLibrary{" +
            "name='" + this.name + '\'' +
            ", url='" + this.url + '\'' +
            ", md5='" + this.md5 + '\'' +
            ", sha1='" + this.sha1 + '\'' +
            ", sha256='" + this.sha256 + '\'' +
            ", sha512='" + this.sha512 + '\'' +
            ", size=" + this.size +
            '}';
    }

    public String getName() {
        return this.name;
    }

    public String getUrl() {
        return this.url;
    }

    public String getMd5() {
        return this.md5;
    }

    public String getSha1() {
        return this.sha1;
    }

    public String getSha256() {
        return this.sha256;
    }

    public String getSha512() {
        return this.sha512;
    }

    public int getSize() {
        return this.size;
    }
}
