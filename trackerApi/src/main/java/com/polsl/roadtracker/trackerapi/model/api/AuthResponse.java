package com.polsl.roadtracker.trackerapi.model.api;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import javax.ws.rs.core.Response;

/**
 *
 * @author alachman
 */
public class AuthResponse extends BasicResponse {

    private String authToken;

    public AuthResponse(Response response, String reason, String authToken) {
       
        this.authToken = authToken;
    }

    public AuthResponse() {

    }

    public AuthResponse(Response response, String reason) {
              super(response, reason);
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

}
