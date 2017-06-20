package com.polsl.roadtracker.model;

import com.google.gson.annotations.SerializedName;
import com.polsl.roadtracker.util.Constants;

/**
 * Created by alachman on 20.05.2017.
 */

public class LogoutData {
    @SerializedName("AuthToken")
    private String authToken;
    @SerializedName("useragent")
    private String userAgent;

    public LogoutData(String authToken) {
        this.authToken = authToken;
        this.userAgent = Constants.USER_AGENT;
    }

    public LogoutData(String authToken, String userAgent) {
        this.authToken = authToken;
        this.userAgent = userAgent;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }


}