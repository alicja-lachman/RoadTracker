package com.polsl.roadtracker.api;

import com.google.gson.annotations.SerializedName;

import okhttp3.Response;

/**
 * Created by alachman on 18.05.2017.
 */

public class AuthResponse extends BasicResponse {
    @SerializedName("AuthToken")
    private String authToken;

    public AuthResponse(Response response, String reason, String authToken) {

        this.authToken = authToken;
    }

    public AuthResponse() {

    }

    public AuthResponse(String result, String reason) {
        super(result, reason);
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

}