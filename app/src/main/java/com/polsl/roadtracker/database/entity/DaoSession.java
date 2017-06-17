package com.polsl.roadtracker.database.entity;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.polsl.roadtracker.database.entity.AccelerometerData;
import com.polsl.roadtracker.database.entity.AmbientTemperatureData;
import com.polsl.roadtracker.database.entity.DatabaseData;
import com.polsl.roadtracker.database.entity.GyroscopeData;
import com.polsl.roadtracker.database.entity.LocationData;
import com.polsl.roadtracker.database.entity.MagneticFieldData;
import com.polsl.roadtracker.database.entity.RouteData;
import com.polsl.roadtracker.database.entity.SpeedData;
import com.polsl.roadtracker.database.entity.ThrottlePositionData;
import com.polsl.roadtracker.database.entity.RpmData;

import com.polsl.roadtracker.database.entity.AccelerometerDataDao;
import com.polsl.roadtracker.database.entity.AmbientTemperatureDataDao;
import com.polsl.roadtracker.database.entity.DatabaseDataDao;
import com.polsl.roadtracker.database.entity.GyroscopeDataDao;
import com.polsl.roadtracker.database.entity.LocationDataDao;
import com.polsl.roadtracker.database.entity.MagneticFieldDataDao;
import com.polsl.roadtracker.database.entity.RouteDataDao;
import com.polsl.roadtracker.database.entity.SpeedDataDao;
import com.polsl.roadtracker.database.entity.ThrottlePositionDataDao;
import com.polsl.roadtracker.database.entity.RpmDataDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig accelerometerDataDaoConfig;
    private final DaoConfig ambientTemperatureDataDaoConfig;
    private final DaoConfig databaseDataDaoConfig;
    private final DaoConfig gyroscopeDataDaoConfig;
    private final DaoConfig locationDataDaoConfig;
    private final DaoConfig magneticFieldDataDaoConfig;
    private final DaoConfig routeDataDaoConfig;
    private final DaoConfig speedDataDaoConfig;
    private final DaoConfig throttlePositionDataDaoConfig;
    private final DaoConfig rpmDataDaoConfig;

    private final AccelerometerDataDao accelerometerDataDao;
    private final AmbientTemperatureDataDao ambientTemperatureDataDao;
    private final DatabaseDataDao databaseDataDao;
    private final GyroscopeDataDao gyroscopeDataDao;
    private final LocationDataDao locationDataDao;
    private final MagneticFieldDataDao magneticFieldDataDao;
    private final RouteDataDao routeDataDao;
    private final SpeedDataDao speedDataDao;
    private final ThrottlePositionDataDao throttlePositionDataDao;
    private final RpmDataDao rpmDataDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        accelerometerDataDaoConfig = daoConfigMap.get(AccelerometerDataDao.class).clone();
        accelerometerDataDaoConfig.initIdentityScope(type);

        ambientTemperatureDataDaoConfig = daoConfigMap.get(AmbientTemperatureDataDao.class).clone();
        ambientTemperatureDataDaoConfig.initIdentityScope(type);

        databaseDataDaoConfig = daoConfigMap.get(DatabaseDataDao.class).clone();
        databaseDataDaoConfig.initIdentityScope(type);

        gyroscopeDataDaoConfig = daoConfigMap.get(GyroscopeDataDao.class).clone();
        gyroscopeDataDaoConfig.initIdentityScope(type);

        locationDataDaoConfig = daoConfigMap.get(LocationDataDao.class).clone();
        locationDataDaoConfig.initIdentityScope(type);

        magneticFieldDataDaoConfig = daoConfigMap.get(MagneticFieldDataDao.class).clone();
        magneticFieldDataDaoConfig.initIdentityScope(type);

        routeDataDaoConfig = daoConfigMap.get(RouteDataDao.class).clone();
        routeDataDaoConfig.initIdentityScope(type);

        speedDataDaoConfig = daoConfigMap.get(SpeedDataDao.class).clone();
        speedDataDaoConfig.initIdentityScope(type);

        throttlePositionDataDaoConfig = daoConfigMap.get(ThrottlePositionDataDao.class).clone();
        throttlePositionDataDaoConfig.initIdentityScope(type);

        rpmDataDaoConfig = daoConfigMap.get(RpmDataDao.class).clone();
        rpmDataDaoConfig.initIdentityScope(type);

        accelerometerDataDao = new AccelerometerDataDao(accelerometerDataDaoConfig, this);
        ambientTemperatureDataDao = new AmbientTemperatureDataDao(ambientTemperatureDataDaoConfig, this);
        databaseDataDao = new DatabaseDataDao(databaseDataDaoConfig, this);
        gyroscopeDataDao = new GyroscopeDataDao(gyroscopeDataDaoConfig, this);
        locationDataDao = new LocationDataDao(locationDataDaoConfig, this);
        magneticFieldDataDao = new MagneticFieldDataDao(magneticFieldDataDaoConfig, this);
        routeDataDao = new RouteDataDao(routeDataDaoConfig, this);
        speedDataDao = new SpeedDataDao(speedDataDaoConfig, this);
        throttlePositionDataDao = new ThrottlePositionDataDao(throttlePositionDataDaoConfig, this);
        rpmDataDao = new RpmDataDao(rpmDataDaoConfig, this);

        registerDao(AccelerometerData.class, accelerometerDataDao);
        registerDao(AmbientTemperatureData.class, ambientTemperatureDataDao);
        registerDao(DatabaseData.class, databaseDataDao);
        registerDao(GyroscopeData.class, gyroscopeDataDao);
        registerDao(LocationData.class, locationDataDao);
        registerDao(MagneticFieldData.class, magneticFieldDataDao);
        registerDao(RouteData.class, routeDataDao);
        registerDao(SpeedData.class, speedDataDao);
        registerDao(ThrottlePositionData.class, throttlePositionDataDao);
        registerDao(RpmData.class, rpmDataDao);
    }
    
    public void clear() {
        accelerometerDataDaoConfig.clearIdentityScope();
        ambientTemperatureDataDaoConfig.clearIdentityScope();
        databaseDataDaoConfig.clearIdentityScope();
        gyroscopeDataDaoConfig.clearIdentityScope();
        locationDataDaoConfig.clearIdentityScope();
        magneticFieldDataDaoConfig.clearIdentityScope();
        routeDataDaoConfig.clearIdentityScope();
        speedDataDaoConfig.clearIdentityScope();
        throttlePositionDataDaoConfig.clearIdentityScope();
        rpmDataDaoConfig.clearIdentityScope();
    }

    public AccelerometerDataDao getAccelerometerDataDao() {
        return accelerometerDataDao;
    }

    public AmbientTemperatureDataDao getAmbientTemperatureDataDao() {
        return ambientTemperatureDataDao;
    }

    public DatabaseDataDao getDatabaseDataDao() {
        return databaseDataDao;
    }

    public GyroscopeDataDao getGyroscopeDataDao() {
        return gyroscopeDataDao;
    }

    public LocationDataDao getLocationDataDao() {
        return locationDataDao;
    }

    public MagneticFieldDataDao getMagneticFieldDataDao() {
        return magneticFieldDataDao;
    }

    public RouteDataDao getRouteDataDao() {
        return routeDataDao;
    }

    public SpeedDataDao getSpeedDataDao() {
        return speedDataDao;
    }

    public ThrottlePositionDataDao getThrottlePositionDataDao() {
        return throttlePositionDataDao;
    }

    public RpmDataDao getRpmDataDao() {
        return rpmDataDao;
    }

}
