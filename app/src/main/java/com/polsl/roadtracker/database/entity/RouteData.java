package com.polsl.roadtracker.database.entity;

import com.polsl.roadtracker.database.UploadStatus;
import com.polsl.roadtracker.database.UploadStatusPropertyConverter;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.util.Date;

/**
 * Created by alachman on 29.03.2017.
 */
@Entity
public class RouteData {
    @Id
    private Long id;
    private Date startDate;
    private Date endDate;
    private String description;
    private String dbName;
    private boolean setToSend;

    @Convert(converter = UploadStatusPropertyConverter.class, columnType = Integer.class)
    private UploadStatus uploadStatus;

    @Generated(hash = 280729267)
    public RouteData() {
    }

    @Generated(hash = 1950729044)
    public RouteData(Long id, Date startDate, Date endDate, String description, String dbName,
                     boolean setToSend, UploadStatus uploadStatus) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.dbName = dbName;
        this.setToSend = setToSend;
        this.uploadStatus = uploadStatus;
    }

    public void start() {

        setStartDate(new Date(System.currentTimeMillis()));
        setUploadStatus(UploadStatus.NOT_UPLOADED);
        setToSend = false;
        setDescription("Route " + dbName.substring(7));
    }

    public void finish() {
        setEndDate(new Date(System.currentTimeMillis()));
        setUploadStatus(UploadStatus.READY_TO_UPLOAD);
    }

    public boolean isSetToSend() {
        return setToSend;
    }

    public String calculateDuration() {
        long ms = (endDate.getTime() - startDate.getTime());
        long seconds = (ms / 1000) % 60;
        long minutes = (ms / (1000 * 60)) % 60;
        long hours = (ms / (1000 * 60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }


    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UploadStatus getUploadStatus() {
        return this.uploadStatus;
    }

    public void setUploadStatus(UploadStatus uploadStatus) {
        this.uploadStatus = uploadStatus;
    }


    public boolean getSetToSend() {
        return this.setToSend;
    }

    public void setSetToSend(boolean setToSend) {
        this.setToSend = setToSend;
    }

    public void fetchAllData() {

    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDbName() {
        return this.dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }


}

