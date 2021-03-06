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
public class LocationData implements SensorData{
    @Index
    private Long timestamp;
    private double latitude;
    private double longitude;



    @Generated(hash = 1606831457)
    public LocationData() {
    }

    @Generated(hash = 1489157304)
    public LocationData(Long timestamp, double latitude, double longitude) {
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public DeleteQuery getQuery(DaoSession session, Long startTime, Long finishTime) {
        QueryBuilder builder = session.queryBuilder(LocationData.class);
        builder.where(builder.or(LocationDataDao.Properties.Timestamp.lt(startTime)
                , LocationDataDao.Properties.Timestamp.gt(finishTime)));
        return builder.buildDelete();
    }
}
