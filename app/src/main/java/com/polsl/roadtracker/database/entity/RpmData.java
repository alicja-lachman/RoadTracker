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
public class RpmData implements SensorData{
    @Index
    private Long timestamp;
    private float value;

    @Generated(hash = 434139780)
    public RpmData(Long timestamp, float value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    @Generated(hash = 1398823423)
    public RpmData() {
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
        QueryBuilder builder = session.queryBuilder(RpmData.class);
        builder.where(builder.or(RpmDataDao.Properties.Timestamp.lt(startTime)
                , RpmDataDao.Properties.Timestamp.gt(finishTime)));
        return builder.buildDelete();
    }
}
