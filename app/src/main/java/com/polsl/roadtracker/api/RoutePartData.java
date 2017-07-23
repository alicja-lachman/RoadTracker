package com.polsl.roadtracker.api;

/**
 * Created by alachman on 18.05.2017.
 */

/**
 * Model class representing part of database file.
 */
public class RoutePartData {
    private String authToken;
    private String packageNumber;
    private String content;
    private boolean lastPackage;

    public RoutePartData() {
    }

    public RoutePartData(String authToken, String packageNumber, String content, boolean lastPackage) {
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