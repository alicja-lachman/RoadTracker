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
    private String name;
    private String email;
    private String password;
    private Long accelometer;
    private Long gyroscope;
    private Long magneticField;
    private Long ambientTemperature;
    private String authToken;
    
    private String debugData;
    

    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";
    public static final String NAME = "name";
    public static final String ID = "id";
    public static final String ACCELOMETER = "accelometer";
    public static final String GYROSCOPE = "gyroscope";
    public static final String MAGNETIC_FIELD = "magneticField";
    public static final String AMBIENT_TEMPERATURE = "ambientTemperature";
    public static final String AUTH_TOKEN = "authToken";
    
    public static final String DEBUG_DATA = "debugData";

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
    

    public User(Long id, String name, String email, String password, Long accelometer, 
            Long gyroscope, Long magneticField, Long ambientTemperature, 
            String authToken, String debugData) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.accelometer = accelometer;
        this.gyroscope = gyroscope;
        this.magneticField = magneticField;
        this.ambientTemperature = ambientTemperature;
        this.authToken=authToken;
        this.debugData = debugData;
    }

    public User() {
    }

    public User(Long id, String email, String password) {
        this.id = id;
        this.email = email;
        this.password = password;

    }

    public String getDebugData() {
        return debugData;
    }

    public void setDebugData(String debugData) {
        this.debugData = debugData;
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

    public void setDefaultSensorSettings() {
        this.accelometer = 1l;
        this.gyroscope = 1l;
        this.magneticField = 1l;
        this.ambientTemperature = 1l;
    }



}
