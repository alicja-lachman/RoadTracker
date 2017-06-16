package com.polsl.roadtracker;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.polsl.roadtracker.database.RoadtrackerDatabaseHelper;
import com.polsl.roadtracker.database.entity.DaoMaster;
import com.polsl.roadtracker.database.entity.DaoSession;

import io.fabric.sdk.android.Fabric;
import org.greenrobot.greendao.database.Database;

import timber.log.Timber;

/**
 * Created by alachman on 15.03.2017.
 */

public class RoadTrackerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        Timber.plant(new Timber.DebugTree());
        RoadtrackerDatabaseHelper.initialise(this);
    }

}
