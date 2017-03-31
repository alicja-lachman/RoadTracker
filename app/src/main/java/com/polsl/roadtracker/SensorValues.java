package com.polsl.roadtracker;

import java.lang.String;

/**
 * Created by Jakub on 27.03.2017.
 */

public class SensorValues {
    private float[] values;//depending on sensor different amount of elements
    private String sensorType;
    private long timestamp;

    public SensorValues(float[] values, String sensorType, long timestamp){
        this.values = values;
        this.sensorType = sensorType;
        this.timestamp = timestamp;
    }

    public float[] getValues() {
        return values;
    }

    public String getSensorType() {
        return sensorType;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
