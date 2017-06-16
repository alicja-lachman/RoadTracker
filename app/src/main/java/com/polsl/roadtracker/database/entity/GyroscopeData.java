package com.polsl.roadtracker.database.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Index;

/**
 * Created by alachman on 29.03.2017.
 */
@Entity
public class GyroscopeData {
    @Index
    private Long timestamp;
    private float x;
    private float y;
    private float z;
    private Long routeId;

    @Generated(hash = 189670957)
    public GyroscopeData(Long timestamp, float x, float y, float z, Long routeId) {
        this.timestamp = timestamp;
        this.x = x;
        this.y = y;
        this.z = z;
        this.routeId = routeId;
    }

    @Generated(hash = 1708783831)
    public GyroscopeData() {
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }
}
