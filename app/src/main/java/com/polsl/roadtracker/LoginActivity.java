package com.polsl.roadtracker;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;

import android.widget.Button;

public class LoginActivity extends AppCompatActivity {
    private Toast message;
    private String login = "login";
    private String password = "password";
    @BindView(R.id.et_login)
    EditText etLogin;
    @BindView(R.id.et_password)
    EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
    }

    public void onLoginButtonClick(View v) {
        if (true){//login.equals(etLogin.getText().toString()) && password.equals(etPassword.getText().toString())) {
            Intent intent = new Intent(LoginActivity.this, MapActivity.class);
            startActivity(intent);
        } else {
            message = Toast.makeText(this, "Login or password incorrect", Toast.LENGTH_LONG);
            message.show();
        }
    }

    public void onRegisterClick(View v) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }
}
