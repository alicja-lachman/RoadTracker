package com.polsl.roadtracker.activity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.polsl.roadtracker.MainService;
import com.polsl.roadtracker.R;
import com.polsl.roadtracker.dagger.di.component.DaggerDatabaseComponent;
import com.polsl.roadtracker.dagger.di.component.DatabaseComponent;
import com.polsl.roadtracker.dagger.di.module.DatabaseModule;
import com.polsl.roadtracker.database.entity.AccelometerData;
import com.polsl.roadtracker.database.entity.AccelometerDataDao;
import com.polsl.roadtracker.database.entity.RouteData;
import com.polsl.roadtracker.database.entity.RouteDataDao;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class ExampleActivity extends AppCompatActivity implements SensorEventListener {

    @Inject
    RouteDataDao routeDataDao;
    @Inject
    AccelometerDataDao accelometerDataDao;

    private SensorManager sensorManager;
    private Sensor accelometer;
    private long lastUpdate;
    private RouteData route;
    private DatabaseComponent databaseComponent;
    private Intent intent;

    @OnClick(R.id.start_button)
    public void onStartClicked(View view) {
        //route = new RouteData();
        //route.start();
        //routeDataDao.insert(route);
        intent = new Intent(this, MainService.class);
        //intent.putExtra("routeID",route.getId());
        startService(intent);

    }

    @OnClick(R.id.stop_button)
    public void onStopClicked(View view) {
        if (intent == null) {
            intent = new Intent(this, MainService.class);
        }
        stopService(intent);
        //route.finish();
        //routeDataDao.update(route);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example2);
        ButterKnife.bind(this);
        injectDependencies();
        lastUpdate = System.currentTimeMillis();

    }

    private void injectDependencies() {
        databaseComponent = DaggerDatabaseComponent.builder()
                .databaseModule(new DatabaseModule())
                .build();
        databaseComponent.inject(this);
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


        saveAccelometerToDatabase(x, y, z);


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

    private void saveAccelometerToDatabase(float x, float y, float z) {
        AccelometerData accelometerData = new AccelometerData(System.currentTimeMillis(), x, y, z, route.getId());
        accelometerDataDao.insert(accelometerData);
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
