package com.polsl.roadtracker.model;

import com.polsl.roadtracker.util.Constants;

/**
 * Created by alachman on 16.05.2017.
 */

public class Credentials {
    private String name;
    private String email;
    private String password;
    private String useragent;

    public Credentials() {
    }

    public Credentials(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        useragent = Constants.USER_AGENT;
    }

    public Credentials(String email, String password) {
        this.email = email;
        this.password = password;
        useragent = "Mobile Application RoadTracker";
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

    public String getUseragent() {
        return useragent;
    }

}
