package com.polsl.roadtracker.database.entity;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "ACCELEROMETER_DATA".
*/
public class AccelerometerDataDao extends AbstractDao<AccelerometerData, Void> {

    public static final String TABLENAME = "ACCELEROMETER_DATA";

    /**
     * Properties of entity AccelerometerData.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Timestamp = new Property(0, Long.class, "timestamp", false, "TIMESTAMP");
        public final static Property X = new Property(1, float.class, "x", false, "X");
        public final static Property Y = new Property(2, float.class, "y", false, "Y");
        public final static Property Z = new Property(3, float.class, "z", false, "Z");
    }


    public AccelerometerDataDao(DaoConfig config) {
        super(config);
    }
    
    public AccelerometerDataDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"ACCELEROMETER_DATA\" (" + //
                "\"TIMESTAMP\" INTEGER," + // 0: timestamp
                "\"X\" REAL NOT NULL ," + // 1: x
                "\"Y\" REAL NOT NULL ," + // 2: y
                "\"Z\" REAL NOT NULL );"); // 3: z
        // Add Indexes
        db.execSQL("CREATE INDEX " + constraint + "IDX_ACCELEROMETER_DATA_TIMESTAMP ON ACCELEROMETER_DATA" +
                " (\"TIMESTAMP\" ASC);");
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"ACCELEROMETER_DATA\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, AccelerometerData entity) {
        stmt.clearBindings();
 
        Long timestamp = entity.getTimestamp();
        if (timestamp != null) {
            stmt.bindLong(1, timestamp);
        }
        stmt.bindDouble(2, entity.getX());
        stmt.bindDouble(3, entity.getY());
        stmt.bindDouble(4, entity.getZ());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, AccelerometerData entity) {
        stmt.clearBindings();
 
        Long timestamp = entity.getTimestamp();
        if (timestamp != null) {
            stmt.bindLong(1, timestamp);
        }
        stmt.bindDouble(2, entity.getX());
        stmt.bindDouble(3, entity.getY());
        stmt.bindDouble(4, entity.getZ());
    }

    @Override
    public Void readKey(Cursor cursor, int offset) {
        return null;
    }    

    @Override
    public AccelerometerData readEntity(Cursor cursor, int offset) {
        AccelerometerData entity = new AccelerometerData( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // timestamp
            cursor.getFloat(offset + 1), // x
            cursor.getFloat(offset + 2), // y
            cursor.getFloat(offset + 3) // z
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, AccelerometerData entity, int offset) {
        entity.setTimestamp(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setX(cursor.getFloat(offset + 1));
        entity.setY(cursor.getFloat(offset + 2));
        entity.setZ(cursor.getFloat(offset + 3));
     }
    
    @Override
    protected final Void updateKeyAfterInsert(AccelerometerData entity, long rowId) {
        // Unsupported or missing PK type
        return null;
    }
    
    @Override
    public Void getKey(AccelerometerData entity) {
        return null;
    }

    @Override
    public boolean hasKey(AccelerometerData entity) {
        // TODO
        return false;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}