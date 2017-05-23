package com.polsl.roadtracker.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.polsl.roadtracker.R;
import com.polsl.roadtracker.api.RoadtrackerService;
import com.polsl.roadtracker.model.Credentials;
import com.polsl.roadtracker.model.SensorSettings;
import com.polsl.roadtracker.util.Constants;
import com.polsl.roadtracker.util.KeyboardHelper;
import com.polsl.roadtracker.util.Base64Encoder;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class RegisterActivity extends AppCompatActivity {
    private Toast message;
    @BindView(R.id.register_ll)
    LinearLayout parentView;
    @BindView(R.id.et_rlogin)
    EditText login;
    @BindView(R.id.et_rpassword)
    EditText password;
    @BindView(R.id.et_confirm_password)
    EditText confirmPassword;

    private RoadtrackerService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        service = new RoadtrackerService();
        KeyboardHelper.setupUI(parentView, this);
    }

    public void onRegisterButtonClick(View v) {
        if (validatePassword()) {
            Credentials credentials = new Credentials("Heniu", login.getText().toString(),
                    Base64Encoder.encodeData(password.getText().toString()));
            service.register(credentials, authResponse -> {
                if (authResponse.getAuthToken() != null) {
                    SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                    prefs.edit().putString(Constants.AUTH_TOKEN, authResponse.getAuthToken()).apply();
                    message = Toast.makeText(RegisterActivity.this, R.string.correct_login, Toast.LENGTH_LONG);
                    message.show();
                     getSensorSettings();
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    String info = getString(R.string.login_failed) + " " + authResponse.getReason();
                    message = Toast.makeText(RegisterActivity.this, info, Toast.LENGTH_LONG);
                    message.show();
                }
            });

        } else {
            if (message != null)
                message.cancel();
            message = Toast.makeText(this, "Your password and confirm password don't match", Toast.LENGTH_SHORT);
            message.show();
        }
    }

    private void getSensorSettings() {
        String authToken = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).getString(Constants.AUTH_TOKEN, null);
        if (authToken != null) {
            service.getSensorSettings(authToken, sensorSettingsResponse -> {
                if (sensorSettingsResponse.getSensorSettings() != null) {
                    SensorSettings sensorSettings = sensorSettingsResponse.getSensorSettings();
                    SharedPreferences sharedPref = RegisterActivity.this.getSharedPreferences("SensorReaderPreferences", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt("accelerometerSamplingPeriod", (int) (long) (sensorSettings.getAccelometer()));
                    editor.putInt("gyroscopeSamplingPeriod", (int) (long) (sensorSettings.getGyroscope()));
                    editor.putInt("magneticFieldSamplingPeriod", (int) (long) (sensorSettings.getMagneticField()));
                    editor.putInt("ambientTemperatureSamplingPeriod", (int) (long) (sensorSettings.getAmbientTemperature()));
                    editor.commit();
                } else {
                    Timber.e("Couldn't get sensor settings: " + sensorSettingsResponse.getReason());
                }
            });
        }
    }
    private boolean validatePassword() {
        return (confirmPassword.getText().toString().equals(password.getText().toString()));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }
}
