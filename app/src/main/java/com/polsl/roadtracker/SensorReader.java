package com.polsl.roadtracker;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
public class SensorReader implements SensorEventListener {
    private SensorManager mSensorManager;
    private final Context context;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private List<SensorValues> sensorValues;

    public SensorReader(Context context){
        this.context=context;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(mAccelerometer!=null){
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if(mGyroscope!=null){
            mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void finishSensorReadings(){
        mSensorManager.unregisterListener(this);
    }

    public List <SensorValues> getSensorReadings(){
        return this.sensorValues;
    }

    private SensorEvent filterReadings(SensorEvent event){
        if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){

        }

        return event;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        SensorValues sensorReading = new SensorValues(event.values, event.sensor.getName(), event.timestamp);
        sensorValues.add(sensorReading);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
