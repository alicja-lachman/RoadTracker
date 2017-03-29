package com.polsl.roadtracker.database;

/**
 * Created by alachman on 29.03.2017.
 */


public enum UploadStatus {
    NOT_UPLOADED(0),
    READY_TO_UPLOAD(1),
    DURING_UPLOAD(2),
    UPLOADED(3),
    REMOVED(4);

    private int id;

    UploadStatus(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static UploadStatus of(Integer databaseValue) {
        for (UploadStatus uploadStatus : UploadStatus.values()) {
            if (uploadStatus.getId() != databaseValue) {
                continue;
            }
            return uploadStatus;
        }
        return null;
    }

}
