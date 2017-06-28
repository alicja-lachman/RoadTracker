package com.polsl.roadtracker.database;

import com.polsl.roadtracker.database.entity.DaoSession;
import org.greenrobot.greendao.query.DeleteQuery;

/**
 * Created by Wojtek on 17.06.2017.
 * Interface enabling deleting data which is not between given time bounds
 */

public interface SensorData {
    /**
     * Returns delete query of data implementing the interface
     * @param session session in which data will be deleted
     * @param startTime start of the relevant data
     * @param finishTime end of the relevant data
     * @return delete query which executing results in holding only relevant data in the database
     */
    DeleteQuery getQuery(DaoSession session, Long startTime, Long finishTime);

    /**
     * Wraps getTimestamp of data in database
     * @return time of the data
     */
    Long getTimestamp();
}
