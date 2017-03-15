package com.polsl.roadtracker;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by alachman on 15.03.2017.
 */

public class RoadTrackerApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree()); //necessary to use timber
    }
}
