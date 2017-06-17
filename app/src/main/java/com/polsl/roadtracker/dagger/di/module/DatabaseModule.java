package com.polsl.roadtracker.dagger.di.module;

import com.polsl.roadtracker.database.RoadtrackerDatabaseHelper;
import com.polsl.roadtracker.database.entity.DatabaseDataDao;

import dagger.Module;
import dagger.Provides;

/**
 * Created by alachman on 29.03.2017.
 */
@Module
public class DatabaseModule {


    @Provides
    public DatabaseDataDao provideDatabaseDataDao() {
        return RoadtrackerDatabaseHelper.getMainDaoSession().getDatabaseDataDao();
    }

}
