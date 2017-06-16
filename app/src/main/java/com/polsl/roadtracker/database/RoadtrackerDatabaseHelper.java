package com.polsl.roadtracker.database;

import android.content.Context;

import com.polsl.roadtracker.database.entity.DaoMaster;
import com.polsl.roadtracker.database.entity.DaoSession;

import org.greenrobot.greendao.database.Database;

import java.util.HashMap;

import timber.log.Timber;

/**
 * Created by alachman on 16.06.2017.
 */

public class RoadtrackerDatabaseHelper {
    private static Database db;
    private static DaoSession daoSession;
    private static HashMap<String, DaoSession> daoSessionMap = new HashMap<>();

    public static void initialise(Context context) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, "main-db");
        db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
        Timber.d("New dao session");
    }

    public static void initialiseDbForRide(Context context, String dbName) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, dbName);
        Database db = helper.getWritableDb();
        DaoSession daoSession = new DaoMaster(db).newSession();
        daoSessionMap.put(dbName, daoSession);
    }

    public static DaoSession getDaoSessionForDb(String dbName) {
        return daoSessionMap.get(dbName);
    }

    public static DaoSession getMainDaoSession() {
        return daoSession;
    }
}

