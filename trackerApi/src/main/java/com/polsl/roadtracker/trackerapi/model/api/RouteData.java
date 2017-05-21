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
public class RouteData {
    private String authToken;
    private String packageNumber;
    private String content;
    private boolean lastPackage;

    public RouteData() {
    }

    public RouteData(String authToken, String packageNumber, String content, boolean lastPackage) {
        this.authToken = authToken;
        this.packageNumber = packageNumber;
        this.content = content;
        this.lastPackage = lastPackage;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getPackageNumber() {
        return packageNumber;
    }

    public void setPackageNumber(String packageNumber) {
        this.packageNumber = packageNumber;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isLastPackage() {
        return lastPackage;
    }

    public void setLastPackage(boolean lastPackage) {
        this.lastPackage = lastPackage;
    }
    
}
