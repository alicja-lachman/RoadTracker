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
    public Long accelometer;
    public Long gyroscope;
    public Long magneticField;
    public Long ambientTemperature;
    

    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";
    public static final String ID = "id";
    public static final String ACCELOMETER = "accelometer";
    public static final String GYROSCOPE = "gyroscope";
    public static final String MAGNETIC_FIELD = "magneticField";
    public static final String AMBIENT_TEMPERATURE = "ambientTemperature";

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public Long getAccelometer() {
        return accelometer;
    }

    public void setAccelometer(Long accelometer) {
        this.accelometer = accelometer;
    }

    public Long getGyroscope() {
        return gyroscope;
    }

    public void setGyroscope(Long gyroscope) {
        this.gyroscope = gyroscope;
    }

    public Long getMagneticField() {
        return magneticField;
    }

    public void setMagneticField(Long magneticField) {
        this.magneticField = magneticField;
    }

    public Long getAmbientTemperature() {
        return ambientTemperature;
    }

    public void setAmbientTemperature(Long ambientTemperature) {
        this.ambientTemperature = ambientTemperature;
    }

    public User(Long id, String email, String password, Long accelometer, Long gyroscope, Long magneticField, Long ambientTemperature) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.accelometer = accelometer;
        this.gyroscope = gyroscope;
        this.magneticField = magneticField;
        this.ambientTemperature = ambientTemperature;
    }

    public User() {
    }

    
    public User(Long id, String email, String password) {
        this.id = id;
        this.email = email;
        this.password = password;

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
