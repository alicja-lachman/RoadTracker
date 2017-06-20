/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.polsl.roadtracker.trackerapi.model.api;

/**
 *
 * @author alachman
 */
public class LogoutData {
    private String AuthToken;
    private String useragent;

    public LogoutData() {
    }

    public LogoutData(String authToken, String userAgent) {
        this.AuthToken = authToken;
        this.useragent = userAgent;
    }

    public String getAuthToken() {
        return AuthToken;
    }

    public void setAuthToken(String authToken) {
        this.AuthToken = authToken;
    }

    public String getUserAgent() {
        return useragent;
    }

    public void setUserAgent(String userAgent) {
        this.useragent = userAgent;
    }
    
    
}
