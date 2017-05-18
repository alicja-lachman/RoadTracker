package com.polsl.roadtracker.model;

/**
 * Created by alachman on 16.05.2017.
 */

public class Credentials {
    private String email;
    private String password;
    private  String userAgent;

    public Credentials() {
    }

    public Credentials(String email, String password) {
        this.email = email;
        this.password = password;
        userAgent = "Mobile Application RoadTracker";

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserAgent() {
        return userAgent;
    }

}
