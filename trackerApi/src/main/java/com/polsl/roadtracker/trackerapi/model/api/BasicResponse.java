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
    private String Result;
    private String Reason;

    public BasicResponse(String apiResult, String reason) {
        this.Result = apiResult;
        this.Reason = reason;
    }

    public BasicResponse() {
    }

    public BasicResponse(String apiResult) {
        this.Result = apiResult;
    }

    public String getApiResult() {
        return Result;
    }

    public void setApiResult(String apiResult) {
        this.Result = apiResult;
    }

    public String getReason() {
        return Reason;
    }

    public void setReason(String reason) {
        this.Reason = reason;
    }


    
}
