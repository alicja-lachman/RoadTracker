package com.polsl.roadtracker;

import android.*;
import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.model.CircleOptions;
import com.polsl.roadtracker.activity.MainActivity;
import com.polsl.roadtracker.dagger.di.component.DaggerDatabaseComponent;
import com.polsl.roadtracker.dagger.di.component.DatabaseComponent;
import com.polsl.roadtracker.dagger.di.module.DatabaseModule;
import com.polsl.roadtracker.database.entity.LocationData;
import com.polsl.roadtracker.database.entity.LocationDataDao;
import com.polsl.roadtracker.database.entity.RouteData;
import com.polsl.roadtracker.database.entity.RouteDataDao;

import javax.inject.Inject;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;
import static com.polsl.roadtracker.activity.LoginActivity.MY_PERMISSIONS_REQUEST_LOCATION;

public class MainService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    @Inject
    LocationDataDao locationDataDao;
    @Inject
    RouteDataDao routeDataDao;
    private RouteData route;
    private DatabaseComponent databaseComponent;
    private SensorReader sensorReader;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private LocationSettingsRequest.Builder builder;
    private Location mCurrentLocation;
    private Long timestamp;
    long id;

    @Override
    public void onCreate() {
        super.onCreate();
        if (sensorReader == null)
            sensorReader = new SensorReader((SensorManager) getSystemService(SENSOR_SERVICE));
        injectDependencies();
        buildGoogleApiClient();
        createLocationRequest();
        createBuilder();
        mGoogleApiClient.connect();


    }

    private void injectDependencies() {
        databaseComponent = DaggerDatabaseComponent.builder()
                .databaseModule(new DatabaseModule())
                .build();
        databaseComponent.inject(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent stopService = new Intent(this, MainService.class);
        stopService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pstopService = PendingIntent.getService(this,0,stopService,FLAG_CANCEL_CURRENT);
        Notification notification = new NotificationCompat.Builder(this)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.logo2)
                .setContentIntent(pstopService)
                .setContentTitle("Road Tracker is running")
                .addAction(android.R.drawable.ic_media_pause,"Stop",pstopService)
                .build();
        startForeground(100,
                notification);
        return START_STICKY;
    }


    @Override
    public void onConnected(Bundle bundle) {
        route = new RouteData();
        route.start();
        routeDataDao.insert(route);
        startLocationUpdate();
        sensorReader.startSensorReading(route.getId());
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        timestamp = System.currentTimeMillis();
        if (mCurrentLocation != null) {
            double longitude = mCurrentLocation.getLongitude();
            double latitude = mCurrentLocation.getLatitude();
            LocationData locationData = new LocationData(timestamp,latitude,longitude,route.getId());
            locationDataDao.insert(locationData);
        }

    }

    @Override
    public void onDestroy() {
        sensorReader.finishSensorReadings();
        stopLocationUpdate();
        route.finish();
        routeDataDao.update(route);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: not really needed, cause it's at login activity

        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    private void stopLocationUpdate() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

    }

    protected void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

        }
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void createBuilder() {
        builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());
    }

}
