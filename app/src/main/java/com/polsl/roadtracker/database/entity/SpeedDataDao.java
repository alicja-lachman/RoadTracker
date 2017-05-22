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
 * DAO for table "SPEED_DATA".
*/
public class SpeedDataDao extends AbstractDao<SpeedData, Void> {

    public static final String TABLENAME = "SPEED_DATA";

    /**
     * Properties of entity SpeedData.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Timestamp = new Property(0, Long.class, "timestamp", false, "TIMESTAMP");
        public final static Property Value = new Property(1, float.class, "value", false, "VALUE");
        public final static Property RouteId = new Property(2, Long.class, "routeId", false, "ROUTE_ID");
    }


    public SpeedDataDao(DaoConfig config) {
        super(config);
    }
    
    public SpeedDataDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"SPEED_DATA\" (" + //
                "\"TIMESTAMP\" INTEGER," + // 0: timestamp
                "\"VALUE\" REAL NOT NULL ," + // 1: value
                "\"ROUTE_ID\" INTEGER);"); // 2: routeId
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"SPEED_DATA\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, SpeedData entity) {
        stmt.clearBindings();
 
        Long timestamp = entity.getTimestamp();
        if (timestamp != null) {
            stmt.bindLong(1, timestamp);
        }
        stmt.bindDouble(2, entity.getValue());
 
        Long routeId = entity.getRouteId();
        if (routeId != null) {
            stmt.bindLong(3, routeId);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, SpeedData entity) {
        stmt.clearBindings();
 
        Long timestamp = entity.getTimestamp();
        if (timestamp != null) {
            stmt.bindLong(1, timestamp);
        }
        stmt.bindDouble(2, entity.getValue());
 
        Long routeId = entity.getRouteId();
        if (routeId != null) {
            stmt.bindLong(3, routeId);
        }
    }

    @Override
    public Void readKey(Cursor cursor, int offset) {
        return null;
    }    

    @Override
    public SpeedData readEntity(Cursor cursor, int offset) {
        SpeedData entity = new SpeedData( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // timestamp
            cursor.getFloat(offset + 1), // value
            cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2) // routeId
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, SpeedData entity, int offset) {
        entity.setTimestamp(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setValue(cursor.getFloat(offset + 1));
        entity.setRouteId(cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2));
     }
    
    @Override
    protected final Void updateKeyAfterInsert(SpeedData entity, long rowId) {
        // Unsupported or missing PK type
        return null;
    }
    
    @Override
    public Void getKey(SpeedData entity) {
        return null;
    }

    @Override
    public boolean hasKey(SpeedData entity) {
        // TODO
        return false;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}