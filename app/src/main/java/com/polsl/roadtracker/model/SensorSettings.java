package com.polsl.roadtracker.model;

/**
 * Created by alachman on 23.04.2017.
 */

public class SensorSettings {

    private Integer accelometer;
    private Integer gyroscope;
    private Integer magneticField;
    private Integer ambientTemperature;

    public SensorSettings() {

    }

    public SensorSettings(Integer accelometer, Integer gyroscope, Integer magneticField, Integer ambientTemperature) {
        this.accelometer = accelometer;
        this.gyroscope = gyroscope;
        this.magneticField = magneticField;
        this.ambientTemperature = ambientTemperature;
    }

    public Integer getAccelometer() {
        return accelometer;
    }

    public void setAccelometer(Integer accelometer) {
        this.accelometer = accelometer;
    }

    public Integer getGyroscope() {
        return gyroscope;
    }

    public void setGyroscope(Integer gyroscope) {
        this.gyroscope = gyroscope;
    }

    public Integer getMagneticField() {
        return magneticField;
    }

    public void setMagneticField(Integer magneticField) {
        this.magneticField = magneticField;
    }

    public Integer getAmbientTemperature() {
        return ambientTemperature;
    }

    public void setAmbientTemperature(Integer ambientTemperature) {
        this.ambientTemperature = ambientTemperature;
    }
}
