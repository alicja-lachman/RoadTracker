package com.polsl.roadtracker.activity;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.polsl.roadtracker.MainService;
import com.polsl.roadtracker.R;
import com.polsl.roadtracker.api.RoadtrackerService;
import com.polsl.roadtracker.model.LogoutData;
import com.polsl.roadtracker.util.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Main activity responsible for starting and stopping route tracking.
 */
public class MainActivity extends AppCompatActivity {
    @BindView(R.id.start_stop_button)
    Button actionButton;
    @BindView(R.id.navigation_view)
    NavigationView navigationView;
    @BindView(R.id.obd_status_text)
    TextView OBDStatusText;
    @BindView(R.id.drawer)
    DrawerLayout drawerLayout;
    @BindView(R.id.obd_status)
    TextView OBDStatus;
    @BindView(R.id.pause_status)
    TextView pauseStatus;

    Context context = this;


    private Intent intent;
    /**
     * instance of RoadtrackerService, used to communicate with server.
     */
    private RoadtrackerService apiService;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    /**
     * Flag indicating if ODB2 data should also be tracked.
     */
    private boolean includeODB = false;
    /**
     * Flag indicating if route should be automatically paused when there is no movement.
     */
    private boolean pauseEnab = false;
    private String deviceAddress = "", deviceName;

    /**
     * Broadcast receiver for receiving messages from ODB2.
     */
    private BroadcastReceiver obdReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            OBDStatusText.setText(intent.getStringExtra("message"));
        }
    };
    /**
     * Broadcast receiver receiving battery level.
     */
    private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            if (level < 5)
                actionButton.setText("START");
            Timber.d("Battery level: " + level);
        }
    };

    /**
     * Method invoked after creating activity, responsible for preparing the view and setup.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        prepareNavigationDrawer();
        setupObdAndPause();
        if (isServiceRunning(MainService.class)) {
            actionButton.setText("END");
        } else {
            actionButton.setText("START");
        }
        checkLocationOptions();
        apiService = new RoadtrackerService(this);

    }

    private void setupObdAndPause() {
        SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        includeODB = prefs.getBoolean(Constants.OBD_ENABLED, false);
        pauseEnab = prefs.getBoolean(Constants.PAUSE_ENABLED, false);

        if (includeODB)
            OBDStatus.setText("ON");
        else
            OBDStatus.setText("OFF");
        if (pauseEnab)
            pauseStatus.setText("ON");
        else
            pauseStatus.setText("OFF");
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

    /**
     * Method used for checking location options and displaying a message when location is disabled.
     */
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

    /**
     * Method starting route when no route is started and finishing route when the route is already started.
     *
     * @param v
     */
    public void onStartButtonClick(View v) {
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        deviceAddress = sharedPreferences.getString("deviceAddress", "");
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Location permission has not been granted. This application works correctly only when location permission is accepted. " +
                    "If you want to use it, you should grand permission to the application in Android Phone Settings." +
                    " Application will be closed.")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                finishAndRemoveTask();
                            } else {
                                System.exit(0);
                            }
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            if (actionButton.getText().equals("START")) {
                if (deviceAddress.equals("") && includeODB) {
                    Toast.makeText(this, "You want to use OBD connection without choose device", Toast.LENGTH_SHORT).show();
                } else {
                    actionButton.setText("END");
                    new Thread() {
                        public void run() {
                            intent = new Intent(MainActivity.this, MainService.class);
                            intent.setAction("START");
                            intent.putExtra("includeODB", includeODB);
                            intent.putExtra("pauseEnab", pauseEnab);
                            intent.putExtra("deviceAddress", deviceAddress);
                            startService(intent);
                        }
                    }.start();
                }
            } else {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.ending_trace_route)
                        .setMessage(R.string.ending_tracking_message)
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            finishRoute();
                            OBDStatusText.setText("");
                        })
                        .setNegativeButton(android.R.string.no, (dialog, which) -> {
                            // do nothing
                        })
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();
            }
        }
    }
    /**
     * Method used for finishing the route and sending intent to MainService.
     */
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

    /**
     * Method opening RouteListActivity.
     *
     * @param w
     */
    public void onMenuItemListClick(MenuItem w) {
        Intent intent = new Intent(MainActivity.this, RouteListActivity.class);
        startActivity(intent);
    }

    /**
     * Method used for logging out.
     *
     * @param w
     */
    public void onMenuItemLogoutClick(MenuItem w) {
        SharedPreferences preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        String authToken = preferences.getString(Constants.AUTH_TOKEN, null);
        preferences.edit().putString(Constants.AUTH_TOKEN, null).apply();
        preferences.edit().putString(Constants.URL, null).apply();
        apiService.logout(new LogoutData(authToken), basicResponse -> {
        });
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    /**
     * Method used for checking if service is running.
     *
     * @param serviceClass
     * @return
     */
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Lifecycle method, used for registering receivers and buttons setup.
     */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(obdReceiver, new IntentFilter("OBDStatus"));
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (isServiceRunning(MainService.class)) {
            actionButton.setText("END");
        } else {
            actionButton.setText("START");
        }
    }

    /**
     * Lifecycle method, used for unregistering the receivers.
     */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(obdReceiver);
        unregisterReceiver(batteryReceiver);
    }

    /**
     * Method for opening SettingsActivity.
     *
     * @param item
     */
    public void onMenuItemSettingsClick(MenuItem item) {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }
}





