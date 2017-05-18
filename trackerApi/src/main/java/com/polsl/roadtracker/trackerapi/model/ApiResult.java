/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.polsl.roadtracker.trackerapi.model;

/**
 *
 * @author alachman
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
