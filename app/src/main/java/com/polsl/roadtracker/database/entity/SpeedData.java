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
public class SpeedData implements SensorData {
    @Index
    private Long timestamp;
    private float value;


    @Generated(hash = 2108339383)
    public SpeedData() {
    }

    @Generated(hash = 205287280)
    public SpeedData(Long timestamp, float value) {
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
        QueryBuilder builder = session.queryBuilder(SpeedData.class);
        builder.where(builder.or(SpeedDataDao.Properties.Timestamp.lt(startTime)
                , SpeedDataDao.Properties.Timestamp.gt(finishTime)));
        return builder.buildDelete();
    }
}
