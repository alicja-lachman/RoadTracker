package com.polsl.roadtracker.utility;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.engine.ThrottlePositionCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.HeadersOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.ObdResetCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.SpacesOffCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;
import com.github.pires.obd.exceptions.MisunderstoodCommandException;
import com.github.pires.obd.exceptions.NoDataException;
import com.github.pires.obd.exceptions.UnableToConnectException;
import com.polsl.roadtracker.database.RoadtrackerDatabaseHelper;
import com.polsl.roadtracker.database.entity.DaoSession;
import com.polsl.roadtracker.database.entity.RpmData;
import com.polsl.roadtracker.database.entity.RpmDataDao;
import com.polsl.roadtracker.database.entity.SpeedData;
import com.polsl.roadtracker.database.entity.SpeedDataDao;
import com.polsl.roadtracker.database.entity.ThrottlePositionData;
import com.polsl.roadtracker.database.entity.ThrottlePositionDataDao;
import com.polsl.roadtracker.model.ObdSetDefaultCommand;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by Jakub on 03.05.2017.
 */

public class ODBInterface {


    SpeedDataDao speedDataDao;

    RpmDataDao rpmDataDao;

    ThrottlePositionDataDao throttlePositionDataDao;

    private String deviceAddress, deviceName;
    private BluetoothSocket socket;
    private Context context;
    private static Long responseDelay = 100L;
    private boolean readValues;
    private SharedPreferences sharedPreferences;
    private boolean isConnected = false;
    private boolean useOldAddress = false;

    //shared pref jak w MainService linia 151

    public ODBInterface(Context con, SharedPreferences sharedPref, String databaseName) {
        context = con;
        sharedPreferences = sharedPref;
        DaoSession daoSession = RoadtrackerDatabaseHelper.getDaoSessionForDb(databaseName);
        speedDataDao = daoSession.getSpeedDataDao();
        throttlePositionDataDao = daoSession.getThrottlePositionDataDao();
        rpmDataDao = daoSession.getRpmDataDao();

    }


    private void saveNewAddress() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(deviceAddress, "previousDeviceAddress");
        editor.commit();
    }

    public void connect_bt(String deviceAddress) {
        Handler handler = new Handler(Looper.getMainLooper());
        Intent intent = new Intent("OBDStatus");
        intent.putExtra("message", "Trying to connect with device");
        context.sendBroadcast(intent);
        disconnect();
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        btAdapter.cancelDiscovery();
        BluetoothDevice device = btAdapter.getRemoteDevice(deviceAddress);

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        try {
            socket = device.createRfcommSocketToServiceRecord(uuid);
            socket.connect();
            isConnected = true;
            handler = new Handler(Looper.getMainLooper());
            intent.putExtra("message", "Connected");
            context.sendBroadcast(intent);
        } catch (IOException e) {
            Log.e("gping2", "There was an error while establishing Bluetooth connection. Falling back..", e);
            Class<?> clazz = socket.getRemoteDevice().getClass();
            Class<?>[] paramTypes = new Class<?>[]{Integer.TYPE};
            BluetoothSocket sockFallback = null;
            try {
                Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                Object[] params = new Object[]{Integer.valueOf(1)};
                sockFallback = (BluetoothSocket) m.invoke(socket.getRemoteDevice(), params);
                sockFallback.connect();
                isConnected = true;
                socket = sockFallback;
            } catch (Exception e2) {
                handler = new Handler(Looper.getMainLooper());
                //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                //SharedPreferences.Editor editor = preferences.edit();
                //editor.putBoolean("finish", true);
                // editor.apply();
                intent.putExtra("message", "OBD Connection error");
                context.sendBroadcast(intent);
                Log.e("gping2", "BT connect error");
            }
        }
        saveNewAddress();
    }


    public void finishODBReadings() {
        readValues = false;
    }


    public void disconnect() {
        try {
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        isConnected = false;
    }

    public void startODBReadings() {
        try {

            readValues = true;
            new Thread() {
                public void run() {
                    boolean goodRPM = true;
                    boolean goodSpeed = true;
                    boolean goodPosition = true;
                    try {
                        ObdSetDefaultCommand defaultCommand = new ObdSetDefaultCommand();
                        defaultCommand.setResponseTimeDelay(responseDelay);
                        defaultCommand.run(socket.getInputStream(), socket.getOutputStream());

                        ObdResetCommand obdResetCommand = new ObdResetCommand();
                        obdResetCommand.setResponseTimeDelay(responseDelay);
                        obdResetCommand.run(socket.getInputStream(), socket.getOutputStream());

                        EchoOffCommand echoOffCommand = new EchoOffCommand();
                        echoOffCommand.setResponseTimeDelay(responseDelay);
                        echoOffCommand.run(socket.getInputStream(), socket.getOutputStream());

                        LineFeedOffCommand lineFeedOffCommand = new LineFeedOffCommand();
                        lineFeedOffCommand.setResponseTimeDelay(responseDelay);
                        lineFeedOffCommand.run(socket.getInputStream(), socket.getOutputStream());

                        SpacesOffCommand spacesOffCommand = new SpacesOffCommand();
                        spacesOffCommand.setResponseTimeDelay(responseDelay);
                        spacesOffCommand.run(socket.getInputStream(), socket.getOutputStream());

                        HeadersOffCommand headersOffCommand = new HeadersOffCommand();
                        headersOffCommand.setResponseTimeDelay(responseDelay);
                        headersOffCommand.run(socket.getInputStream(), socket.getOutputStream());

                        SelectProtocolCommand selectProtocolCommand = new SelectProtocolCommand(ObdProtocols.AUTO);
                        selectProtocolCommand.setResponseTimeDelay(responseDelay);
                        selectProtocolCommand.run(socket.getInputStream(), socket.getOutputStream());

                        TimeoutCommand timeoutCommand = new TimeoutCommand(200);
                        timeoutCommand.setResponseTimeDelay(responseDelay);
                        timeoutCommand.run(socket.getInputStream(), socket.getOutputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    while (socket.isConnected() && readValues) {
                        try {
                            RPMCommand engineRpmCommand = new RPMCommand();
                            SpeedCommand speedCommand = new SpeedCommand();
                            ThrottlePositionCommand throttlePositionCommand = new ThrottlePositionCommand();

                            engineRpmCommand.setResponseTimeDelay(responseDelay);
                            speedCommand.setResponseTimeDelay(responseDelay);
                            throttlePositionCommand.setResponseTimeDelay(responseDelay);
                            try {
                                engineRpmCommand.run(socket.getInputStream(), socket.getOutputStream());
                                RpmData rpmData = new RpmData(System.currentTimeMillis(), engineRpmCommand.getRPM());
                                rpmDataDao.insert(rpmData);
                                Intent intent = new Intent("DATA");
                                intent.putExtra("engineRpm", engineRpmCommand.getFormattedResult());
                                context.sendBroadcast(intent);
                                goodRPM = true;
                            } catch (NoDataException e) {
                                goodRPM = false;
                            } catch (IndexOutOfBoundsException e) {
                            }
                            try {
                                speedCommand.run(socket.getInputStream(), socket.getOutputStream());
                                SpeedData speedData = new SpeedData(System.currentTimeMillis(), speedCommand.getMetricSpeed());
                                speedDataDao.insert(speedData);
                                goodSpeed = true;
                            } catch (NoDataException e) {
                                goodSpeed = false;
                            } catch (IndexOutOfBoundsException e) {
                            }
                            try {
                                throttlePositionCommand.run(socket.getInputStream(), socket.getOutputStream());
                                ThrottlePositionData throttlePositionData = new ThrottlePositionData(System.currentTimeMillis(), throttlePositionCommand.getPercentage());
                                throttlePositionDataDao.insert(throttlePositionData);
                                goodPosition = true;
                            } catch (NoDataException e) {
                                goodPosition = false;
                            } catch (IndexOutOfBoundsException e) {
                            }
                            if ((!goodPosition) && (!goodRPM) && (!goodSpeed)) {
                                finishODBReadings();
                                disconnect();
                                Intent intent = new Intent("OBDStatus");
                                intent.putExtra("message", "NO DATA received, trying to reconnect with device");
                                context.sendBroadcast(intent);

                            }
                        } catch (IOException e) {
                        } catch (InterruptedException e) {
                            Log.e("gping2", "test error");
                            e.printStackTrace();
                        } catch (UnableToConnectException e) {
                            finishODBReadings();
                            disconnect();
                            Intent intent = new Intent("OBDStatus");
                            intent.putExtra("message", "Unable to connect");
                            context.sendBroadcast(intent);
                        }
                    }
                }
            }.start();
        } catch (MisunderstoodCommandException e) {
            Log.e("gping2", "MisunderstoodCommandException: " + e.toString());

        }

    }

    public boolean isConnected() {
        return this.isConnected;
    }
}
