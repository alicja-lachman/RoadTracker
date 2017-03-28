package com.polsl.roadtracker;

import java.lang.String;

/**
 * Created by Jakub on 27.03.2017.
 */

public class SensorValues {
    private float[] values;
    private String sensorType;
    private long timestamp;

    public SensorValues(float[] values, String sensorType, long timestamp){
        this.values = values;
        this.sensorType = sensorType;
        this.timestamp = timestamp;
    }
    //private float[] accelerometer;//3 values
    //private float[] gyroscope;//3 values
}
