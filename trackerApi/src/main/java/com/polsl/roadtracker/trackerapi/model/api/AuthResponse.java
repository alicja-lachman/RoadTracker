package com.polsl.roadtracker.trackerapi.model.api;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author alachman
 */
public class AuthResponse extends BasicResponse {

    private String AuthToken;

    public AuthResponse(String apiResult, String reason, String authToken) {
        super(apiResult, reason);
        this.AuthToken = authToken;
    }

    public AuthResponse() {

    }

    public AuthResponse(String apiResult, String reason) {
        super(apiResult, reason);
    }

    public String getAuthToken() {
        return AuthToken;
    }

    public void setAuthToken(String authToken) {
        this.AuthToken = authToken;
    }

}
