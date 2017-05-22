package com.polsl.roadtracker.dagger.di.module;

import com.polsl.roadtracker.RoadTrackerApplication;
import com.polsl.roadtracker.database.entity.AccelometerDataDao;
import com.polsl.roadtracker.database.entity.AmbientTemperatureDataDao;
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

    @Provides
    public RouteDataDao provideRouteDataDao() {
        return RoadTrackerApplication.getDaoSession().getRouteDataDao();
    }

    @Provides
    public AccelometerDataDao provideAccelometerDataDao() {
        return RoadTrackerApplication.getDaoSession().getAccelometerDataDao();
    }

    @Provides
    public GyroscopeDataDao provideGyroscopeDataDao() {
        return RoadTrackerApplication.getDaoSession().getGyroscopeDataDao();
    }

    @Provides
    public MagneticFieldDataDao provideMagneticFieldDataDao(){
        return  RoadTrackerApplication.getDaoSession().getMagneticFieldDataDao();
    }

    @Provides
    public AmbientTemperatureDataDao provideAmbientTemperatureDataDao(){
        return RoadTrackerApplication.getDaoSession().getAmbientTemperatureDataDao();
    }

    @Provides
    public LocationDataDao provideLocationDataDao() {
        return RoadTrackerApplication.getDaoSession().getLocationDataDao();
    }

    @Provides
    public SpeedDataDao provideSpeedDataDao(){
        return RoadTrackerApplication.getDaoSession().getSpeedDataDao();
    }

    @Provides
    public RMPDataDao provideRMPDataDao(){
        return RoadTrackerApplication.getDaoSession().getRMPDataDao();
    }

    @Provides
    public ThrottlePositionDataDao provideThrottlePositionDataDao(){
        return RoadTrackerApplication.getDaoSession().getThrottlePositionDataDao();
    }
}
