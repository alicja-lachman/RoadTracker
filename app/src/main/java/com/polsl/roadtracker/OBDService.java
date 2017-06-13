package com.polsl.roadtracker;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.polsl.roadtracker.dagger.di.component.DaggerDatabaseComponent;
import com.polsl.roadtracker.dagger.di.component.DatabaseComponent;
import com.polsl.roadtracker.dagger.di.module.DatabaseModule;
import com.polsl.roadtracker.database.entity.RouteData;
import com.polsl.roadtracker.database.entity.RouteDataDao;
import com.polsl.roadtracker.utility.ODBInterface;

import javax.inject.Inject;


/**
 * @author m_ligus
 */
public class OBDService extends IntentService {

    @Inject
    RouteDataDao routeDataDao;

    DatabaseComponent databaseComponent;
    ODBInterface obdInterface;
    boolean finish = false;
    private String deviceAddress;
    long routeID;

    public OBDService() {
        super (".OBDService");
    }


    private void injectDependencies() {
        databaseComponent = DaggerDatabaseComponent.builder()
                .databaseModule(new DatabaseModule())
                .build();
        databaseComponent.inject(this);
    }


    @Override
    public void onCreate() { //<==to jako pierwsze
        super.onCreate();
        injectDependencies();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        obdInterface = new ODBInterface(this,preferences);
        finish = preferences.getBoolean("finish",false);
        deviceAddress = preferences.getString("deviceAddress","");
        Toast.makeText(this,"Create",Toast.LENGTH_SHORT).show();
        Log.d("MyService","OnCreate");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) { //<== drugie
        routeID = intent.getLongExtra("routeID",0);
        obdInterface.connect_bt(deviceAddress);
        obdInterface.startODBReadings(routeID);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        while (!finish) {
            finish = preferences.getBoolean("finish",false);
            if (!obdInterface.isConnected()) {
                obdInterface.connect_bt(deviceAddress);
                obdInterface.startODBReadings(routeID);
            }
        }
        obdInterface.finishODBReadings();
        obdInterface.disconnect();
    }




    @Override
    public void onDestroy() {
        Toast.makeText(this,"Delete",Toast.LENGTH_SHORT).show();
    }


}
