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
public class SensorSettings {

    public Long accelometer;
    public Long gyroscope;
    public Long magneticField;
    public Long ambientTemperature;

    public SensorSettings() {
    }

    public SensorSettings(Long accelometer, Long gyroscope, Long magneticField, Long ambientTemperature) {
        this.accelometer = accelometer;
        this.gyroscope = gyroscope;
        this.magneticField = magneticField;
        this.ambientTemperature = ambientTemperature;
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

  

}
