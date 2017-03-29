package com.polsl.roadtracker;

import android.app.Application;

import com.polsl.roadtracker.database.entity.DaoMaster;
import com.polsl.roadtracker.database.entity.DaoSession;

import org.greenrobot.greendao.database.Database;

import timber.log.Timber;

/**
 * Created by alachman on 15.03.2017.
 */

public class RoadTrackerApplication extends Application {
    public static final boolean ENCRYPTED = false;

    private DaoSession daoSession;
    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, ENCRYPTED ? "notes-db-encrypted" : "notes-db");
        Database db = ENCRYPTED ? helper.getEncryptedWritableDb("super-secret") : helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }
}
