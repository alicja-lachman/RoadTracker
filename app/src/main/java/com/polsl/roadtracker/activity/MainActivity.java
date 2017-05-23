package com.polsl.roadtracker.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.polsl.roadtracker.MainService;
import com.polsl.roadtracker.R;
import com.polsl.roadtracker.api.RoadtrackerService;
import com.polsl.roadtracker.dagger.di.component.DaggerDatabaseComponent;
import com.polsl.roadtracker.dagger.di.component.DatabaseComponent;
import com.polsl.roadtracker.dagger.di.module.DatabaseModule;
import com.polsl.roadtracker.database.entity.LocationDataDao;
import com.polsl.roadtracker.database.entity.RouteData;
import com.polsl.roadtracker.database.entity.RouteDataDao;
import com.polsl.roadtracker.event.RouteFinishedEvent;
import com.polsl.roadtracker.model.LogoutData;
import com.polsl.roadtracker.util.Constants;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.start_stop_button)
    Button actionButton;
    @BindView(R.id.navigation_view)
    NavigationView navigationView;
    @BindView(R.id.drawer)
    DrawerLayout drawerLayout;
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
    private RoadtrackerService apiService;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Switch ODBSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        prepareNavigationDrawer();
        if (isServiceRunning(MainService.class)) {
            actionButton.setText("END");
        } else {
            actionButton.setText("START");
        }
        injectDependencies();
        checkLocationOptions();
        apiService = new RoadtrackerService();
        LinearLayout layout = (LinearLayout) View.inflate(this,R.layout.actionbar_obd_toogle,null);
        ODBSwitch = (Switch) layout.findViewById(R.id.obd_toggle_button);
        ODBSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    message = Toast.makeText(context,"ddddddddddddddddddddddddddddddddddddddddddddddddddddd",Toast.LENGTH_SHORT);
                    message.show();
                } else {
                    message = Toast.makeText(context,"ffffffffffffffffffffffffffffffffffffffffffffffffffffff",Toast.LENGTH_SHORT);
                    message.show();
                }
            }
        });
    }

    private void prepareNavigationDrawer() {
        actionBarDrawerToggle = new ActionBarDrawerToggle(this,
                drawerLayout, R.string.app_name, R.string.app_name) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        actionBarDrawerToggle.syncState();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        finishRoute();
                    })
                    .setNegativeButton(android.R.string.no, (dialog, which) -> {
                        // do nothing
                    })
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .show();
        }
    }

    private void finishRoute() {
        Timber.d("Finished route");
        actionButton.setText("START");
        if (intent == null) {
            intent = new Intent(context, MainService.class); //TODO: this is just a placeholder, i will be thinking how to do it better way
            intent.setAction("STOP");
        }
        intent = new Intent(MainActivity.this, MainService.class);
        intent.setAction("STOP");
        startService(intent);
    }


    public void onMenuItemListClick(MenuItem w) {
        Intent intent = new Intent(MainActivity.this, RouteListActivity.class);
        startActivity(intent);
    }

    public void onMenuItemLogoutClick(MenuItem w) {
        SharedPreferences preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        String authToken = preferences.getString(Constants.AUTH_TOKEN, null);
        preferences.edit().putString(Constants.AUTH_TOKEN, null).apply();
        apiService.logout(new LogoutData(authToken), basicResponse -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
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

    public void ODBMenuClick(View w) {


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

    @SuppressWarnings("unused")
    @Subscribe
    public void onRouteFinished(RouteFinishedEvent event) {
        finishRoute();
    }


}





