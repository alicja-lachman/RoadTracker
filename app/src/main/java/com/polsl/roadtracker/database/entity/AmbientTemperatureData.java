package com.polsl.roadtracker.database.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Index;

/**
 * Created by Jakub on 02.04.2017.
 */

@Entity
public class AmbientTemperatureData {
    @Index
    private Long timestamp;
    private float temperature;



    @Generated(hash = 734342101)
    public AmbientTemperatureData() {
    }

    @Generated(hash = 984401045)
    public AmbientTemperatureData(Long timestamp, float temperature) {
        this.timestamp = timestamp;
        this.temperature = temperature;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

}
