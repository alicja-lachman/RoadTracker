package com.polsl.roadtracker.database;

import org.greenrobot.greendao.converter.PropertyConverter;

/**
 * Created by alachman on 29.03.2017.
 */

public class UploadStatusPropertyConverter implements PropertyConverter<UploadStatus, Integer> {
    @Override
    public UploadStatus convertToEntityProperty(Integer databaseValue) {
        return UploadStatus.of(databaseValue);
    }

    @Override
    public Integer convertToDatabaseValue(UploadStatus entityProperty) {
        return entityProperty.getId();
    }
}