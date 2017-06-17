package com.polsl.roadtracker.database;

import android.content.Context;

import com.polsl.roadtracker.database.entity.AccelometerDataDao;
import com.polsl.roadtracker.database.entity.AmbientTemperatureDataDao;
import com.polsl.roadtracker.database.entity.DaoMaster;
import com.polsl.roadtracker.database.entity.DaoSession;
import com.polsl.roadtracker.database.entity.DatabaseData;
import com.polsl.roadtracker.database.entity.DatabaseDataDao;
import com.polsl.roadtracker.database.entity.GyroscopeDataDao;
import com.polsl.roadtracker.database.entity.LocationDataDao;
import com.polsl.roadtracker.database.entity.MagneticFieldDataDao;
import com.polsl.roadtracker.database.entity.RmpDataDao;
import com.polsl.roadtracker.database.entity.RouteDataDao;
import com.polsl.roadtracker.database.entity.SpeedDataDao;
import com.polsl.roadtracker.database.entity.ThrottlePositionDataDao;

import org.greenrobot.greendao.database.Database;

import java.util.HashMap;
import java.util.List;

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

    public static void deleteDatabase(Context ctx, String dbName) {
        DatabaseDataDao dao = daoSession.getDatabaseDataDao();
        List<DatabaseData> databases = dao.loadAll();
        for (DatabaseData data : databases)
            if (data.getDatabaseName().equals(dbName))
                dao.delete(data);
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(ctx, dbName);
        Database db = helper.getWritableDb();

        RouteDataDao.dropTable(db, true);
        GyroscopeDataDao.dropTable(db, true);
        AccelometerDataDao.dropTable(db, true);

        LocationDataDao.dropTable(db, true);
        MagneticFieldDataDao.dropTable(db, true);
        AmbientTemperatureDataDao.dropTable(db, true);

        RmpDataDao.dropTable(db, true);
        SpeedDataDao.dropTable(db, true);
        ThrottlePositionDataDao.dropTable(db, true);

        daoSessionMap.remove(dbName);
    }
}

