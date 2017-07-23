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
import com.polsl.roadtracker.util.Base64Encoder;
import com.polsl.roadtracker.util.Constants;
import com.polsl.roadtracker.util.KeyboardHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import timber.log.Timber;

/**
 * Activity that allows user to log in (when account already exists), change server address or go to register view.
 */
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
    @BindView(R.id.server_address_et)
    EditText serverAddress;

    /**
     * method managing visibility of serverAddress editText, based on checkbox state.
     *
     * @param isChecked
     */
    @OnCheckedChanged(R.id.custom_server_checkbox)
    public void onCheckboxClicked(boolean isChecked) {
        if (isChecked) {
            serverAddress.setVisibility(View.VISIBLE);
        } else {
            serverAddress.setVisibility(View.GONE);
            SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
            prefs.edit().putString(Constants.URL, null).apply();
        }
    }

    /**
     * Method invoked after creating activity, responsible for preparing the view and setup.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        KeyboardHelper.setupUI(parentView, this);
        checkLocationPermission();
        checkLogin();
    }

    /**
     * Method checking if the user is already logged in. If yes, the LoginActivity is skipped
     * and the user is moved to MainActivity.
     */
    private void checkLogin() {
        SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        String token = prefs.getString(Constants.AUTH_TOKEN, null);
        if (token != null) {
            getSensorSettings();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            finish();
            startActivity(intent);
        }
    }

    /**
     * Method sending login request to server.
     *
     * @param v
     */
    public void onLoginButtonClick(View v) {

        Credentials credentials = new Credentials(etLogin.getText().toString(),
                Base64Encoder.encodeData(etPassword.getText().toString()));
        if (serverAddress.getVisibility() == View.VISIBLE) {
            if (serverAddress.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please provide server address!", Toast.LENGTH_LONG).show();
                return;
            } else {
                SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                prefs.edit().putString(Constants.URL, serverAddress.getText().toString()).apply();
            }
        }
        apiService = new RoadtrackerService(this);
        apiService.login(credentials, authResponse -> {
            if (authResponse.getAuthToken() != null) {
                SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                prefs.edit().putString(Constants.AUTH_TOKEN, authResponse.getAuthToken()).apply();
                message = Toast.makeText(LoginActivity.this, R.string.correct_login, Toast.LENGTH_LONG);
                message.show();
                getSensorSettings();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                String info = getString(R.string.login_failed) + " " + authResponse.getReason();
                message = Toast.makeText(LoginActivity.this, info, Toast.LENGTH_LONG);
                message.show();
            }
        });


    }

    /**
     * Method used for getting sensor settings from server.
     */
    private void getSensorSettings() {
        String authToken = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).getString(Constants.AUTH_TOKEN, null);
        if (authToken != null) {
            if (apiService == null)
                apiService = new RoadtrackerService(this);
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

    /**
     * Method used for transferring user to RegisterActivity, after clicking on Register button.
     *
     * @param v
     */
    public void onRegisterClick(View v) {
        if (serverAddress.getVisibility() == View.VISIBLE) {
            if (serverAddress.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please provide server address!", Toast.LENGTH_LONG).show();
                return;
            } else {
                SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                prefs.edit().putString(Constants.URL, serverAddress.getText().toString()).apply();
            }
        }
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    /**
     * Method used for requesting user permission for location access.
     *
     * @return
     */
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
