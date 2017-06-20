package com.polsl.roadtracker.database;

import com.polsl.roadtracker.database.entity.DaoSession;
import org.greenrobot.greendao.query.DeleteQuery;

/**
 * Created by Wojtek on 17.06.2017.
 */

public interface SensorData {
    DeleteQuery getQuery(DaoSession session, Long startTime, Long finishTime);
    Long getTimestamp();
}
