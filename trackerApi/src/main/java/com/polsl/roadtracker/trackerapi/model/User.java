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
public class User {

    private Long id;
    private String email;
    private String password;
    private Long sensorDelay;

    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";
    public static final String ID = "id";
        public static final String SENSOR_DELAY = "sensorDelay";

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }
      public User(String email, String password, Long sensorDelay) {
        this.email = email;
        this.password = password;
        this.sensorDelay = sensorDelay;
    }

    public User(Long id, String email, String password, Long sensorDelay) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.sensorDelay = sensorDelay;
    }


    public User(Long id, String email, String password) {
        this.id = id;
        this.email = email;
        this.password = password;
    }

    public Long getSensorDelay() {
        return sensorDelay;
    }

    public void setSensorDelay(Long sensorDelay) {
        this.sensorDelay = sensorDelay;
    }
    

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
    

}
