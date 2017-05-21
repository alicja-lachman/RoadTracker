package com.polsl.roadtracker.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.polsl.roadtracker.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SendingActivity extends AppCompatActivity {
    @BindView(R.id.sending_text)
    TextView text;

    private ArrayList<Integer> ids = new ArrayList<>();
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sending);
        ButterKnife.bind(this);
        intent = getIntent();
        ids = intent.getIntegerArrayListExtra("IDs");
        String idsText = "";
        for(Integer i : ids) {
            idsText += i + ", ";
        }
        text.setText(idsText);
    }

}
