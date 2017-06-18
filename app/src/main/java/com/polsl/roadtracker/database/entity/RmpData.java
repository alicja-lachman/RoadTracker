package com.polsl.roadtracker.database.entity;

import com.polsl.roadtracker.database.SensorData;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Index;

/**
 * Created by Jakub on 15.05.2017.
 */
@Entity
public class RmpData implements SensorData {
    @Index
    private Long timestamp;
    private float value;


    @Generated(hash = 316504271)
    public RmpData(Long timestamp, float value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    @Generated(hash = 978505827)
    public RmpData() {
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
