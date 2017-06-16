package com.polsl.roadtracker.dagger.di.module;

import com.polsl.roadtracker.RoadTrackerApplication;
import com.polsl.roadtracker.database.RoadtrackerDatabaseHelper;
import com.polsl.roadtracker.database.entity.AccelometerDataDao;
import com.polsl.roadtracker.database.entity.AmbientTemperatureDataDao;
import com.polsl.roadtracker.database.entity.DatabaseDataDao;
import com.polsl.roadtracker.database.entity.GyroscopeDataDao;
import com.polsl.roadtracker.database.entity.LocationDataDao;
import com.polsl.roadtracker.database.entity.MagneticFieldDataDao;
import com.polsl.roadtracker.database.entity.RMPDataDao;
import com.polsl.roadtracker.database.entity.RouteDataDao;
import com.polsl.roadtracker.database.entity.SpeedDataDao;
import com.polsl.roadtracker.database.entity.ThrottlePositionDataDao;

import dagger.Module;
import dagger.Provides;

/**
 * Created by alachman on 29.03.2017.
 */
@Module
public class DatabaseModule {

//    @Provides
//    public RouteDataDao provideRouteDataDao() {
//        return RoadtrackerDatabaseHelper.getDaoSession().getRouteDataDao();
//    }
//
//    @Provides
//    public AccelometerDataDao provideAccelometerDataDao() {
//        return RoadtrackerDatabaseHelper.getDaoSession().getAccelometerDataDao();
//    }
//
//    @Provides
//    public GyroscopeDataDao provideGyroscopeDataDao() {
//        return RoadtrackerDatabaseHelper.getDaoSession().getGyroscopeDataDao();
//    }
//
//    @Provides
//    public MagneticFieldDataDao provideMagneticFieldDataDao(){
//        return  RoadtrackerDatabaseHelper.getDaoSession().getMagneticFieldDataDao();
//    }
//
//    @Provides
//    public AmbientTemperatureDataDao provideAmbientTemperatureDataDao(){
//        return RoadtrackerDatabaseHelper.getDaoSession().getAmbientTemperatureDataDao();
//    }
//
//    @Provides
//    public LocationDataDao provideLocationDataDao() {
//        return RoadtrackerDatabaseHelper.getDaoSession().getLocationDataDao();
//    }
//
//    @Provides
//    public SpeedDataDao provideSpeedDataDao(){
//        return RoadtrackerDatabaseHelper.getDaoSession().getSpeedDataDao();
//    }
//
//    @Provides
//    public RMPDataDao provideRMPDataDao(){
//        return RoadtrackerDatabaseHelper.getDaoSession().getRMPDataDao();
//    }
//
//    @Provides
//    public ThrottlePositionDataDao provideThrottlePositionDataDao(){
//        return RoadtrackerDatabaseHelper.getDaoSession().getThrottlePositionDataDao();
//    }
    @Provides
    public DatabaseDataDao provideDatabaseDataDao(){
        return RoadtrackerDatabaseHelper.getMainDaoSession().getDatabaseDataDao();
    }

}
