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
public class BasicResponse {
    private String result;
    private String reason;

    public BasicResponse(String apiResult, String reason) {
        this.result = apiResult;
        this.reason = reason;
    }

    public BasicResponse() {
    }

    public BasicResponse(String apiResult) {
        this.result = apiResult;
    }

    public String getApiResult() {
        return result;
    }

    public void setApiResult(String apiResult) {
        this.result = apiResult;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }


    
}
