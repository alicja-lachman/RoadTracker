package com.polsl.roadtracker.api;

/**
 * Created by alachman on 18.05.2017.
 */

public class BasicResponse {
    private String result;
    private String reason;

    public BasicResponse(String result, String reason) {
        this.result = result;
        this.reason = reason;
    }

    public BasicResponse() {
    }

    public BasicResponse(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }


}