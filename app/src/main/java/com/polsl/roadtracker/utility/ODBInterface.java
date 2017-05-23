package com.polsl.roadtracker.utility;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.engine.ThrottlePositionCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;
import com.github.pires.obd.exceptions.MisunderstoodCommandException;
import com.polsl.roadtracker.dagger.di.component.DaggerDatabaseComponent;
import com.polsl.roadtracker.dagger.di.component.DatabaseComponent;
import com.polsl.roadtracker.dagger.di.module.DatabaseModule;
import com.polsl.roadtracker.database.entity.RMPData;
import com.polsl.roadtracker.database.entity.RMPDataDao;
import com.polsl.roadtracker.database.entity.SpeedData;
import com.polsl.roadtracker.database.entity.SpeedDataDao;
import com.polsl.roadtracker.database.entity.ThrottlePositionData;
import com.polsl.roadtracker.database.entity.ThrottlePositionDataDao;

import java.io.IOException;
import java.util.UUID;

import javax.inject.Inject;

/**
 * Created by Jakub on 03.05.2017.
 */

public class ODBInterface {

    @Inject
    SpeedDataDao speedDataDao;

    @Inject
    RMPDataDao rmpDataDao;

    @Inject
    ThrottlePositionDataDao throttlePositionDataDao;

    private String deviceAddress, deviceName;
    private BluetoothSocket socket;
    private Context context;
    private static Long responseDelay = 100L;
    private boolean readValues;
    private Long routeId;
    private DatabaseComponent databaseComponent;
    private SharedPreferences sharedPreferences;
    private boolean useOldAddress = false;

    //shared pref jak w MainService linia 151
    public ODBInterface(Context con, SharedPreferences sharedPref) {
        context = con;
        sharedPreferences = sharedPref;
        injectDependencies();
    }

    private void injectDependencies() {
        databaseComponent = DaggerDatabaseComponent.builder()
                .databaseModule(new DatabaseModule())
                .build();
        databaseComponent.inject(this);
    }

    private void saveNewAddress() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(deviceAddress, "previousDeviceAddress");
        editor.commit();
    }

    public void connect_bt(String deviceAddress) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(context,
                        "Trying to connect with device ",
                        Toast.LENGTH_SHORT).show();
            }
        });

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        btAdapter.cancelDiscovery();
        BluetoothDevice device = btAdapter.getRemoteDevice(deviceAddress);

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        try {
            socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
            socket.connect();
            handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(context,
                            "Connected",
                            Toast.LENGTH_SHORT).show();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(context,
                            "ODB connection failed",
                            Toast.LENGTH_SHORT).show();
                }
            });
            Log.e("gping2", "BT connect error");
        }
        saveNewAddress();
    }

    public void finishODBReadings() {
        readValues = false;
    }

    public void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startODBReadings(Long id) {
        routeId = id;
        try {

            new EchoOffCommand().run(socket.getInputStream(), socket.getOutputStream());

            new LineFeedOffCommand().run(socket.getInputStream(), socket.getOutputStream());

            try {
                new TimeoutCommand(10).run(socket.getInputStream(), socket.getOutputStream());
                //new TimeoutObdCommand().run(socket.getInputStream(), socket.getOutputStream()); A MOZE TO?

            } catch (MisunderstoodCommandException e) {
                Log.d("gping2", "Timeout command not understood, hope that wasn't important..");
            }

            try {
                new SelectProtocolCommand(ObdProtocols.AUTO).run(socket.getInputStream(), socket.getOutputStream());
            } catch (MisunderstoodCommandException e) {
                Log.d("gping2", "Select protocol command failed");
            }

            RPMCommand engineRpmCommand = new RPMCommand();
            SpeedCommand speedCommand = new SpeedCommand();
            ThrottlePositionCommand throttlePositionCommand = new ThrottlePositionCommand();
            engineRpmCommand.setResponseTimeDelay(responseDelay);
            speedCommand.setResponseTimeDelay(responseDelay);
            throttlePositionCommand.setResponseTimeDelay(responseDelay);
            readValues = true;
            new Thread() {
                public void run() {
                    while (socket.isConnected() && readValues) {
                        try {
                            engineRpmCommand.run(socket.getInputStream(), socket.getOutputStream());
                            RMPData rmpData = new RMPData(System.currentTimeMillis(), engineRpmCommand.getRPM(), id);
                            rmpDataDao.insert(rmpData);

                            speedCommand.run(socket.getInputStream(), socket.getOutputStream());
                            SpeedData speedData = new SpeedData(System.currentTimeMillis(), speedCommand.getImperialSpeed(), id);
                            speedDataDao.insert(speedData);

                            throttlePositionCommand.run(socket.getInputStream(), socket.getOutputStream());
                            ThrottlePositionData throttlePositionData = new ThrottlePositionData(System.currentTimeMillis(), throttlePositionCommand.getPercentage(), id);
                            throttlePositionDataDao.insert(throttlePositionData);
                        } catch (IOException e) {
                        } catch (InterruptedException e) {
                            Log.e("gping2", "test error");
                            e.printStackTrace();
                        }
                    }
                }
            }.start();


        } catch (MisunderstoodCommandException e) {
            Log.e("gping2", "MisunderstoodCommandException: " + e.toString());
        } catch (IOException e) {
            Log.e("gping2", "test error");
            e.printStackTrace();
        } catch (InterruptedException e) {
            Log.e("gping2", "test error");
            e.printStackTrace();
        }
    }
}
