package com.polsl.roadtracker.utility;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.polsl.roadtracker.dagger.di.component.DatabaseComponent;
import com.polsl.roadtracker.database.entity.LocationData;
import com.polsl.roadtracker.database.entity.LocationDataDao;

/**
 * Created by m_lig on 15.04.2017.
 */

public class LocationReader implements LocationListener {
    private LocationDataDao locationDataDao;
    private DatabaseComponent databaseComponent;
    private Long routeId;

    public LocationReader() {


    }

    public void setId(Long routeId) {
        this.routeId = routeId;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            LocationData locationData = new LocationData(System.currentTimeMillis(), latitude, longitude, routeId);
            locationDataDao.insert(locationData);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
