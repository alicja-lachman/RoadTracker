package com.polsl.roadtracker.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.Toast;

import com.polsl.roadtracker.R;
import com.polsl.roadtracker.adapter.RouteListAdapter;
import com.polsl.roadtracker.dagger.di.component.DaggerDatabaseComponent;
import com.polsl.roadtracker.dagger.di.component.DatabaseComponent;
import com.polsl.roadtracker.dagger.di.module.DatabaseModule;
import com.polsl.roadtracker.database.entity.RouteData;
import com.polsl.roadtracker.database.entity.RouteDataDao;
import com.polsl.roadtracker.utility.PositionInfo;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;


public class RouteListActivity extends AppCompatActivity {

    @Inject
    RouteDataDao routeDataDao;

    private List<RouteData> tracks = new ArrayList<>();
    private RouteListAdapter tAdapter;
    private RecyclerView routeList;
    private DatabaseComponent databaseComponent;
    private Toast message;

    private void injectDependencies() {
        databaseComponent = DaggerDatabaseComponent.builder()
                .databaseModule(new DatabaseModule())
                .build();
        databaseComponent.inject(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_list);
        injectDependencies();
        ButterKnife.bind(this);
        tracks = routeDataDao.loadAll();
        routeList = (RecyclerView) findViewById(R.id.route_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        routeList.setLayoutManager(layoutManager);
        tAdapter = new RouteListAdapter(tracks, RouteListActivity.this);
        routeList.setAdapter(tAdapter);
        routeList.invalidate();
    }

    //TODO match with actual activity(currently: matching with MainActivity)
    public void onMenuItemMapClick(MenuItem w) {
        Intent intent = new Intent(RouteListActivity.this, MapActivity.class);
        startActivity(intent);
    }

    public void onMenuItemListClick(MenuItem w) {
        Intent intent = new Intent(RouteListActivity.this, MainActivity.class);
        startActivity(intent);
    }

    public void onMenuItemSendClick(MenuItem w) {
        message = Toast.makeText(this, "Wysle dane", Toast.LENGTH_SHORT);
        message.show();
    }

    public void testClick(MenuItem w) {
        Intent intent = new Intent(RouteListActivity.this, ExampleActivity.class);
        startActivity(intent);
    }
}
