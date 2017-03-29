package com.polsl.roadtracker.database.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by alachman on 27.03.2017.
 */

@Entity
public class AccelometerData {

    private Long timestamp;
    private float x;
    private float y;
    private float z;
    private Long routeId;


    @Generated(hash = 2092866538)
    public AccelometerData(Long timestamp, float x, float y, float z,
                           Long routeId) {
        this.timestamp = timestamp;
        this.x = x;
        this.y = y;
        this.z = z;
        this.routeId = routeId;
    }

    @Generated(hash = 1310240796)
    public AccelometerData() {
    }


    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
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
}
