package com.polsl.roadtracker.database;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.content.ContextCompat;

import com.polsl.roadtracker.database.entity.AccelerometerDataDao;
import com.polsl.roadtracker.database.entity.AmbientTemperatureDataDao;
import com.polsl.roadtracker.database.entity.DaoMaster;
import com.polsl.roadtracker.database.entity.DaoSession;
import com.polsl.roadtracker.database.entity.DatabaseData;
import com.polsl.roadtracker.database.entity.DatabaseDataDao;
import com.polsl.roadtracker.database.entity.GyroscopeDataDao;
import com.polsl.roadtracker.database.entity.LocationDataDao;
import com.polsl.roadtracker.database.entity.MagneticFieldDataDao;
import com.polsl.roadtracker.database.entity.RouteDataDao;
import com.polsl.roadtracker.database.entity.RpmDataDao;
import com.polsl.roadtracker.database.entity.SpeedDataDao;
import com.polsl.roadtracker.database.entity.ThrottlePositionDataDao;

import org.greenrobot.greendao.database.Database;

import java.io.File;
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
//        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE , Manifest.permission.READ_EXTERNAL_STORAGE};
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            ActivityCompat.requestPermissions(,permissions,1034);
//        }
        DaoMaster.DevOpenHelper helper;
        if ((ContextCompat.checkSelfPermission(context,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File path = new File(Environment.getExternalStorageDirectory().getPath(), "external-main-db");
            path.getParentFile().mkdirs();
            helper = new DaoMaster.DevOpenHelper(context, path.getAbsolutePath(), null);
        } else {
            helper = new DaoMaster.DevOpenHelper(context, "main-db");
        }
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
        AccelerometerDataDao.dropTable(db, true);

        LocationDataDao.dropTable(db, true);
        MagneticFieldDataDao.dropTable(db, true);
        AmbientTemperatureDataDao.dropTable(db, true);

        RpmDataDao.dropTable(db, true);
        SpeedDataDao.dropTable(db, true);
        ThrottlePositionDataDao.dropTable(db, true);

        daoSessionMap.remove(dbName);
    }
}

