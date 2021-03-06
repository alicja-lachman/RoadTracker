package com.polsl.roadtracker.database.entity;

import com.polsl.roadtracker.database.SensorData;

import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.query.DeleteQuery;

/**
 * Created by Jakub on 02.04.2017.
 */

@Entity
public class AmbientTemperatureData implements SensorData {
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

    @Override
    public DeleteQuery getQuery(DaoSession session, Long startTime, Long finishTime) {
        return session.queryBuilder(AmbientTemperatureData.class)
                .where(AmbientTemperatureDataDao.Properties.Timestamp.lt(startTime)
                        , AmbientTemperatureDataDao.Properties.Timestamp.gt(finishTime))
                .buildDelete();
    }
}
