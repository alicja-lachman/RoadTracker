package com.polsl.roadtracker.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.annotations.Since;
import com.polsl.roadtracker.MainService;
import com.polsl.roadtracker.R;
import com.polsl.roadtracker.dagger.di.component.DaggerDatabaseComponent;
import com.polsl.roadtracker.dagger.di.component.DatabaseComponent;
import com.polsl.roadtracker.dagger.di.module.DatabaseModule;
import com.polsl.roadtracker.database.entity.LocationDataDao;
import com.polsl.roadtracker.database.entity.RouteData;
import com.polsl.roadtracker.database.entity.RouteDataDao;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.start_stop_button)
    Button actionButton;
    @Inject
    RouteDataDao routeDataDao;
    @Inject
    LocationDataDao locationDataDao;
    private Toast message;
    private RouteData route;
    private DatabaseComponent databaseComponent;
    private Intent intent;
    private Thread thread;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        if (isServiceRunning(MainService.class)) {
            actionButton.setText("END");
        } else {
            actionButton.setText("START");
        }
        injectDependencies();
        checkLocationOptions();
    }

    private void checkLocationOptions() {
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = false;
        boolean networkEnabled = false;
        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
        }
        try {
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
        }

        if (!gpsEnabled && !networkEnabled) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage(R.string.location_disabled);
            dialog.setPositiveButton(R.string.turn_on_location, (paramDialogInterface, paramInt) -> {
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);

            });
            dialog.setNegativeButton(R.string.cancel, (paramDialogInterface, paramInt) -> {

            });
            dialog.show();
        }
    }

    //TODO: might be useful when working in background, to save the state of activity and restore button state
//    private void updateValuesFromBundle(Bundle savedInstanceState) {
//        if (savedInstanceState != null) {
//             Update the value of mRequestingLocationUpdates from the Bundle, and
//             make sure that the Start Updates and Stop Updates buttons are
//             correctly enabled or disabled.
//            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
//                mRequestingLocationUpdates = savedInstanceState.getBoolean(
//                        REQUESTING_LOCATION_UPDATES_KEY);
//                setButtonsEnabledState();
//             }
//
//             Update the value of mCurrentLocation from the Bundle and update the
//             UI to show the correct latitude and longitude.
//            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
//             Since LOCATION_KEY was found in the Bundle, we can be sure that
//             mCurrentLocationis not null.
//            mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
//            }
//
//             Update the value of mLastUpdateTime from the Bundle and update the UI.
//            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
//             mLastUpdateTime = savedInstanceState.getString(
//                   LAST_UPDATED_TIME_STRING_KEY);
//            }
//        }
//    }
//
//    public void onSaveInstanceState(Bundle savedInstanceState) {
//        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY,
//                mRequestingLocationUpdates);
//        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
//        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
//         super.onSaveInstanceState(savedInstanceState);
//    }

    public void onStartButtonClick(View v) {
        if (actionButton.getText().equals("START")) {
            actionButton.setText("END");
            new Thread() {
                public void run() {
                    intent = new Intent(MainActivity.this, MainService.class);
                    intent.setAction("START");
                    startService(intent);
                }
            }.start();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.ending_trace_route)
                    .setMessage(R.string.ending_tracking_message)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            actionButton.setText("START");
                            if (intent == null) {
                                intent = new Intent(context, MainService.class); //TODO: this is just a placeholder, i will be thinking how to do it better way
                                intent.setAction("START");
                            }
                            stopService(intent);
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

//    public void onMenuItemMapClick(MenuItem w) {
//        Intent intent = new Intent(MainActivity.this, MapActivity.class);
//        startActivity(intent);
//    }

    public void onMenuItemListClick(MenuItem w) {
            Intent intent = new Intent(MainActivity.this, RouteListActivity.class);
            startActivity(intent);
    }

    public void onMenuItemSendClick(MenuItem w) {

        message = Toast.makeText(this, "Wysle dane", Toast.LENGTH_SHORT);
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

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isServiceRunning(MainService.class)) {
            actionButton.setText("END");
        } else {
            actionButton.setText("START");
        }
    }


}





