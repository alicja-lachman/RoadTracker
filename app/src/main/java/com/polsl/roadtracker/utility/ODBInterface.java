package com.polsl.roadtracker.utility;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.github.pires.obd.commands.protocol.*;
import com.github.pires.obd.commands.engine.*;
import com.github.pires.obd.commands.*;
import com.github.pires.obd.exceptions.*;
import com.github.pires.obd.enums.*;
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
import java.util.ArrayList;
import java.util.Set;
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
    private boolean useOldAddress=false;
    public static final int REQUEST_ENABLE_BT = 99;


    //shared pref jak w MainService linia 151
    public ODBInterface(Context con, SharedPreferences sharedPref)
    {
        context=con;
        sharedPreferences = sharedPref;
        injectDependencies();
    }

    private void injectDependencies() {
        databaseComponent = DaggerDatabaseComponent.builder()
                .databaseModule(new DatabaseModule())
                .build();
        databaseComponent.inject(this);
    }

    public void setupODB() {
        final ArrayList deviceStrs = new ArrayList();
        final ArrayList devices = new ArrayList();

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter==null)
        {
            //TODO inform user that his device don't have requited module
        }else{
            //TODO .
            if (!btAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                context.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }

            Set pairedDevices = btAdapter.getBondedDevices();
            String previousDeviceAddress = sharedPreferences.getString("previousDeviceAddress","");
            if (pairedDevices.size() > 0)
            {
                for (Object device : pairedDevices)
                {
                    BluetoothDevice device1 = (BluetoothDevice) device;
                    Log.d("gping2","BT: "+device1.getName() + " - " + device1.getAddress());
                    deviceStrs.add(device1.getName() + "\n" + device1.getAddress());
                    devices.add(device1.getAddress());
                    if(previousDeviceAddress.equals(device1.getAddress())){
                        useOldAddress=true;
                        deviceName = device1.getName();
                    }
                }
            }

            // show list
            if(!useOldAddress) {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

                ArrayAdapter adapter = new ArrayAdapter(context, android.R.layout.select_dialog_singlechoice,
                        deviceStrs.toArray(new String[deviceStrs.size()]));

                alertDialog.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        deviceAddress = (String) devices.get(position);
                        deviceName = (String)deviceStrs.get(position);
                        Log.d("gping2", "Picked: " + deviceAddress);
                        saveNewAddress();
                        connect_bt();
                    }
                });
                alertDialog.setTitle("Choose Bluetooth device");
                alertDialog.show();
            }else{
                connect_bt();
            }
        }
    }

    private void saveNewAddress(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(deviceAddress, "previousDeviceAddress");
        editor.commit();
    }

    private void connect_bt() {
        Toast.makeText(context, "Trying to connect with device " + deviceName , Toast.LENGTH_SHORT).show();
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        btAdapter.cancelDiscovery();
        BluetoothDevice device = btAdapter.getRemoteDevice(deviceAddress);

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        try {
            socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
            socket.connect();
            Log.d("gping2","Connected: "+uuid);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("gping2","BT connect error");
        }
    }

    public void finishODBReadings(){
        readValues=false;
    }

    public void disconnect(){
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startODBReadings(Long id) {
        routeId=id;
        try {

            new EchoOffCommand().run(socket.getInputStream(), socket.getOutputStream());

            new LineFeedOffCommand().run(socket.getInputStream(), socket.getOutputStream());

            try {
                new TimeoutCommand(10).run(socket.getInputStream(), socket.getOutputStream());
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
            readValues=true;
            while (socket.isConnected() && readValues)
            {
                engineRpmCommand.run(socket.getInputStream(), socket.getOutputStream());
                float rmpValue = Float.parseFloat(engineRpmCommand.getFormattedResult());
                RMPData rmpData = new RMPData(System.currentTimeMillis(), rmpValue, id);
                rmpDataDao.insert(rmpData);

                speedCommand.run(socket.getInputStream(), socket.getOutputStream());
                float speedValue = Float.parseFloat(speedCommand.getFormattedResult());
                SpeedData speedData = new SpeedData(System.currentTimeMillis(), speedValue, id);
                speedDataDao.insert(speedData);

                throttlePositionCommand.run(socket.getInputStream(), socket.getOutputStream());
                float throttlePositionValue = Float.parseFloat(throttlePositionCommand.getFormattedResult());
                ThrottlePositionData throttlePositionData = new ThrottlePositionData(System.currentTimeMillis(), throttlePositionValue, id);
                throttlePositionDataDao.insert(throttlePositionData);

            }
        } catch (MisunderstoodCommandException e) {
            Log.e("gping2", "MisunderstoodCommandException: "+e.toString());
        } catch (IOException e) {
            Log.e("gping2", "test error");
            e.printStackTrace();
        } catch (InterruptedException e) {
            Log.e("gping2", "test error");
            e.printStackTrace();
        }finally {
            readValues=false;
        }
    }
}
