package com.polsl.roadtracker;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;

public class SensorReader implements SensorEventListener {
    private SensorManager mSensorManager;
    private List<SensorValues> accelerometerValues;
    private List<SensorValues> gyroscopeValues;
    private List<SensorValues> magneticFieldValues;

    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;
    private static final float EPSILON = 0.000000001f;

    public SensorReader(SensorManager sm) {
        mSensorManager = sm;
        accelerometerValues = new ArrayList<SensorValues>();
        gyroscopeValues = new ArrayList<SensorValues>();
        magneticFieldValues = new ArrayList<SensorValues>();


        Sensor mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(mAccelerometer !=null) {
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        Sensor mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if(mGyroscope !=null){
            mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        }

        Sensor mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (mMagneticField!=null){
            mSensorManager.registerListener(this, mMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }



    public void finishSensorReadings(){
        mSensorManager.unregisterListener(this);
    }

    public List <SensorValues> getAccelerometerReadings(){
        List <SensorValues> returnedValues = this.accelerometerValues;
        if(this.accelerometerValues!=null)
            this.accelerometerValues.clear();
        return returnedValues;
    }

    public List <SensorValues> getGyroscopeReadings(){
        List <SensorValues> returnedValues = this.gyroscopeValues;
        if(this.gyroscopeValues!=null)
            this.gyroscopeValues.clear();
        return returnedValues;
    }

    public List <SensorValues> getMagneticFieldReadings(){
        List <SensorValues> returnedValues = this.gyroscopeValues;
        if (this.gyroscopeValues!=null)
            this.gyroscopeValues.clear();
        return returnedValues;
    }

    private SensorEvent filterReadings(SensorEvent event){
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){

        }else if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
            // This time step's delta rotation to be multiplied by the current rotation
            // after computing it from the gyro sample data.
            if (timestamp != 0) {
                final float dT = (event.timestamp - timestamp) * NS2S;
                // Axis of the rotation sample, not normalized yet.
                float axisX = event.values[0];
                float axisY = event.values[1];
                float axisZ = event.values[2];

                // Calculate the angular speed of the sample
                float omegaMagnitude = (float)sqrt(axisX*axisX + axisY*axisY + axisZ*axisZ);

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
                float sinThetaOverTwo = (float)sin(thetaOverTwo);
                float cosThetaOverTwo = (float)cos(thetaOverTwo);
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
        }

        return event;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        SensorValues sensorReading = new SensorValues(event.values, event.sensor.getName(), event.timestamp);
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            accelerometerValues.add(sensorReading);
        }
        else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
            gyroscopeValues.add(sensorReading);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
