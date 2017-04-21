package com.polsl.roadtracker.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.polsl.roadtracker.MainService;
import com.polsl.roadtracker.SensorReader;
import com.polsl.roadtracker.R;
import com.polsl.roadtracker.dagger.di.component.DaggerDatabaseComponent;
import com.polsl.roadtracker.dagger.di.component.DatabaseComponent;
import com.polsl.roadtracker.dagger.di.module.DatabaseModule;
import com.polsl.roadtracker.database.entity.LocationData;
import com.polsl.roadtracker.database.entity.LocationDataDao;
import com.polsl.roadtracker.database.entity.RouteData;
import com.polsl.roadtracker.database.entity.RouteDataDao;



import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.polsl.roadtracker.activity.LoginActivity.MY_PERMISSIONS_REQUEST_LOCATION;

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
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        injectDependencies();
    }

    //TODO: will be useful when working in background, to save the state of activity and restore button state
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and
            // make sure that the Start Updates and Stop Updates buttons are
            // correctly enabled or disabled.
            //if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
            //    mRequestingLocationUpdates = savedInstanceState.getBoolean(
            //            REQUESTING_LOCATION_UPDATES_KEY);
            //    setButtonsEnabledState();
           // }

            // Update the value of mCurrentLocation from the Bundle and update the
            // UI to show the correct latitude and longitude.
            //if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that
                // mCurrentLocationis not null.
                //mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            //}

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            //if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
               // mLastUpdateTime = savedInstanceState.getString(
                 //       LAST_UPDATED_TIME_STRING_KEY);
            //}
        }
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        //savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY,
        //        mRequestingLocationUpdates);
        //savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        //savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
       // super.onSaveInstanceState(savedInstanceState);
    }

    public void onStartButtonClick(View v) {
        //TODO: button still need to "remember" his status
        if (actionButton.getText().equals("START")) {
            actionButton.setText("END");
            intent = new Intent(this, MainService.class);
            startService(intent);
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.ending_trace_route)
                    .setMessage(R.string.ending_tracking_message)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            actionButton.setText("START");
                            if (intent == null) {
                                intent = new Intent(context, MainService.class); //TODO: this is just a placeholder, i will be thinking how to do it better way
                            }
                            stopService(intent);

                            //TODO: save actual state of button(if already clicked start)
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
        if (actionButton.getText().equals("START")) {
            Intent intent = new Intent(MainActivity.this, RouteListActivity.class);
            startActivity(intent);
        } else {
            message = Toast.makeText(this, "Podczas aktywnego pomiaru nie można przeglądać listy", Toast.LENGTH_SHORT);
            message.show();
        }
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
}





