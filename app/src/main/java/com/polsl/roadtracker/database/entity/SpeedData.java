package com.polsl.roadtracker.database.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Index;

/**
 * Created by Jakub on 15.05.2017.
 */
@Entity
public class SpeedData {
    @Index
    private Long timestamp;
    private float value;
    private Long routeId;

    @Generated(hash = 144700382)
    public SpeedData(Long timestamp, float value, Long routeId) {
        this.timestamp = timestamp;
        this.value = value;
        this.routeId = routeId;
    }

    @Generated(hash = 2108339383)
    public SpeedData() {
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }
}
