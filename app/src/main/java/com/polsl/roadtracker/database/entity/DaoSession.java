package com.polsl.roadtracker.database.entity;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.polsl.roadtracker.database.entity.AccelometerData;
import com.polsl.roadtracker.database.entity.GyroscopeData;
import com.polsl.roadtracker.database.entity.LocationData;
import com.polsl.roadtracker.database.entity.RouteData;

import com.polsl.roadtracker.database.entity.AccelometerDataDao;
import com.polsl.roadtracker.database.entity.GyroscopeDataDao;
import com.polsl.roadtracker.database.entity.LocationDataDao;
import com.polsl.roadtracker.database.entity.RouteDataDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig accelometerDataDaoConfig;
    private final DaoConfig gyroscopeDataDaoConfig;
    private final DaoConfig locationDataDaoConfig;
    private final DaoConfig routeDataDaoConfig;

    private final AccelometerDataDao accelometerDataDao;
    private final GyroscopeDataDao gyroscopeDataDao;
    private final LocationDataDao locationDataDao;
    private final RouteDataDao routeDataDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        accelometerDataDaoConfig = daoConfigMap.get(AccelometerDataDao.class).clone();
        accelometerDataDaoConfig.initIdentityScope(type);

        gyroscopeDataDaoConfig = daoConfigMap.get(GyroscopeDataDao.class).clone();
        gyroscopeDataDaoConfig.initIdentityScope(type);

        locationDataDaoConfig = daoConfigMap.get(LocationDataDao.class).clone();
        locationDataDaoConfig.initIdentityScope(type);

        routeDataDaoConfig = daoConfigMap.get(RouteDataDao.class).clone();
        routeDataDaoConfig.initIdentityScope(type);

        accelometerDataDao = new AccelometerDataDao(accelometerDataDaoConfig, this);
        gyroscopeDataDao = new GyroscopeDataDao(gyroscopeDataDaoConfig, this);
        locationDataDao = new LocationDataDao(locationDataDaoConfig, this);
        routeDataDao = new RouteDataDao(routeDataDaoConfig, this);

        registerDao(AccelometerData.class, accelometerDataDao);
        registerDao(GyroscopeData.class, gyroscopeDataDao);
        registerDao(LocationData.class, locationDataDao);
        registerDao(RouteData.class, routeDataDao);
    }
    
    public void clear() {
        accelometerDataDaoConfig.clearIdentityScope();
        gyroscopeDataDaoConfig.clearIdentityScope();
        locationDataDaoConfig.clearIdentityScope();
        routeDataDaoConfig.clearIdentityScope();
    }

    public AccelometerDataDao getAccelometerDataDao() {
        return accelometerDataDao;
    }

    public GyroscopeDataDao getGyroscopeDataDao() {
        return gyroscopeDataDao;
    }

    public LocationDataDao getLocationDataDao() {
        return locationDataDao;
    }

    public RouteDataDao getRouteDataDao() {
        return routeDataDao;
    }

}
