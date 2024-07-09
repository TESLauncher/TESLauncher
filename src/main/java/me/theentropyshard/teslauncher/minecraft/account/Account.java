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

package me.theentropyshard.teslauncher.minecraft.account;

import me.theentropyshard.teslauncher.minecraft.auth.microsoft.AuthException;

import java.io.IOException;
import java.util.UUID;

public abstract class Account {
    public static final String DEFAULT_HEAD = "iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAIAAAD8GO2jAAABB0lEQVR42u3OvUuCQRzAccdIMhKC" +
            "iDIHk3gSFLQhpBKlXKQhaYkwKEgSH4RDDBTh6WXwIbAmp2dqjJp6G8LG/q3avs9veOJwv+Mz3f3u" +
            "vhdKx2ewHvOlV2axthRGYmEKycVpyLvWsi9kAtqAPPhPNjGHohXFjjWPVCwC+SETmCzw4TYxHir8" +
            "jByM75Rv2MZz5wi51ShMQB946p/j7daG3H9xbHjqGF+ugndRCWQC+oB89Fesgy0X77065KNyRt6V" +
            "nzMBfcA728Nr7xSf1wqNQibQ96CJx0YZTjUHE9AHHmoljE4KuCxuTOS+to2r6iZMQB9o7aZglyzI" +
            "Iam7n8XNYT6QnDEBbeAP9OterSp4FpcAAAAASUVORK5CYII=";

    private String username;
    private UUID uuid;
    private String accessToken;
    private String headIcon;

    public Account() {

    }

    public Account(String username, UUID uuid, String accessToken, String headIcon) {
        this.username = username;
        this.uuid = uuid;
        this.accessToken = accessToken;
        this.headIcon = headIcon;
    }

    public abstract void authenticate() throws IOException, AuthException;

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getHeadIcon() {
        return this.headIcon;
    }

    public void setHeadIcon(String headIcon) {
        this.headIcon = headIcon;
    }
}
