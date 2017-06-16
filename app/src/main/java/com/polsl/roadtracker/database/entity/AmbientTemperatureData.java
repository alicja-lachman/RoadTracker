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
    private Long routeId;

    @Generated(hash = 1613163233)
    public AmbientTemperatureData(Long timestamp, float temperature, Long routeId) {
        this.timestamp = timestamp;
        this.temperature = temperature;
        this.routeId = routeId;
    }

    @Generated(hash = 734342101)
    public AmbientTemperatureData() {
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

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }
}
