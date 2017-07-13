package com.polsl.roadtracker;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
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
import com.polsl.roadtracker.database.RoadtrackerDatabaseHelper;
import com.polsl.roadtracker.database.entity.DatabaseData;
import com.polsl.roadtracker.database.entity.DatabaseDataDao;
import com.polsl.roadtracker.database.entity.LocationData;
import com.polsl.roadtracker.database.entity.LocationDataDao;
import com.polsl.roadtracker.database.entity.RouteData;
import com.polsl.roadtracker.database.entity.RouteDataDao;
import com.polsl.roadtracker.event.RouteFinishedEvent;
import com.polsl.roadtracker.util.FileHelper;
import com.polsl.roadtracker.utility.ODBInterface;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * The Service that running in the background if user starts the collection of data
 *
 * @author m_ligus
 */
public class MainService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    LocationDataDao locationDataDao;
    RouteDataDao routeDataDao;
    @Inject
    DatabaseDataDao databaseDataDao;

    private RouteData route;
    private DatabaseComponent databaseComponent;
    private SensorReader sensorReader;
    private LocationRequest mLocationRequest;

    private GoogleApiClient mGoogleApiClient;
    private LocationSettingsRequest.Builder builder;

    private Location mCurrentLocation;
    private Long timestamp;
    private Handler mHandler;

    /**
     * Connector between the device and OBD interface
     */
    private ODBInterface ODBConnection;
    /**
     * Determine if the OBD interface should be used
     */
    private boolean useODB;
    /**
     * Determine if pause option is enabled
     */
    private boolean pauseEnab;
    /**
     * The OBD device address
     */
    private String deviceAddress;
    /**
     * Determine state of measurement
     */
    private boolean finish = false;
    private boolean obdConnected;
    /**
     * Power manager of the device
     */
    private PowerManager powerManager;
    /**
     * Wake lock used to maintain the device active
     */
    private PowerManager.WakeLock wakeLock;

    public DatabaseData getData() {
        return data;
    }

    private DatabaseData data;

    private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            if (level < 5)
                finishRoute();
            Timber.d("Battery level: " + level);
        }
    };

    /**
     * Calls when the Service is started.
     */
    @Override
    public void onCreate() {
        Timber.d("On create");
        super.onCreate();

        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        wakeLock.acquire();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        finish = preferences.getBoolean("finish", false);
        mHandler = new Handler();

        injectDependencies();
        buildGoogleApiClient();
        createLocationRequest();
        createBuilder();
        mGoogleApiClient.connect();
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    private void injectDependencies() {
        databaseComponent = DaggerDatabaseComponent.builder()
                .databaseModule(new DatabaseModule())
                .build();
        databaseComponent.inject(this);
    }

    /**
     * Calls when any command to service is send. Creates notification on start and stops service when Kill command is send.
     *
     * @param intent  Intent that passes
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_STICKY;
        }
        if (intent.getAction().equals("SELFKILL")) {
            this.stopForeground(true);
            if (!sensorReader.isPaused()) {
                stopLocationUpdate();
                sensorReader.finishSensorReadings();
                if (useODB) {
                    finish = true;
                    ODBConnection.finishODBReadings();
                    ODBConnection.disconnect();
                }
                route.finish();
                routeDataDao.update(route);
            }
            sensorReader.finishSensorReadings();
            wakeLock.release();
            this.stopSelf();
            EventBus.getDefault().post(new RouteFinishedEvent());

        } else if (intent.getAction().equals("START")) {
            Timber.d("Starting service");
            data = new DatabaseData();
            Long id = databaseDataDao.insert(data);
            data.setDatabaseName("dbRoute" + id);
            databaseDataDao.update(data);

            RoadtrackerDatabaseHelper.initialiseDbForRide(getApplicationContext(), data.getDatabaseName());
            routeDataDao = RoadtrackerDatabaseHelper.getDaoSessionForDb(data.getDatabaseName()).getRouteDataDao();
            locationDataDao = RoadtrackerDatabaseHelper.getDaoSessionForDb(data.getDatabaseName()).getLocationDataDao();
            if (sensorReader == null)
                sensorReader = new SensorReader((SensorManager) getSystemService(SENSOR_SERVICE), this, data.getDatabaseName());
            useODB = intent.getBooleanExtra("includeODB", false);
            pauseEnab = intent.getBooleanExtra("pauseEnab", false);
            finish = false;
            if (useODB) {
                deviceAddress = intent.getStringExtra("deviceAddress");
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                ODBConnection = new ODBInterface(this, preferences, data.getDatabaseName());//MainService.this.getShar...
            }
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

        } else if (intent.getAction().equals("STOP")) {
            finishRoute();
        }
        return START_STICKY;
    }

    private void finishRoute() {
        if (!sensorReader.isPaused()) {
            stopLocationUpdate();

            if (useODB) {
                finish = true;
                ODBConnection.finishODBReadings();
                ODBConnection.disconnect();
            }
            route.finish();
            routeDataDao.update(route);
        }
        sensorReader.finishSensorReadings();
        wakeLock.release();
        Timber.d("Yup, done");
        this.stopSelf();
    }

    /**
     * While service is running checks if reconnection with OBD device is required.
     */
    protected void maintainOBDConnection() {
        new Thread(() -> {
            ODBConnection.connect_bt(deviceAddress);
            ODBConnection.startODBReadings();
            while (!finish) {
                if (!ODBConnection.isConnected()) {
                    ODBConnection.connect_bt(deviceAddress);
                    ODBConnection.startODBReadings();
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    /**
     * Calls when device connected with maps provider. Starts location readings
     *
     * @param bundle
     */
    @Override
    public void onConnected(Bundle bundle) {
        mHandler.post(() -> {

            route = new RouteData();
            route.setDbName(data.getDatabaseName());
            routeDataDao.insert(route);
            route.start();

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
                LocationData locationData = new LocationData(timestamp, latitude, longitude);
                locationDataDao.insert(locationData);
            }
            startLocationUpdate();
            if (useODB) {
                maintainOBDConnection();
            }
            sensorReader.startSensorReading(MainService.this.getSharedPreferences("SensorReaderPreferences", Context.MODE_PRIVATE), mHandler);
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    /**
     * Called when location is changed
     *
     * @param location New location read
     */
    @Override
    public void onLocationChanged(Location location) {
        mHandler.post(() -> {
            mCurrentLocation = location;
            timestamp = System.currentTimeMillis();
            if (mCurrentLocation != null) {
                double longitude = mCurrentLocation.getLongitude();
                double latitude = mCurrentLocation.getLatitude();
                LocationData locationData = new LocationData(timestamp, latitude, longitude);
                locationDataDao.insert(locationData);
                Timber.d("Free internal memory: " + FileHelper.getFreeInternalMemory());
            }
        });
    }

    /**
     * Calls when service is ending
     */
    @Override
    public void onDestroy() {
        Timber.d("Is it done?");
        Toast.makeText(this, "Route saved", Toast.LENGTH_SHORT).show();
        unregisterReceiver(batteryReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    /**
     * Start location readings
     */
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

    /**
     * Stopping location readings
     */
    protected void stopLocationUpdate() {
        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        } catch (Exception e) {
            Timber.e("Keep on going, nothing to see here");
        }
    }

    /**
     * Build Google Api requested to gaining Location
     */
    protected void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    /**
     * Prepare location readings
     */
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

    /**
     * @return RouteData
     */
    public RouteData getRoute() {
        return route;
    }

    /**
     * @return
     */
    public GoogleApiClient getmGoogleApiClient() {
        return mGoogleApiClient;
    }

    /**
     * @return Current location of the device
     */
    public Location getmCurrentLocation() {
        return mCurrentLocation;
    }

    /**
     * @return
     */
    public Long getTimestamp() {
        return timestamp;
    }

    public ODBInterface getODBConnection() {
        return ODBConnection;
    }

    /**
     * @return Use OBD or not use
     */
    public boolean isUseODB() {
        return useODB;
    }

    /**
     * @return Param that indicate usage of Pause
     */
    public boolean isPauseEnab() {
        return pauseEnab;
    }

    /**
     * Set current location to that passed by param
     *
     * @param mCurrentLocation location
     */
    public void setmCurrentLocation(Location mCurrentLocation) {
        this.mCurrentLocation = mCurrentLocation;
    }

    /**
     * Set current timestamp passed by param
     *
     * @param timestamp timestamp
     */
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Set current route
     *
     * @param route route
     */
    public void setRoute(RouteData route) {
        this.route = route;
    }

    /**
     * @return value Finish
     */
    public boolean isFinish() {
        return finish;
    }

    /**
     * Finish connection
     *
     * @param finish
     */
    public void setFinish(boolean finish) {
        this.finish = finish;
    }
}
