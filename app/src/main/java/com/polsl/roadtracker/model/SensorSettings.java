package com.polsl.roadtracker.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by alachman on 23.04.2017.
 */

public class SensorSettings {
    @SerializedName("AccelerometerIntervalLength")
    private Long accelometer;
    @SerializedName("GyroscopeIntervalLength")
    private Long gyroscope;
    @SerializedName("MagneticIntervalLength")
    private Long magneticField;
    private Long ambientTemperature;

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
