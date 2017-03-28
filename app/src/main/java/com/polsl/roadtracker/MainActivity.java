package com.polsl.roadtracker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.start_stop_button)
    Button actionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    public void onStartButtonClick(View v) {
        if (actionButton.getText().equals("START")) {
            actionButton.setText("END");
        } else if (actionButton.getText().equals("END")) {
            actionButton.setText("START");
        }
//        Intent intent = new Intent(MainActivity.this, MapActivity.class);
//        startActivity(intent);
    }

    public void onMenuItemMapClick(MenuItem w) {
        Intent intent = new Intent(MainActivity.this, MapActivity.class);
        startActivity(intent);
    }

}
