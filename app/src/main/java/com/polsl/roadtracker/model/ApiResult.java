package com.polsl.roadtracker.model;

/**
 * Created by alachman on 18.05.2017.
 */

public enum ApiResult {
    RESULT_OK("Ok"),
    RESULT_FAILED("Failed");
    private String info;

    private ApiResult(String info) {
        this.info = info;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }


}