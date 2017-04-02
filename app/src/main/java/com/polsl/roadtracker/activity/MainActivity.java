package com.polsl.roadtracker.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.polsl.roadtracker.SensorReader;
import com.polsl.roadtracker.R;
import com.polsl.roadtracker.dagger.di.component.DaggerDatabaseComponent;
import com.polsl.roadtracker.dagger.di.component.DatabaseComponent;
import com.polsl.roadtracker.dagger.di.module.DatabaseModule;
import com.polsl.roadtracker.database.entity.RouteData;
import com.polsl.roadtracker.database.entity.RouteDataDao;


import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity{
    @BindView(R.id.start_stop_button)
    Button actionButton;

    private Toast message;

    @Inject
    RouteDataDao routeDataDao;

    private RouteData route;
    private DatabaseComponent databaseComponent;
    private SensorReader sensorReader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        injectDependencies();
        if(sensorReader==null)
            sensorReader = new SensorReader((SensorManager)getSystemService(SENSOR_SERVICE));
    }

    public void onStartButtonClick(View v) {
        if (actionButton.getText().equals("START")) {
            actionButton.setText("END");
            route = new RouteData();
            route.start();
            sensorReader.startSensorReading(route.getId());
            routeDataDao.insert(route);
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.ending_trace_route)
                    .setMessage(R.string.ending_tracking_message)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            actionButton.setText("START");
                            sensorReader.finishSensorReadings();
                            route.finish();
                            routeDataDao.update(route);
                            //TODO more stuff - example saving our road into local database
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .show();

        }
    }

    public void onMenuItemMapClick(MenuItem w) {
        Intent intent = new Intent(MainActivity.this, MapActivity.class);
        startActivity(intent);
    }

    public void onMenuItemListClick(MenuItem w) {
        Intent intent = new Intent(MainActivity.this, RouteListActivity.class);
        startActivity(intent);
    }

    public void onMenuItemSendClick(MenuItem w) {
        message = Toast.makeText(this,"Wysle dane",Toast.LENGTH_SHORT);
        message.show();
    }

    public void testClick(MenuItem w) {
        Intent intent = new Intent(MainActivity.this, ExampleActivity.class);
        startActivity(intent);
    }

    private void injectDependencies() {
        databaseComponent = DaggerDatabaseComponent.builder()
                .databaseModule(new DatabaseModule())
                .build();
        databaseComponent.inject(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}





