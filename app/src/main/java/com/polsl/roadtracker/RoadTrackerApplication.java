package com.polsl.roadtracker;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.polsl.roadtracker.activity.LoginActivity;
import com.polsl.roadtracker.database.RoadtrackerDatabaseHelper;

import io.fabric.sdk.android.Fabric;
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
       // RoadtrackerDatabaseHelper.initialise(this);
    }

}
