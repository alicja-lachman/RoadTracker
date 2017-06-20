package com.polsl.roadtracker.activity;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
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
import com.polsl.roadtracker.event.RouteFinishedEvent;
import com.polsl.roadtracker.model.LogoutData;
import com.polsl.roadtracker.util.Constants;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

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
    private RoadtrackerService apiService;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private boolean includeODB = false;
    private boolean pauseEnab = false;
    private String deviceAddress="", deviceName;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (includeODB = intent.getBooleanExtra("OBDEnabled",false))
                OBDStatus.setText("YES");
            else
                OBDStatus.setText("NO");
            if (pauseEnab = intent.getBooleanExtra("pauseEnabled",false))
                pauseStatus.setText("YES");
            else
                pauseStatus.setText("NO");
        }
    };
    private BroadcastReceiver obdReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            OBDStatusText.setText(intent.getStringExtra("message"));
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerReceiver(broadcastReceiver, new IntentFilter("settingsData"));
        registerReceiver(obdReceiver,new IntentFilter("OBDStatus"));
        ButterKnife.bind(this);
        prepareNavigationDrawer();
        if (isServiceRunning(MainService.class)) {
            actionButton.setText("END");
        } else {
            actionButton.setText("START");
        }
        checkLocationOptions();
        apiService = new RoadtrackerService(this);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean enable = sharedPreferences.getBoolean("OBDEnabled",false);
        includeODB = enable;
        if (enable) {
            OBDStatus.setText("ON");
        } else {
            OBDStatus.setText("OFF");
        }
        enable = sharedPreferences.getBoolean("pauseEnabled",false);
        pauseEnab = enable;
        if (enable) {
            pauseStatus.setText("ON");
        } else {
            pauseStatus.setText("OFF");
        }
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

    public void onStartButtonClick(View v) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        deviceAddress = sharedPreferences.getString("deviceAddress","");
        if (actionButton.getText().equals("START")) {
            if (deviceAddress.equals("")&&includeODB) {
                Toast.makeText(this,"You want to use OBD connection without choose device",Toast.LENGTH_SHORT).show();
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
        preferences.edit().putString(Constants.URL, null).apply();
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

    @SuppressWarnings("unused")
    @Subscribe
    public void onRouteFinished(RouteFinishedEvent event) {
        finishRoute();
    }

    public void onMenuItemSettingsClick(MenuItem item) {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }
}





