package com.polsl.roadtracker.dagger.di.module;

import com.polsl.roadtracker.RoadTrackerApplication;
import com.polsl.roadtracker.database.entity.AccelometerDataDao;
import com.polsl.roadtracker.database.entity.GyroscopeDataDao;
import com.polsl.roadtracker.database.entity.LocationDataDao;
import com.polsl.roadtracker.database.entity.RouteDataDao;

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
    public LocationDataDao provideLocationDataDao() {
        return RoadTrackerApplication.getDaoSession().getLocationDataDao();
    }
}
