package com.polsl.roadtracker.database.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by alachman on 16.06.2017.
 */
@Entity
public class DatabaseData {
    @Id(autoincrement = true)
    private Long dbId;
    private String databaseName;
    @Generated(hash = 1353610297)
    public DatabaseData(Long dbId, String databaseName) {
        this.dbId = dbId;
        this.databaseName = databaseName;
    }
    @Generated(hash = 279320982)
    public DatabaseData() {
    }
    public Long getDbId() {
        return this.dbId;
    }
    public void setDbId(Long dbId) {
        this.dbId = dbId;
    }
    public String getDatabaseName() {
        return this.databaseName;
    }
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

}
