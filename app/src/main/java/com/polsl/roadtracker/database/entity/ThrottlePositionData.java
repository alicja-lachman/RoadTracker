package com.polsl.roadtracker.database.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Index;

/**
 * Created by Jakub on 15.05.2017.
 */
@Entity
public class ThrottlePositionData {
    @Index
    private Long timestamp;
    private float value;



    @Generated(hash = 905753147)
    public ThrottlePositionData() {
    }

    @Generated(hash = 211155014)
    public ThrottlePositionData(Long timestamp, float value) {
        this.timestamp = timestamp;
        this.value = value;
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

}
