package com.polsl.roadtracker.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.polsl.roadtracker.R;
import com.polsl.roadtracker.util.KeyboardHelper;

import java.util.List;

public class LoginActivity extends AppCompatActivity {
    private Toast message;
    private String login = "login";
    private String password = "password";
    @BindView(R.id.et_login)
    EditText etLogin;
    @BindView(R.id.et_password)
    EditText etPassword;
    @BindView(R.id.activity_login)
    LinearLayout parentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        KeyboardHelper.setupUI(parentView, this);
    }


    public void onLoginButtonClick(View v) {

        if (true) {
            int temp = SensorManager.SENSOR_DELAY_NORMAL;
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            setSharedPreferences(true, temp, true, temp, true, temp, true, temp);
            startActivity(intent);
        } else {
            message = Toast.makeText(this, R.string.incorrect_data, Toast.LENGTH_LONG);
            message.show();
        }
    }

    public void onRegisterClick(View v) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
        overridePendingTransition( R.anim.slide_in_right, R.anim.slide_out_right );
    }

    public void setSharedPreferences(boolean useAccelerometer, int accelerometerSamplingPeriod,
                                     boolean useGyroscope, int gyroscopeSamplingPeriod,
                                     boolean useMagneticField, int magneticFieldSamplingPeriod,
                                     boolean useAmbientTemperature, int ambientTemperatureSamplingPeriod){
        SharedPreferences sharedPref = this.getSharedPreferences("SensorReaderPreferences",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("useAccelerometer", useAccelerometer);
        editor.putInt("accelerometerSamplingPeriod", accelerometerSamplingPeriod);
        editor.putBoolean("useGyroscope", useGyroscope);
        editor.putInt("gyroscopeSamplingPeriod", gyroscopeSamplingPeriod);
        editor.putBoolean("useMagneticField", useMagneticField);
        editor.putInt("magneticFieldSamplingPeriod", magneticFieldSamplingPeriod);
        editor.putBoolean("useAmbientTemperature", useAmbientTemperature);
        editor.putInt("ambientTemperatureSamplingPeriod", ambientTemperatureSamplingPeriod);
        editor.commit();
    }
}
