package com.polsl.roadtracker.database.entity;

import com.polsl.roadtracker.database.SensorData;

import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.query.DeleteQuery;
import org.greenrobot.greendao.query.QueryBuilder;

/**
 * Created by Jakub on 15.05.2017.
 */
@Entity
public class ThrottlePositionData implements SensorData {
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

    @Override
    public DeleteQuery getQuery(DaoSession session, Long startTime, Long finishTime) {
        QueryBuilder builder = session.queryBuilder(ThrottlePositionData.class);
        builder.where(builder.or(ThrottlePositionDataDao.Properties.Timestamp.lt(startTime)
                , ThrottlePositionDataDao.Properties.Timestamp.gt(finishTime)));
        return builder.buildDelete();
    }
}
