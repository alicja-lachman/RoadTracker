package com.polsl.roadtracker.api;

import com.google.gson.annotations.SerializedName;

/**
 * Created by alachman on 18.05.2017.
 */

public class BasicResponse {
    @SerializedName("Result")
    private String result;
    @SerializedName("Reason")
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