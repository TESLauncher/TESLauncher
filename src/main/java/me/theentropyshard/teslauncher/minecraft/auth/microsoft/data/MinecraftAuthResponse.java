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

package me.theentropyshard.teslauncher.minecraft.auth.microsoft.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MinecraftAuthResponse {
    @SerializedName("username")
    public String username;

    @SerializedName("roles")
    public List<Object> roles;

    @SerializedName("access_token")
    public String accessToken;

    @SerializedName("token_type")
    public String tokenType;

    @SerializedName("expires_in")
    public int expiresIn;

    @Override
    public String toString() {
        return "MinecraftAuthResponse{" +
                "username='" + this.username + '\'' +
                ", roles=" + this.roles +
                ", accessToken='" + this.accessToken + '\'' +
                ", tokenType='" + this.tokenType + '\'' +
                ", expiresIn=" + this.expiresIn +
                '}';
    }
}
