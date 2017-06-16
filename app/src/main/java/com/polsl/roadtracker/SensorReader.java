package com.polsl.roadtracker;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.location.LocationServices;
import com.polsl.roadtracker.dagger.di.component.DatabaseComponent;
import com.polsl.roadtracker.database.RoadtrackerDatabaseHelper;
import com.polsl.roadtracker.database.entity.AccelometerData;
import com.polsl.roadtracker.database.entity.AccelometerDataDao;
import com.polsl.roadtracker.database.entity.AmbientTemperatureData;
import com.polsl.roadtracker.database.entity.AmbientTemperatureDataDao;
import com.polsl.roadtracker.database.entity.GyroscopeData;
import com.polsl.roadtracker.database.entity.GyroscopeDataDao;
import com.polsl.roadtracker.database.entity.LocationData;
import com.polsl.roadtracker.database.entity.MagneticFieldData;
import com.polsl.roadtracker.database.entity.MagneticFieldDataDao;
import com.polsl.roadtracker.database.entity.RouteData;

public class SensorReader implements SensorEventListener {

    AccelometerDataDao accelometerDataDao;

    GyroscopeDataDao gyroscopeDataDao;

    MagneticFieldDataDao magneticFieldDataDao;

    AmbientTemperatureDataDao ambientTemperatureDataDao;

    private SensorManager mSensorManager;
    private DatabaseComponent databaseComponent;

    private SharedPreferences sharedPreferences;
    private Handler mHandler;
    private double lastValue;
    private long startTime;
    private boolean paused;
    private MainService mainService;

    public SensorReader(SensorManager sm) {
        mSensorManager = sm;

    }

    public SensorReader(SensorManager sm, MainService mService, String databaseName) {
        mSensorManager = sm;
        mainService = mService;
        accelometerDataDao = RoadtrackerDatabaseHelper.getDaoSessionForDb(databaseName).getAccelometerDataDao();
        gyroscopeDataDao = RoadtrackerDatabaseHelper.getDaoSessionForDb(databaseName).getGyroscopeDataDao();
        magneticFieldDataDao = RoadtrackerDatabaseHelper.getDaoSessionForDb(databaseName).getMagneticFieldDataDao();
        ambientTemperatureDataDao = RoadtrackerDatabaseHelper.getDaoSessionForDb(databaseName).getAmbientTemperatureDataDao();
    }

    public void startSensorReading(SharedPreferences sharedPref, Handler handler) {

        sharedPreferences = sharedPref;
        this.mHandler = handler;
        int samplingPeriod;
        paused = false;

        samplingPeriod = sharedPreferences.getInt("accelerometerSamplingPeriod", SensorManager.SENSOR_DELAY_NORMAL);
        if (samplingPeriod != -1) {
            Sensor mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (mAccelerometer != null) {
                mSensorManager.registerListener(this, mAccelerometer, samplingPeriod);
            }
        }

        samplingPeriod = sharedPreferences.getInt("gyroscopeSamplingPeriod", SensorManager.SENSOR_DELAY_NORMAL);
        if (samplingPeriod != -1) {
            Sensor mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            if (mGyroscope != null) {
                mSensorManager.registerListener(this, mGyroscope, samplingPeriod);
            }
        }

        samplingPeriod = sharedPreferences.getInt("magneticFieldSamplingPeriod", SensorManager.SENSOR_DELAY_NORMAL);
        if (samplingPeriod != -1) {
            Sensor mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            if (mMagneticField != null) {
                mSensorManager.registerListener(this, mMagneticField, samplingPeriod);
            }
        }

        samplingPeriod = sharedPreferences.getInt("ambientTemperatureSamplingPeriod", SensorManager.SENSOR_DELAY_NORMAL);
        if (samplingPeriod != -1) {
            Sensor mTemperature = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
            if (mTemperature != null) {
                mSensorManager.registerListener(this, mTemperature, samplingPeriod);
            }
        }
    }



    public void finishSensorReadings() {
        mSensorManager.unregisterListener(this);
    }

    public void pauseTracking() {
        mSensorManager.unregisterListener(this);
        int samplingPeriod = sharedPreferences.getInt("accelerometerSamplingPeriod", SensorManager.SENSOR_DELAY_NORMAL);
        Sensor mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (mAccelerometer != null) {
            mSensorManager.registerListener(this, mAccelerometer, samplingPeriod);
        }
        paused = true;

        /***********************************************************************/
        mainService.stopLocationUpdate();
        if (mainService.isUseODB()) {
            mainService.setFinish(true);
            mainService.getODBConnection().finishODBReadings();
            mainService.getODBConnection().disconnect();
        }
        mainService.getRoute().finish();
        mainService.routeDataDao.update(mainService.getRoute());
    }

    public void unpauseTracking() {
        int samplingPeriod = sharedPreferences.getInt("gyroscopeSamplingPeriod", SensorManager.SENSOR_DELAY_NORMAL);
        if (samplingPeriod != -1) {
            Sensor mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            if (mGyroscope != null) {
                mSensorManager.registerListener(this, mGyroscope, samplingPeriod);
            }
        }

        samplingPeriod = sharedPreferences.getInt("magneticFieldSamplingPeriod", SensorManager.SENSOR_DELAY_NORMAL);
        if (samplingPeriod != -1) {
            Sensor mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            if (mMagneticField != null) {
                mSensorManager.registerListener(this, mMagneticField, samplingPeriod);
            }
        }

        samplingPeriod = sharedPreferences.getInt("ambientTemperatureSamplingPeriod", SensorManager.SENSOR_DELAY_NORMAL);
        if (samplingPeriod != -1) {
            Sensor mTemperature = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
            if (mTemperature != null) {
                mSensorManager.registerListener(this, mTemperature, samplingPeriod);
            }
        }
        paused = false;

        /***********************************************************************/
        mainService.setRoute(new RouteData());
        mainService.routeDataDao.insert(mainService.getRoute());
        mainService.getRoute().start();
        if (ActivityCompat.checkSelfPermission(mainService, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mainService, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: not really needed, cause it's at login activity
        }
        mainService.setmCurrentLocation(LocationServices.FusedLocationApi.getLastLocation(
                mainService.getmGoogleApiClient()));
        mainService.setTimestamp(System.currentTimeMillis());
        if (mainService.getmCurrentLocation() != null) {
            double longitude = mainService.getmCurrentLocation().getLongitude();
            double latitude = mainService.getmCurrentLocation().getLatitude();
            LocationData locationData = new LocationData(mainService.getTimestamp(), latitude, longitude);
            mainService.locationDataDao.insert(locationData);
        }
        mainService.startLocationUpdate();
        mainService.setFinish(false);
        if (mainService.isUseODB()) {
            mainService.maintainOBDConnection();
        }
    }

    public boolean isPaused() {
        return paused;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        mHandler.post(() -> {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                if (!paused) {
                    AccelometerData accelometerData = new AccelometerData(System.currentTimeMillis(), x, y, z);
                    accelometerDataDao.insert(accelometerData);
                }

                if (mainService.isPauseEnab()) {
                    double tempAccValue = computeAccelerometerValues(event.values);//acc value
                    long difference;
                    if (tempAccValue > 1.1 * lastValue || tempAccValue < 0.9 * lastValue) {//if it was big enough change
                        lastValue = tempAccValue;
                        startTime = System.currentTimeMillis();
                        if (paused) {
                            unpauseTracking();
                        }
                    } else {
                        difference = System.currentTimeMillis() - startTime;
                        if (difference / 1000 > 180 && !paused) {
                            pauseTracking();
                        }
                    }
                }
            } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                GyroscopeData gyroscopeData = new GyroscopeData(System.currentTimeMillis(), x, y, z);
                gyroscopeDataDao.insert(gyroscopeData);
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                MagneticFieldData magneticFieldData = new MagneticFieldData(System.currentTimeMillis(), x, y, z);
                magneticFieldDataDao.insert(magneticFieldData);
            } else if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                AmbientTemperatureData ambientTemperatureData = new AmbientTemperatureData(
                        System.currentTimeMillis(), event.values[0]
                );
                ambientTemperatureDataDao.insert(ambientTemperatureData);
            }
        });
    }

    public double computeAccelerometerValues(float[] values) {
        return Math.sqrt(Math.pow(values[0], 2) + Math.pow(values[1], 2) + Math.pow(values[2], 2));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}