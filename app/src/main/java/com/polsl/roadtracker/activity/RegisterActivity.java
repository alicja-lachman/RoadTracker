package com.polsl.roadtracker.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.polsl.roadtracker.R;
import com.polsl.roadtracker.util.KeyboardHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RegisterActivity extends AppCompatActivity {
    private Toast message;
    @BindView(R.id.register_ll)
    LinearLayout parentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        KeyboardHelper.setupUI(parentView, this);
    }

    public void onRegisterButtonClick(View v) {
        if (message != null)
            message.cancel();
        message = Toast.makeText(this, "Register", Toast.LENGTH_SHORT);
        message.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);

    }
}
