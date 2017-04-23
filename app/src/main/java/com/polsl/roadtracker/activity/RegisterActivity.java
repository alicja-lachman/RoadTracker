package com.polsl.roadtracker.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.polsl.roadtracker.R;
import com.polsl.roadtracker.api.RoadtrackerService;
import com.polsl.roadtracker.util.KeyboardHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

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
            service.register(login.getText().toString(), password.getText().toString(), id -> {

                message = Toast.makeText(this, "You were registered correctly! Now login", Toast.LENGTH_SHORT);
                message.show();
            });
        } else {
            if (message != null)
                message.cancel();
            message = Toast.makeText(this, "Your password and confirm password don't match", Toast.LENGTH_SHORT);
            message.show();
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
