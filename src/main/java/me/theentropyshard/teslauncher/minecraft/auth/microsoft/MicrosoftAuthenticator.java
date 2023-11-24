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

package me.theentropyshard.teslauncher.minecraft.auth.microsoft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

public class MicrosoftAuthenticator {
    private final Gson gson;
    private final OkHttpClient httpClient;
    private final AuthListener listener;

    public MicrosoftAuthenticator(OkHttpClient httpClient, AuthListener listener) {
        this.listener = listener;
        this.gson = new GsonBuilder()
                .create();
        this.httpClient = httpClient;
    }

    // DO NOT USE MY APPLICATION (CLIENT) ID!!! YOU MUST CREATE YOUR OWN APPLICATION!!!

    public MinecraftProfile authenticate() throws IOException {
        DeviceCodeResponse deviceCodeResponse = this.getDeviceCode("consumers", "394fd08d-cb75-4f21-9807-ae14babcb4c0", "XboxLive.signin offline_access");
        //System.out.println(deviceCodeResponse);
        this.listener.onUserCodeReceived(deviceCodeResponse.userCode, deviceCodeResponse.verificationUri);
        System.out.println("Code: " + deviceCodeResponse.userCode);
        System.out.println("Url: " + deviceCodeResponse.verificationUri);

        //System.out.println();
        OAuthCodeResponse microsoftOAuthCode = this.getMicrosoftOAuthCode(deviceCodeResponse);
        //System.out.println(microsoftOAuthCode);

        //System.out.println();
        XboxLiveAuthResponse xboxLiveAuthResponse = this.authenticateWithXboxLive(microsoftOAuthCode);
        //System.out.println(xboxLiveAuthResponse);
        //System.out.println();

        XSTSAuthResponse xstsAuthResponse = this.obtainXSTSToken(xboxLiveAuthResponse);
        //System.out.println(xstsAuthResponse);
        //System.out.println();

        MinecraftAuthResponse minecraftAuthResponse = this.authenticateWithMinecraft(xstsAuthResponse);
        //System.out.println("Response:");
        //System.out.println(minecraftAuthResponse);

        if (!this.checkGameOwnership(minecraftAuthResponse)) {
            System.err.println("Account does not own Minecraft");
            return null;
        }

        return this.getProfile(minecraftAuthResponse);
    }

    private DeviceCodeResponse getDeviceCode(String tenant, String clientId, String scope) throws IOException {
        String deviceCodeUrl = String.format("https://login.microsoftonline.com/%s/oauth2/v2.0/devicecode", tenant);

        RequestBody requestBody = new FormBody(
                Arrays.asList(
                        "client_id",
                        "scope"
                ),
                Arrays.asList(
                        clientId,
                        scope
                )
        );

        Request request = new Request.Builder()
                .url(deviceCodeUrl)
                .post(requestBody)
                .build();

        try (Response response = this.httpClient.newCall(request).execute()) {
            return this.gson.fromJson(MicrosoftAuthenticator.getReader(response.body().byteStream()), DeviceCodeResponse.class);
        }
    }

    private OAuthCodeResponse getMicrosoftOAuthCode(DeviceCodeResponse deviceCodeResponse) throws IOException {
        String tokenUrl = "https://login.microsoftonline.com/consumers/oauth2/v2.0/token";

        RequestBody requestBody = new FormBody(
                Arrays.asList(
                        "grant_type",
                        "client_id",
                        "device_code"
                ),
                Arrays.asList(
                        "urn:ietf:params:oauth:grant-type:device_code",
                        "394fd08d-cb75-4f21-9807-ae14babcb4c0",
                        deviceCodeResponse.deviceCode
                )
        );

        Request request = new Request.Builder()
                .url(tokenUrl)
                .post(requestBody)
                .build();

        loop:
        while (true) {
            try (Response response = this.httpClient.newCall(request).execute()) {
                JsonObject jsonObject = this.gson.fromJson(MicrosoftAuthenticator.getReader(response.body().byteStream()), JsonObject.class);
                if (jsonObject.has("error")) {
                    String error = jsonObject.get("error").getAsString();
                    switch (error) {
                        case "authorization_pending":
                            try {
                                Thread.sleep(deviceCodeResponse.interval * 1000L);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            break;
                        case "authorization_declined":
                            break loop;
                        case "bad_verification_code":
                            System.out.println("Wrong verification code: " + deviceCodeResponse.deviceCode);
                            return null;
                        case "expired_token":
                            System.out.println("Device code expired");
                            return null;
                    }
                } else {
                    return this.gson.fromJson(jsonObject, OAuthCodeResponse.class);
                }
            }
        }

        return null;
    }

    private XboxLiveAuthResponse authenticateWithXboxLive(OAuthCodeResponse oAuthCodeResponse) throws IOException {
        String xboxAuthUrl = "https://user.auth.xboxlive.com/user/authenticate";

        XboxLiveAuthRequest authRequest = new XboxLiveAuthRequest();
        XboxAuthProperties properties = new XboxAuthProperties();
        properties.authMethod = "RPS";
        properties.siteName = "user.auth.xboxlive.com";
        properties.rpsTicket = String.format("d=%s", oAuthCodeResponse.accessToken);
        authRequest.properties = properties;
        authRequest.relyingParty = "http://auth.xboxlive.com";
        authRequest.tokenType = "JWT";

        RequestBody requestBody = RequestBody.create(this.gson.toJson(authRequest), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(xboxAuthUrl)
                .post(requestBody)
                .build();

        try (Response response = this.httpClient.newCall(request).execute()) {
            return this.gson.fromJson(MicrosoftAuthenticator.getReader(response.body().byteStream()), XboxLiveAuthResponse.class);
        }
    }

    private XSTSAuthResponse obtainXSTSToken(XboxLiveAuthResponse authResponse) throws IOException {
        String xstsUrl = "https://xsts.auth.xboxlive.com/xsts/authorize";

        XSTSTokenRequest tokenRequest = new XSTSTokenRequest();
        XSTSProperties properties = new XSTSProperties();
        properties.sandboxId = "RETAIL";
        properties.userTokens = Collections.singletonList(authResponse.token);
        tokenRequest.properties = properties;
        tokenRequest.tokenType = "JWT";
        tokenRequest.relyingParty = "rp://api.minecraftservices.com/";

        RequestBody requestBody = RequestBody.create(this.gson.toJson(tokenRequest), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(xstsUrl)
                .post(requestBody)
                .build();

        try (Response response = this.httpClient.newCall(request).execute()) {
            Reader reader = MicrosoftAuthenticator.getReader(response.body().byteStream());

            if (response.code() == 401) {
                JsonObject jsonObject = this.gson.fromJson(reader, JsonObject.class);
                String xErr = jsonObject.get("XErr").getAsString();
                String errorMsg = MicrosoftAuthenticator.getXSTSErrorMessage(xErr);
                System.out.println("Error obtaining XSTS token: " + errorMsg + " (" + xErr + ")");
                return null;
            } else {
                return this.gson.fromJson(reader, XSTSAuthResponse.class);
            }
        }
    }

    private MinecraftAuthResponse authenticateWithMinecraft(XSTSAuthResponse authResponse) throws IOException {
        String mcAuthUrl = "https://api.minecraftservices.com/authentication/login_with_xbox";

        //String json = String.format("{\"identityToken\": \"XBL3.0 x=%s;%s\"}", authResponse.displayClaims.xui.get(0).uhs, authResponse.token);

        MinecraftAuthRequest authRequest = new MinecraftAuthRequest(String.format("XBL3.0 x=%s;%s", authResponse.displayClaims.xui.get(0).uhs, authResponse.token));
        String json = this.gson.toJson(authRequest);

        RequestBody requestBody = RequestBody.create(json, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(mcAuthUrl)
                .post(requestBody)
                .build();

        try (Response response = this.httpClient.newCall(request).execute()) {
            return this.gson.fromJson(MicrosoftAuthenticator.getReader(response.body().byteStream()), MinecraftAuthResponse.class);
        }
    }

    private boolean checkGameOwnership(MinecraftAuthResponse mcResponse) throws IOException {
        String url = "https://api.minecraftservices.com/entitlements/mcstore";

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + mcResponse.accessToken)
                .get()
                .build();

        try (Response response = this.httpClient.newCall(request).execute()) {
            GameOwnershipResponse gameOwnershipResponse = this.gson.fromJson(MicrosoftAuthenticator.getReader(response.body().byteStream()), GameOwnershipResponse.class);
            return gameOwnershipResponse.items != null && !gameOwnershipResponse.items.isEmpty();
        }
    }

    private MinecraftProfile getProfile(MinecraftAuthResponse mcResponse) throws IOException {
        String url = "https://api.minecraftservices.com/minecraft/profile";

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + mcResponse.accessToken)
                .get()
                .build();

        try (Response response = this.httpClient.newCall(request).execute()) {
            MinecraftProfile profile = this.gson.fromJson(MicrosoftAuthenticator.getReader(response.body().byteStream()), MinecraftProfile.class);
            profile.accessToken = mcResponse.accessToken;
            return profile;
        }
    }

    private static Reader getReader(InputStream inputStream) {
        return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

    private static String getXSTSErrorMessage(String errorCode) {
        switch (errorCode) {
            case "2148916233":
                return "The account doesn't have an Xbox account";
            case "2148916235":
                return "The account is from a country where Xbox Live is not available/banned";
            case "2148916236":
            case "2148916237":
                return "The account needs adult verification on Xbox page";
            case "2148916238":
                return "The account is a child (under 18) and cannot proceed unless the account is added to a Family by an adult";
            default:
                return "unknown error";
        }
    }
}
