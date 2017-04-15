package com.polsl.roadtracker;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.polsl.roadtracker.dagger.di.component.DaggerDatabaseComponent;
import com.polsl.roadtracker.dagger.di.component.DatabaseComponent;
import com.polsl.roadtracker.dagger.di.module.DatabaseModule;
import com.polsl.roadtracker.database.entity.AccelometerData;
import com.polsl.roadtracker.database.entity.AccelometerDataDao;
import com.polsl.roadtracker.database.entity.AmbientTemperatureData;
import com.polsl.roadtracker.database.entity.AmbientTemperatureDataDao;
import com.polsl.roadtracker.database.entity.GyroscopeData;
import com.polsl.roadtracker.database.entity.GyroscopeDataDao;
import com.polsl.roadtracker.database.entity.MagneticFieldData;
import com.polsl.roadtracker.database.entity.MagneticFieldDataDao;

import javax.inject.Inject;

public class SensorReader implements SensorEventListener {

    @Inject
    AccelometerDataDao accelometerDataDao;

    @Inject
    GyroscopeDataDao gyroscopeDataDao;

    @Inject
    MagneticFieldDataDao magneticFieldDataDao;

    @Inject
    AmbientTemperatureDataDao ambientTemperatureDataDao;

    private SensorManager mSensorManager;
    private DatabaseComponent databaseComponent;
    private Long routeId;

    public SensorReader(SensorManager sm) {
        mSensorManager = sm;
        injectDependencies();
    }

    private void injectDependencies() {
        databaseComponent = DaggerDatabaseComponent.builder()
                .databaseModule(new DatabaseModule())
                .build();
        databaseComponent.inject(this);
    }

    public void startSensorReading(long id) {
        routeId = id;



        Sensor mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (mAccelerometer != null) {
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        Sensor mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (mGyroscope != null) {
            mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        }

        Sensor mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (mMagneticField != null) {
            mSensorManager.registerListener(this, mMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
        }

        Sensor mTemperature = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        if (mTemperature != null) {
            mSensorManager.registerListener(this, mTemperature, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void finishSensorReadings() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            //do przemyślenia - moment zapisu może być całkiem oddalony w czasie od czasu eventu
            AccelometerData accelometerData = new AccelometerData(System.currentTimeMillis(),x, y, z, routeId);
            accelometerDataDao.insert(accelometerData);
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            GyroscopeData gyroscopeData = new GyroscopeData(System.currentTimeMillis(), x, y, z, routeId);
            gyroscopeDataDao.insert(gyroscopeData);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            MagneticFieldData magneticFieldData = new MagneticFieldData(System.currentTimeMillis(), x, y, z, routeId);
            magneticFieldDataDao.insert(magneticFieldData);
        } else if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            AmbientTemperatureData ambientTemperatureData = new AmbientTemperatureData(
                    System.currentTimeMillis(), event.values[0], routeId
            );
            ambientTemperatureDataDao.insert(ambientTemperatureData);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
