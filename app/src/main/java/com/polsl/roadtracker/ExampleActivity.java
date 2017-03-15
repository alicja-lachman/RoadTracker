package com.polsl.roadtracker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

//TODO: delete this activity
public class ExampleActivity extends AppCompatActivity {

    @BindView(R.id.surprise_text)
    TextView text;

    @OnClick(R.id.button)
    public void onButtonClick(View view) {
        Timber.d("You clicked on button!");
        if (text.getVisibility() == View.GONE)
            text.setVisibility(View.VISIBLE);
        else text.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example);
        ButterKnife.bind(this); //this is necessary in every activity that we want to use Butterknife in

    }
}

