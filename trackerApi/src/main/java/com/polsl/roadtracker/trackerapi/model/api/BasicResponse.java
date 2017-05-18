/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.polsl.roadtracker.trackerapi.model.api;

import javax.ws.rs.core.Response;

/**
 *
 * @author alachman
 */
public class BasicResponse {
    private Response response;
    private String reason;

    public BasicResponse(Response response, String reason) {
        this.response = response;
        this.reason = reason;
    }

    public BasicResponse() {
    }

    public BasicResponse(Response response) {
        this.response = response;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }


    
}
