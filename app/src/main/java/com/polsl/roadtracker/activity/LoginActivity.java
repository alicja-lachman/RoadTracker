package com.polsl.roadtracker.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import com.polsl.roadtracker.util.PasswordEncoder;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class LoginActivity extends AppCompatActivity {
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    @BindView(R.id.et_login)
    EditText etLogin;
    @BindView(R.id.et_password)
    EditText etPassword;
    @BindView(R.id.activity_login)
    LinearLayout parentView;
    private Toast message;
    private RoadtrackerService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        apiService = new RoadtrackerService();
        KeyboardHelper.setupUI(parentView, this);
        checkLocationPermission();
    }

    public void onLoginButtonClick(View v) {
        Credentials credentials = new Credentials(etLogin.getText().toString(),
                PasswordEncoder.encodePassword(etPassword.getText().toString()));
        apiService.login(credentials, authResponse -> {
            if (authResponse.getAuthToken() != null) {
                SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                prefs.edit().putString(Constants.AUTH_TOKEN, authResponse.getAuthToken()).apply();
                message = Toast.makeText(LoginActivity.this, R.string.correct_login, Toast.LENGTH_LONG);
                message.show();
                getSensorSettings();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            } else {
                String info = getString(R.string.login_failed) + " " + authResponse.getReason();
                message = Toast.makeText(LoginActivity.this, info, Toast.LENGTH_LONG);
                message.show();
            }
        });


    }

    private void getSensorSettings() {
        String authToken = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).getString(Constants.AUTH_TOKEN, null);
        if (authToken != null) {
            apiService.getSensorSettings(authToken, sensorSettingsResponse -> {
                if (sensorSettingsResponse.getSensorSettings() != null) {
                    SensorSettings sensorSettings = sensorSettingsResponse.getSensorSettings();
                    SharedPreferences sharedPref = LoginActivity.this.getSharedPreferences("SensorReaderPreferences", Context.MODE_PRIVATE);
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

    public void onRegisterClick(View v) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    //TODO: if no permission, stop application because it can't work properly
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user asynchronously -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }
}
