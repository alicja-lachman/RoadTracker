package com.polsl.roadtracker.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.polsl.roadtracker.R;
import com.polsl.roadtracker.util.Constants;

import java.util.ArrayList;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;

/**
 * Activity that allows user to change application's settings.
 */
public class SettingsActivity extends AppCompatActivity {

    @BindView(R.id.pause_toggle_button)
    Switch pauseSwitch;
    @BindView(R.id.obd_toggle_button)
    Switch OBDSwitch;
    SharedPreferences sharedPreferences;
    private String deviceName;
    private String deviceAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        OBDSwitch.setChecked(sharedPreferences.getBoolean(Constants.OBD_ENABLED, false));
        pauseSwitch.setChecked(sharedPreferences.getBoolean(Constants.PAUSE_ENABLED, false));
    }

    @OnCheckedChanged(R.id.obd_toggle_button)
    public void OBDOptionsClick(CompoundButton w, boolean checked) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        OBDSwitch.setChecked(!OBDSwitch.isChecked());
        OBDSwitch.setChecked(checked);
        if (OBDSwitch.isChecked()) {
            chooseDevice();
        }
        editor.putBoolean(Constants.OBD_ENABLED, OBDSwitch.isChecked());
        editor.apply();

    }

    @OnCheckedChanged(R.id.pause_toggle_button)
    public void pauseButtonClick(CompoundButton view, boolean checked) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        pauseSwitch.setChecked(!pauseSwitch.isChecked());
        pauseSwitch.setChecked(checked);
        editor.putBoolean(Constants.PAUSE_ENABLED, pauseSwitch.isChecked());
        editor.apply();
    }


    public void chooseDevice() {
        final ArrayList deviceStrs = new ArrayList();
        final ArrayList devices = new ArrayList();
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        int REQUEST_ENABLE_BT = 99;
        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        Set pairedDevices = btAdapter.getBondedDevices();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String previousDeviceAddress = sharedPreferences.getString("previousDeviceAddress", "");
        if (pairedDevices.size() > 0) {
            for (Object device : pairedDevices) {
                BluetoothDevice device1 = (BluetoothDevice) device;
                Log.d("gping2", "BT: " + device1.getName() + " - " + device1.getAddress());
                deviceStrs.add(device1.getName() + "\n" + device1.getAddress());
                devices.add(device1.getAddress());
                if (previousDeviceAddress.equals(device1.getAddress())) {
                    deviceName = device1.getName();
                }
            }
        }
        final android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(this);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.select_dialog_singlechoice,
                deviceStrs.toArray(new String[deviceStrs.size()]));
        alertDialog.setSingleChoiceItems(adapter, -1, (dialog, which) -> {
            dialog.dismiss();
            int position = ((android.app.AlertDialog) dialog).getListView().getCheckedItemPosition();
            deviceAddress = (String) devices.get(position);
            editor.putString("deviceAddress", deviceAddress);
            editor.apply();
            deviceName = (String) deviceStrs.get(position);
        });
        alertDialog.setTitle("Choose Bluetooth device");
        alertDialog.show();
    }

}
