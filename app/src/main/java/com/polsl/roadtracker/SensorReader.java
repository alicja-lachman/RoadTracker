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

//import java.util.ArrayList;
//import java.util.List;

import javax.inject.Inject;

//import static java.lang.Math.*;

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
    /*private List<SensorValues> accelerometerValues;
    private List<SensorValues> gyroscopeValues;
    private List<SensorValues> magneticFieldValues;
    private List<SensorValues> ambientTemperatureValues;


    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;
    private static final float EPSILON = 0.000000001f;*/

    public SensorReader(SensorManager sm, long id) {
        mSensorManager = sm;
        routeId = id;
        injectDependencies();
        /*accelerometerValues = new ArrayList<SensorValues>();
        gyroscopeValues = new ArrayList<SensorValues>();
        magneticFieldValues = new ArrayList<SensorValues>();
        ambientTemperatureValues = new ArrayList<SensorValues>();*/
    }

    private void injectDependencies() {
        databaseComponent = DaggerDatabaseComponent.builder()
                .databaseModule(new DatabaseModule())
                .build();
        databaseComponent.inject(this);
    }

    public void startSensorReading() {
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

    /*public List<SensorValues> getAccelerometerReadings() {
        List<SensorValues> returnedValues = new ArrayList<SensorValues>();
        returnedValues.addAll(this.accelerometerValues);
        if (this.accelerometerValues != null)
            this.accelerometerValues.clear();
        return returnedValues;
    }

    public List<SensorValues> getGyroscopeReadings() {
        List<SensorValues> returnedValues = new ArrayList<SensorValues>();
        returnedValues.addAll(this.gyroscopeValues);
        if (this.gyroscopeValues != null)
            this.gyroscopeValues.clear();
        return returnedValues;
    }

    public List<SensorValues> getMagneticFieldReadings() {
        List<SensorValues> returnedValues = new ArrayList<SensorValues>();
        returnedValues.addAll(this.magneticFieldValues);
        if (this.gyroscopeValues != null)
            this.gyroscopeValues.clear();
        return returnedValues;
    }

    public List<SensorValues> getAmbientTemperatureReadings() {
        List<SensorValues> returnedValues = new ArrayList<SensorValues>();
        returnedValues.addAll(this.ambientTemperatureValues);
        if (this.ambientTemperatureValues != null)
            this.ambientTemperatureValues.clear();
        return returnedValues;
    }*/

    @Override
    public void onSensorChanged(SensorEvent event) {
        SensorValues sensorReading = new SensorValues(event.values, event.sensor.getName(), event.timestamp);
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            //do przemyślenia - moment zapisy może być całkiem oddalony w czasie od czasu eventu
            AccelometerData accelometerData = new AccelometerData(System.currentTimeMillis(),x, y, z, routeId);
            accelometerDataDao.insert(accelometerData);
            //accelerometerValues.add(sensorReading);
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            GyroscopeData gyroscopeData = new GyroscopeData(System.currentTimeMillis(), x, y, z, routeId);
            gyroscopeDataDao.insert(gyroscopeData);
            //gyroscopeValues.add(sensorReading);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            MagneticFieldData magneticFieldData = new MagneticFieldData(System.currentTimeMillis(), x, y, z, routeId);
            magneticFieldDataDao.insert(magneticFieldData);
            //magneticFieldValues.add(sensorReading);
        } else if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            AmbientTemperatureData ambientTemperatureData = new AmbientTemperatureData(
                    System.currentTimeMillis(), event.values[0], routeId
            );
            ambientTemperatureDataDao.insert(ambientTemperatureData);
            //ambientTemperatureValues.add(sensorReading);
        }
    }
/*
    private SensorEvent filterGyroscopeReadings(SensorEvent event) {
        // This time step's delta rotation to be multiplied by the current rotation
        // after computing it from the gyro sample data.
        if (timestamp != 0) {
            final float dT = (event.timestamp - timestamp) * NS2S;
            // Axis of the rotation sample, not normalized yet.
            float axisX = event.values[0];
            float axisY = event.values[1];
            float axisZ = event.values[2];

            // Calculate the angular speed of the sample
            float omegaMagnitude = (float) sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);

            // Normalize the rotation vector if it's big enough to get the axis
            if (omegaMagnitude > EPSILON) {
                axisX /= omegaMagnitude;
                axisY /= omegaMagnitude;
                axisZ /= omegaMagnitude;
            }

            // Integrate around this axis with the angular speed by the time step
            // in order to get a delta rotation from this sample over the time step
            // We will convert this axis-angle representation of the delta rotation
            // into a quaternion before turning it into the rotation matrix.
            float thetaOverTwo = omegaMagnitude * dT / 2.0f;
            float sinThetaOverTwo = (float) sin(thetaOverTwo);
            float cosThetaOverTwo = (float) cos(thetaOverTwo);
            deltaRotationVector[0] = sinThetaOverTwo * axisX;
            deltaRotationVector[1] = sinThetaOverTwo * axisY;
            deltaRotationVector[2] = sinThetaOverTwo * axisZ;
            deltaRotationVector[3] = cosThetaOverTwo;
        }
        timestamp = event.timestamp;
        float[] deltaRotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
        // User code should concatenate the delta rotation we computed with the current rotation
        // in order to get the updated rotation.
        // rotationCurrent = rotationCurrent * deltaRotationMatrix;
        return event;
    }*/

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
