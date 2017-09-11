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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

/**
 * Created by alachman on 16.06.2017.
 */

public class RoadtrackerDatabaseHelper {
    private static Database db;
    private static DaoSession daoSession;
    private static HashMap<String, DaoSession> daoSessionMap = new HashMap<>();

    public static void initialise(Context context) {
        DaoMaster.DevOpenHelper helper = null;
        List<String> storages = getExternalMounts();
        try {
            if ((ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                    && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && storages.size() > 0) {
                File path;
                String state = Environment.getExternalStorageState();
                File fileList[] = new File("/storage/").listFiles();
                File folder;

                for (File file : fileList) {
                    if (!file.getAbsolutePath().equalsIgnoreCase(Environment.getExternalStorageDirectory().getAbsolutePath()) && file.isDirectory() && file.canRead() && file.canWrite()) {

                        path = new File(file.getPath(), "external-main-db.db");
                        helper = new DaoMaster.DevOpenHelper(context, path.getAbsolutePath(), null);
                        break;
                    }
                }
//                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
//                    path = new File(Environment.getExternalStorageDirectory().getPath(), "external-main-db.db");
//                } else {
//                    path = new File(storages.get(0), "external-main-db.db");
//                }
//
//                path.getParentFile().mkdirs();
                Timber.d("SD card");

            } else {
                helper = new DaoMaster.DevOpenHelper(context, "main-db.db");
                Timber.d("Device");
            }
        } catch (Exception e) {
            helper = new DaoMaster.DevOpenHelper(context, "main-db.db");
            Timber.d("Device");
        } finally {
            if (helper == null)
                helper = new DaoMaster.DevOpenHelper(context, "main-db.db");
            db = helper.getWritableDb();
            daoSession = new DaoMaster(db).newSession();
            Timber.d("New dao session");
        }

    }

    public static List<String> getExternalMounts() {
        final List<String> out = new ArrayList<String>();
        String reg = "(?i).*vold.*(vfat|ntfs|exfat|fat32|ext3|ext4).*rw.*";
        String s = "";
        try {
            final Process process = new ProcessBuilder().command("mount")
                    .redirectErrorStream(true).start();
            process.waitFor();
            final InputStream is = process.getInputStream();
            final byte[] buffer = new byte[1024];
            while (is.read(buffer) != -1) {
                s = s + new String(buffer);
            }
            is.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        // parse output
        final String[] lines = s.split("\n");
        for (String line : lines) {
            if (!line.toLowerCase(Locale.US).contains("asec")) {
                if (line.matches(reg)) {
                    String[] parts = line.split(" ");
                    for (String part : parts) {
                        if (part.startsWith("/"))
                            if (!part.toLowerCase(Locale.US).contains("vold"))
                                out.add(part);
                    }
                }
            }
        }
        return out;
    }

    public static void initialiseDbForRide(Context context, String dbName) {
        DaoMaster.DevOpenHelper helper = null;
        List<String> storages = getExternalMounts();
        File path;
        try {
            if ((ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                    && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && storages.size() > 0) {

                String state = Environment.getExternalStorageState();
                File fileList[] = new File("/storage/").listFiles();
                File folder;

                for (File file : fileList) {
                    if (!file.getAbsolutePath().equalsIgnoreCase(Environment.getExternalStorageDirectory().getAbsolutePath()) && file.isDirectory() && file.canRead() && file.canWrite()) {

                        path = new File(file.getPath(), dbName + ".db");
                        helper = new DaoMaster.DevOpenHelper(context, path.getAbsolutePath(), null);
                        break;
                    }
                }
//                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
//                    path = new File(Environment.getExternalStorageDirectory().getPath(), dbName);
//                    path.getParentFile().mkdirs();
//                    helper = new DaoMaster.DevOpenHelper(context, path.getAbsolutePath(), null);
//                } else {
//                    path = new File(storages.get(0), dbName + ".db");
//                }
//
//                path.getParentFile().mkdirs();
//                helper = new DaoMaster.DevOpenHelper(context, path.getAbsolutePath(), null);
            } else {
                helper = new DaoMaster.DevOpenHelper(context, dbName + ".db");
            }
        } catch (Exception e) {
            helper = new DaoMaster.DevOpenHelper(context, dbName + ".db");
        } finally {
            if (helper == null)
                helper = new DaoMaster.DevOpenHelper(context, dbName + ".db");
            Database db = helper.getWritableDb();
            DaoSession daoSession = new DaoMaster(db).newSession();
            daoSessionMap.put(dbName, daoSession);
        }
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

