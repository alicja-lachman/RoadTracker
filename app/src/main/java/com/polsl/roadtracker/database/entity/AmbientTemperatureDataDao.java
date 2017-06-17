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
 * DAO for table "AMBIENT_TEMPERATURE_DATA".
*/
public class AmbientTemperatureDataDao extends AbstractDao<AmbientTemperatureData, Void> {

    public static final String TABLENAME = "AMBIENT_TEMPERATURE_DATA";

    /**
     * Properties of entity AmbientTemperatureData.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Timestamp = new Property(0, Long.class, "timestamp", false, "TIMESTAMP");
        public final static Property Temperature = new Property(1, float.class, "temperature", false, "TEMPERATURE");
    }


    public AmbientTemperatureDataDao(DaoConfig config) {
        super(config);
    }
    
    public AmbientTemperatureDataDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"AMBIENT_TEMPERATURE_DATA\" (" + //
                "\"TIMESTAMP\" INTEGER," + // 0: timestamp
                "\"TEMPERATURE\" REAL NOT NULL );"); // 1: temperature
        // Add Indexes
        db.execSQL("CREATE INDEX " + constraint + "IDX_AMBIENT_TEMPERATURE_DATA_TIMESTAMP ON AMBIENT_TEMPERATURE_DATA" +
                " (\"TIMESTAMP\" ASC);");
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"AMBIENT_TEMPERATURE_DATA\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, AmbientTemperatureData entity) {
        stmt.clearBindings();
 
        Long timestamp = entity.getTimestamp();
        if (timestamp != null) {
            stmt.bindLong(1, timestamp);
        }
        stmt.bindDouble(2, entity.getTemperature());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, AmbientTemperatureData entity) {
        stmt.clearBindings();
 
        Long timestamp = entity.getTimestamp();
        if (timestamp != null) {
            stmt.bindLong(1, timestamp);
        }
        stmt.bindDouble(2, entity.getTemperature());
    }

    @Override
    public Void readKey(Cursor cursor, int offset) {
        return null;
    }    

    @Override
    public AmbientTemperatureData readEntity(Cursor cursor, int offset) {
        AmbientTemperatureData entity = new AmbientTemperatureData( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // timestamp
            cursor.getFloat(offset + 1) // temperature
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, AmbientTemperatureData entity, int offset) {
        entity.setTimestamp(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setTemperature(cursor.getFloat(offset + 1));
     }
    
    @Override
    protected final Void updateKeyAfterInsert(AmbientTemperatureData entity, long rowId) {
        // Unsupported or missing PK type
        return null;
    }
    
    @Override
    public Void getKey(AmbientTemperatureData entity) {
        return null;
    }

    @Override
    public boolean hasKey(AmbientTemperatureData entity) {
        // TODO
        return false;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
