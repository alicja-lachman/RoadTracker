package com.polsl.roadtracker;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.widget.Toast;

import com.polsl.roadtracker.activity.MainActivity;

public class MainService extends Service {
    public MainService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO do something useful
        Toast message = Toast.makeText(getApplicationContext(),"jest",Toast.LENGTH_SHORT);
        message.show();
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
