package com.polsl.roadtracker.database.entity;

import com.polsl.roadtracker.database.SensorData;

import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.query.DeleteQuery;
import org.greenrobot.greendao.query.QueryBuilder;

/**
 * Created by alachman on 29.03.2017.
 */
@Entity
public class GyroscopeData implements SensorData {
    @Index
    private Long timestamp;
    private float x;
    private float y;
    private float z;



    @Generated(hash = 1708783831)
    public GyroscopeData() {
    }

    @Generated(hash = 159617503)
    public GyroscopeData(Long timestamp, float x, float y, float z) {
        this.timestamp = timestamp;
        this.x = x;
        this.y = y;
        this.z = z;
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


    @Override
    public DeleteQuery getQuery(DaoSession session, Long startTime, Long finishTime) {
        QueryBuilder builder = session.queryBuilder(GyroscopeData.class);
        builder.where(builder.or(GyroscopeDataDao.Properties.Timestamp.lt(startTime)
                , GyroscopeDataDao.Properties.Timestamp.gt(finishTime)));
        return builder.buildDelete();
    }
}
