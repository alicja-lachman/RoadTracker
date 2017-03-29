package com.polsl.roadtracker.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.polsl.roadtracker.R;

public class RegisterActivity extends AppCompatActivity {
    private Toast message;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    public void onRegisterButtonClick(View v) {
        if (message!=null)
            message.cancel();
        message = Toast.makeText(this,"Register",Toast.LENGTH_SHORT);
        message.show();
    }
}
