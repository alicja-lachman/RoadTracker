package com.polsl.roadtracker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    private Toast message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void onLoginButtonClick(View v) {
        if(message!=null)
            message.cancel();
        message = Toast.makeText(this, "Login", Toast.LENGTH_SHORT);
        message.show();
    }

    public void onRegisterClick(View v) {
        Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
        LoginActivity.this.startActivity(intent);
    }
}
