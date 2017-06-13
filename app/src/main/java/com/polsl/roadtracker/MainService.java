package com.polsl.roadtracker;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.polsl.roadtracker.activity.MainActivity;
import com.polsl.roadtracker.dagger.di.component.DaggerDatabaseComponent;
import com.polsl.roadtracker.dagger.di.component.DatabaseComponent;
import com.polsl.roadtracker.dagger.di.module.DatabaseModule;
import com.polsl.roadtracker.database.entity.LocationData;
import com.polsl.roadtracker.database.entity.LocationDataDao;
import com.polsl.roadtracker.database.entity.RouteData;
import com.polsl.roadtracker.database.entity.RouteDataDao;
import com.polsl.roadtracker.event.RouteFinishedEvent;
import com.polsl.roadtracker.utility.ODBInterface;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import timber.log.Timber;

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
    private Handler mHandler;

    private ODBInterface ODBConnection;
    private boolean useODB;
    private boolean pauseEnab;
    private String deviceAddress;
    private boolean finish = false;
    private boolean obdConnected;

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
//        finish = preferences.getBoolean("finish",false);
        mHandler = new Handler();
        if (sensorReader == null)
            sensorReader = new SensorReader((SensorManager) getSystemService(SENSOR_SERVICE), this);
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
        if (intent.getAction().equals("SELFKILL")) {
            this.stopForeground(true);
            if (!sensorReader.isPaused()) {
                stopLocationUpdate();
                sensorReader.finishSensorReadings();
//                if (useODB) {
//                    finish = true;
 //                   ODBConnection.finishODBReadings();
//                    ODBConnection.disconnect();
//                }
                route.finish();
                routeDataDao.update(route);
            }
            sensorReader.finishSensorReadings();
            this.stopSelf();
            EventBus.getDefault().post(new RouteFinishedEvent());

        } else if (intent.getAction().equals("START")) {
//            useODB = intent.getBooleanExtra("includeODB",false);
            pauseEnab = intent.getBooleanExtra("pauseEnab", false);
            finish = false;
//            if (useODB) {
//                deviceAddress = intent.getStringExtra("deviceAddress");
//                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
//                ODBConnection = new ODBInterface(this, preferences);//MainService.this.getShar...
//            }
            Intent showApplicationIntent = new Intent(this, MainActivity.class);
            Intent stopSelf = new Intent(this, MainService.class);
            stopSelf.setAction("SELFKILL");
            PendingIntent pstopSelf = PendingIntent.getService(this, 0, stopSelf, 0);
            PendingIntent pshowApplicationIntent = PendingIntent.getActivity(this, 0, showApplicationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            Notification notification = new NotificationCompat.Builder(this)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setSmallIcon(R.drawable.logo2)
                    .setContentIntent(pshowApplicationIntent)
                    .setContentTitle("Road Tracker is running")
                    .addAction(android.R.drawable.ic_media_pause, "Stop", pstopSelf)
                    .build();
            startForeground(100,
                    notification);

            route = new RouteData();
            routeDataDao.insert(route);
            route.start();
            Intent routeIntent = new Intent("RouteData");
            routeIntent.putExtra("routeID",route.getId());
            sendBroadcast(routeIntent);

        } else if (intent.getAction().equals("STOP")) {
            if (!sensorReader.isPaused()) {
                stopLocationUpdate();

//                if (useODB) {
//                    finish = true;
//                    ODBConnection.finishODBReadings();
//                    ODBConnection.disconnect();
//                }
                route.finish();
                routeDataDao.update(route);
            }
            sensorReader.finishSensorReadings();
            Timber.d("Yup, done");
            this.stopSelf();
        }
        return START_STICKY;
    }

//    protected void maintainOBDConnection() {
//        new Thread(() -> {
//            ODBConnection.connect_bt(deviceAddress);
//            ODBConnection.startODBReadings(route.getId());
//            while (!finish) {
//                if (!ODBConnection.isConnected()) {
//                    ODBConnection.connect_bt(deviceAddress);
//                    ODBConnection.startODBReadings(route.getId());
//                }
//            }
//        }).start();
//    }


    @Override
    public void onConnected(Bundle bundle) {
        mHandler.post(() -> {
            if (ActivityCompat.checkSelfPermission(MainService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(MainService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: not really needed, cause it's at login activity
            }
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            timestamp = System.currentTimeMillis();
            if (mCurrentLocation != null) {
                double longitude = mCurrentLocation.getLongitude();
                double latitude = mCurrentLocation.getLatitude();
                LocationData locationData = new LocationData(timestamp, latitude, longitude, route.getId());
                locationDataDao.insert(locationData);
            }
            startLocationUpdate();
//            if (useODB) {
//                maintainOBDConnection();
//            }
            sensorReader.startSensorReading(route.getId(), MainService.this.getSharedPreferences("SensorReaderPreferences", Context.MODE_PRIVATE), mHandler);
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onLocationChanged(Location location) {
        mHandler.post(() -> {
            mCurrentLocation = location;
            timestamp = System.currentTimeMillis();
            if (mCurrentLocation != null) {
                double longitude = mCurrentLocation.getLongitude();
                double latitude = mCurrentLocation.getLatitude();
                LocationData locationData = new LocationData(timestamp, latitude, longitude, route.getId());
                locationDataDao.insert(locationData);
            }
        });
    }

    @Override
    public void onDestroy() {
        Timber.d("Is it done?");
        Toast.makeText(this, "Route saved", Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }


    protected void startLocationUpdate() {
        mHandler.post(() -> {
            if (ActivityCompat.checkSelfPermission(MainService.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(MainService.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: not really needed, cause it's at login activity
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, MainService.this);
        });
    }


    protected void stopLocationUpdate() {
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

    public RouteData getRoute() {
        return route;
    }

    public GoogleApiClient getmGoogleApiClient() {
        return mGoogleApiClient;
    }

    public Location getmCurrentLocation() {
        return mCurrentLocation;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public ODBInterface getODBConnection() {
        return ODBConnection;
    }

    public boolean isUseODB() {
        return useODB;
    }

    public boolean isPauseEnab() {
        return pauseEnab;
    }

    public void setmCurrentLocation(Location mCurrentLocation) {
        this.mCurrentLocation = mCurrentLocation;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public void setRoute(RouteData route) {
        this.route = route;
    }

    public boolean isFinish() {
        return finish;
    }

    public void setFinish(boolean finish) {
        this.finish = finish;
    }
}
