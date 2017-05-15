package com.polsl.roadtracker.utility;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.github.pires.obd.commands.protocol.*;
import com.github.pires.obd.commands.engine.*;
import com.github.pires.obd.commands.*;
import com.github.pires.obd.exceptions.*;
import com.github.pires.obd.enums.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Jakub on 03.05.2017.
 */

public class ODBInterface {
    private String deviceAddress;
    private BluetoothSocket socket;
    private EditText textPanel;
    private Context context;
    public static final int REQUEST_ENABLE_BT = 99;

    public ODBInterface(Context con)
    {
        context=con;
    }
    private void setupODB() {
        final ArrayList deviceStrs = new ArrayList();
        final ArrayList devices = new ArrayList();

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter==null)
        {

        }else{
            if (!btAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                //context.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }

            Set pairedDevices = btAdapter.getBondedDevices();
            if (pairedDevices.size() > 0)
            {
                for (Object device : pairedDevices)
                {
                    BluetoothDevice device1 = (BluetoothDevice) device;
                    Log.d("gping2","BT: "+device1.getName() + " - " + device1.getAddress());
                    deviceStrs.add(device1.getName() + "\n" + device1.getAddress());
                    devices.add(device1.getAddress());
                }
            }

            // show list
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

            ArrayAdapter adapter = new ArrayAdapter(context, android.R.layout.select_dialog_singlechoice,
                    deviceStrs.toArray(new String[deviceStrs.size()]));

            alertDialog.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                    int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    deviceAddress = (String) deviceStrs.get(position);
                    Log.d("gping2","Picked: "+deviceAddress);
                }
            });

            alertDialog.setTitle("Choose Bluetooth device");
            alertDialog.show();
        }
    }

    private void connect_bt() {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

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

    public void test_odb() {
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
            String text;
            while (!Thread.currentThread().isInterrupted())
            {
                engineRpmCommand.run(socket.getInputStream(), socket.getOutputStream());
                speedCommand.run(socket.getInputStream(), socket.getOutputStream());
                text =  "RPM: " + engineRpmCommand.getFormattedResult()+ "\n"+
                        "Speed: " + speedCommand.getFormattedResult();
                textPanel.setText(text);
                // TODO handle commands result
                Log.d("gping2", "RPM: " + engineRpmCommand.getFormattedResult());
                //mAid.setText("RPM: " + engineRpmCommand.getFormattedResult());
                Log.d("gping2", "Speed: " + speedCommand.getFormattedResult());
            }
        } catch (MisunderstoodCommandException e) {
            Log.e("gping2", "MisunderstoodCommandException: "+e.toString());
        } catch (IOException e) {
            Log.e("gping2", "test error");
            e.printStackTrace();
        } catch (InterruptedException e) {
            Log.e("gping2", "test error");
            e.printStackTrace();
        }
    }
}
