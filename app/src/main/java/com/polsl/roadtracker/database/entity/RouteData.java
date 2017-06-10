package com.polsl.roadtracker.database.entity;

import com.polsl.roadtracker.database.UploadStatus;
import com.polsl.roadtracker.database.UploadStatusPropertyConverter;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.JoinProperty;
import org.greenrobot.greendao.annotation.OrderBy;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by alachman on 29.03.2017.
 */
@Entity
public class RouteData {
    @Id(autoincrement = true)
    private Long id;
    private Date startDate;
    private Date endDate;
    private String description;
    private boolean setToSend;

    @ToMany(joinProperties = {
            @JoinProperty(name = "id", referencedName = "routeId")
    })
    @OrderBy("timestamp ASC")
    private List<AccelometerData> accelometerDataList;

    @ToMany(joinProperties = {
            @JoinProperty(name = "id", referencedName = "routeId")
    })
    @OrderBy("timestamp ASC")
    private List<LocationData> locationDataList;

    @ToMany(joinProperties = {
            @JoinProperty(name = "id", referencedName = "routeId")
    })
    @OrderBy("timestamp ASC")
    private List<GyroscopeData> gyroscopeDataList;

    @ToMany(joinProperties = {
            @JoinProperty(name = "id",referencedName = "routeId")
    })
    @OrderBy("timestamp ASC")
    private List<SpeedData> speedDataList;

    @ToMany(joinProperties = {
            @JoinProperty(name = "id",referencedName = "routeId")
    })
    @OrderBy("timestamp ASC")
    private List<RMPData> rmpDataList;

    @ToMany(joinProperties = {
            @JoinProperty(name = "id",referencedName = "routeId")
    })
    @OrderBy("timestamp ASC")
    private List<ThrottlePositionData> throttlePositionDataList;

    @Convert(converter = UploadStatusPropertyConverter.class, columnType = Integer.class)
    private UploadStatus uploadStatus;
    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /**
     * Used for active entity operations.
     */
    @Generated(hash = 368698595)
    private transient RouteDataDao myDao;


    @Generated(hash = 1865995691)
    public RouteData(Long id, Date startDate, Date endDate, String description,
            boolean setToSend, UploadStatus uploadStatus) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.setToSend = setToSend;
        this.uploadStatus = uploadStatus;
    }

    @Generated(hash = 280729267)
    public RouteData() {
    }

    public void start() {
//        Random generator = new Random();
//        setId((long)generator.nextInt(100));
        setStartDate(new Date(System.currentTimeMillis()));
        setUploadStatus(UploadStatus.NOT_UPLOADED);
        setDescription("Route " + this.getId());
        setToSend = false;
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
        return  String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 226986899)
    public List<AccelometerData> getAccelometerDataList() {
        if (accelometerDataList == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            AccelometerDataDao targetDao = daoSession.getAccelometerDataDao();
            List<AccelometerData> accelometerDataListNew = targetDao
                    ._queryRouteData_AccelometerDataList(id);
            synchronized (this) {
                if (accelometerDataList == null) {
                    accelometerDataList = accelometerDataListNew;
                }
            }
        }
        return accelometerDataList;
    }

    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */
    @Generated(hash = 186320036)
    public synchronized void resetAccelometerDataList() {
        accelometerDataList = null;
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 884091940)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getRouteDataDao() : null;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1179144003)
    public List<LocationData> getLocationDataList() {
        if (locationDataList == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            LocationDataDao targetDao = daoSession.getLocationDataDao();
            List<LocationData> locationDataListNew = targetDao
                    ._queryRouteData_LocationDataList(id);
            synchronized (this) {
                if (locationDataList == null) {
                    locationDataList = locationDataListNew;
                }
            }
        }
        return locationDataList;
    }

    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */
    @Generated(hash = 1180624715)
    public synchronized void resetLocationDataList() {
        locationDataList = null;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1225420398)
    public List<GyroscopeData> getGyroscopeDataList() {
        if (gyroscopeDataList == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            GyroscopeDataDao targetDao = daoSession.getGyroscopeDataDao();
            List<GyroscopeData> gyroscopeDataListNew = targetDao
                    ._queryRouteData_GyroscopeDataList(id);
            synchronized (this) {
                if (gyroscopeDataList == null) {
                    gyroscopeDataList = gyroscopeDataListNew;
                }
            }
        }
        return gyroscopeDataList;
    }

    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */
    @Generated(hash = 1166767524)
    public synchronized void resetGyroscopeDataList() {
        gyroscopeDataList = null;
    }

    public boolean getSetToSend() {
        return this.setToSend;
    }

    public void setSetToSend(boolean setToSend) {
        this.setToSend = setToSend;
    }

    public void fetchAllData() {
        getAccelometerDataList();
        getGyroscopeDataList();
        getLocationDataList();
        getSpeedDataList();
        getRmpDataList();
        getThrottlePositionDataList();
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1810813848)
    public List<SpeedData> getSpeedDataList() {
        if (speedDataList == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            SpeedDataDao targetDao = daoSession.getSpeedDataDao();
            List<SpeedData> speedDataListNew = targetDao._queryRouteData_SpeedDataList(id);
            synchronized (this) {
                if (speedDataList == null) {
                    speedDataList = speedDataListNew;
                }
            }
        }
        return speedDataList;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1865682672)
    public synchronized void resetSpeedDataList() {
        speedDataList = null;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 955995478)
    public List<RMPData> getRmpDataList() {
        if (rmpDataList == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            RMPDataDao targetDao = daoSession.getRMPDataDao();
            List<RMPData> rmpDataListNew = targetDao._queryRouteData_RmpDataList(id);
            synchronized (this) {
                if (rmpDataList == null) {
                    rmpDataList = rmpDataListNew;
                }
            }
        }
        return rmpDataList;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 637369168)
    public synchronized void resetRmpDataList() {
        rmpDataList = null;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1604962013)
    public List<ThrottlePositionData> getThrottlePositionDataList() {
        if (throttlePositionDataList == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ThrottlePositionDataDao targetDao = daoSession.getThrottlePositionDataDao();
            List<ThrottlePositionData> throttlePositionDataListNew = targetDao
                    ._queryRouteData_ThrottlePositionDataList(id);
            synchronized (this) {
                if (throttlePositionDataList == null) {
                    throttlePositionDataList = throttlePositionDataListNew;
                }
            }
        }
        return throttlePositionDataList;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 950751996)
    public synchronized void resetThrottlePositionDataList() {
        throttlePositionDataList = null;
    }
}

