package com.polsl.roadtracker.activity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.polsl.roadtracker.R;
import com.polsl.roadtracker.RoadTrackerApplication;
import com.polsl.roadtracker.database.entity.AccelometerData;
import com.polsl.roadtracker.database.entity.AccelometerDataDao;
import com.polsl.roadtracker.database.entity.DaoSession;
import com.polsl.roadtracker.database.entity.RouteData;
import com.polsl.roadtracker.database.entity.RouteDataDao;

import java.util.Date;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class ExampleActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelometer;
    private long lastUpdate;
    private DaoSession daoSession;
    private RouteDataDao routeDataDao;
    private AccelometerDataDao accelometerDataDao;
    private RouteData route;


    @OnClick(R.id.start_button)
    public void onStartClicked(View view) {
        route = new RouteData();
        route.setStartDate(new Date(System.currentTimeMillis()));
        routeDataDao.insert(route);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @OnClick(R.id.stop_button)
    public void onStopClicked(View view) {
        sensorManager.unregisterListener(this);
        route.finish();
        routeDataDao.update(route);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example2);
        ButterKnife.bind(this);
        daoSession = ((RoadTrackerApplication) getApplication()).getDaoSession();
        routeDataDao = daoSession.getRouteDataDao();
        accelometerDataDao = daoSession.getAccelometerDataDao();
        lastUpdate = System.currentTimeMillis();

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void getAccelerometer(SensorEvent event) {
        float[] values = event.values;
        // Movement
        float x = values[0];
        float y = values[1];
        float z = values[2];

        AccelometerData accelometerData = new AccelometerData(System.currentTimeMillis(), x, y, z, route.getId());
        accelometerDataDao.insert(accelometerData);

        float accelationSquareRoot = (x * x + y * y + z * z)
                / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
        long actualTime = event.timestamp;
        if (accelationSquareRoot >= 2) //
        {
            if (actualTime - lastUpdate < 200) {
                return;
            }
            lastUpdate = actualTime;
            Toast.makeText(this, "Device was shuffed", Toast.LENGTH_SHORT)
                    .show();


        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // register this class as a listener for the orientation and
        // accelerometer sensors
        if (sensorManager != null)
            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        // unregister listener
        super.onPause();
        if (sensorManager != null)
            sensorManager.unregisterListener(this);
    }
}
