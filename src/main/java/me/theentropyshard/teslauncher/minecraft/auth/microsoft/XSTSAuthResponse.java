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

package me.theentropyshard.teslauncher.minecraft.auth.microsoft;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class XSTSAuthResponse {
    @SerializedName("IssueInstant")
    public String issueInstant;

    @SerializedName("NotAfter")
    public String notAfter;

    @SerializedName("Token")
    public String token;

    @SerializedName("DisplayClaims")
    public DisplayClaims displayClaims;

    public static final class UserHash {
        public String uhs;

        @Override
        public String toString() {
            return "UserHash{" +
                    "uhs='" + this.uhs + '\'' +
                    '}';
        }
    }

    public static final class DisplayClaims {
        public List<UserHash> xui;

        @Override
        public String toString() {
            return "DisplayClaims{" +
                    "xui=" + this.xui +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "XSTSAuthResponse{" +
                "issueInstant='" + this.issueInstant + '\'' +
                ", notAfter='" + this.notAfter + '\'' +
                ", token='" + this.token + '\'' +
                ", displayClaims=" + this.displayClaims +
                '}';
    }
}
