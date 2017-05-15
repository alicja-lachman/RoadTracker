package com.polsl.roadtracker.database.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by Jakub on 15.05.2017.
 */
@Entity
public class ThrottlePositionData {
    private Long timestamp;
    private float value;
    private Long routeId;

    @Generated(hash = 340171573)
    public ThrottlePositionData(Long timestamp, float value, Long routeId) {
        this.timestamp = timestamp;
        this.value = value;
        this.routeId = routeId;
    }

    @Generated(hash = 905753147)
    public ThrottlePositionData() {
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
