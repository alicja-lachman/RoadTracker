package com.polsl.roadtracker.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.polsl.roadtracker.R;
import com.polsl.roadtracker.adapter.SendingListAdapter;
import com.polsl.roadtracker.api.RoadtrackerService;
import com.polsl.roadtracker.dagger.di.component.DaggerDatabaseComponent;
import com.polsl.roadtracker.dagger.di.component.DatabaseComponent;
import com.polsl.roadtracker.dagger.di.module.DatabaseModule;
import com.polsl.roadtracker.database.entity.RouteData;
import com.polsl.roadtracker.database.entity.RouteDataDao;
import com.polsl.roadtracker.model.LogoutData;
import com.polsl.roadtracker.util.Constants;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SendingActivity extends AppCompatActivity {
    @Inject
    RouteDataDao routeDataDao;
    @BindView(R.id.navigation_view_sending_list)
    NavigationView navigationView;
    @BindView(R.id.drawer_sending)
    DrawerLayout drawerLayout;

    @BindView(R.id.sending_recycler)
    RecyclerView recyclerView;
    @BindView(R.id.status_tv)
    TextView statusTv;

    private Intent intent;
    private DatabaseComponent databaseComponent;
    private RoadtrackerService apiService;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private SendingListAdapter adapter;
    private List<RouteData> routes = new ArrayList<>();
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sending);
        injectDependencies();
        ButterKnife.bind(this);
        prepareNavigationDrawer();
        prepareRouteDatas();
        prepareRecyclerView();


    }

    private void prepareRouteDatas() {
        intent = getIntent();
        ArrayList<Long> ids;
        ids = (ArrayList<Long>) intent.getExtras().get("IDs");

        for (long id : ids) {
            routes.add(routeDataDao.load(id));
        }

    }

    private void injectDependencies() {
        databaseComponent = DaggerDatabaseComponent.builder()
                .databaseModule(new DatabaseModule())
                .build();
        databaseComponent.inject(this);
    }

    private void prepareRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new SendingListAdapter(routes, SendingActivity.this);
        recyclerView.setAdapter(adapter);
        recyclerView.invalidate();
    }

    public void sendRoute(RouteData routeData) {
        statusTv.setText("Sending route");
        progressDialog = ProgressDialog.show(this, "Please wait",
                "Sending route " + routeData.getId(), true);


    }

    private void prepareNavigationDrawer() {
        actionBarDrawerToggle = new ActionBarDrawerToggle(this,
                drawerLayout, R.string.app_name, R.string.app_name) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        actionBarDrawerToggle.syncState();

    }

    public void onMenuItemLogoutClick(MenuItem w) {
        SharedPreferences preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        String authToken = preferences.getString(Constants.AUTH_TOKEN, null);
        preferences.edit().putString(Constants.AUTH_TOKEN, null).apply();
        apiService.logout(new LogoutData(authToken), basicResponse -> {
            Intent intent = new Intent(SendingActivity.this, LoginActivity.class);
            startActivity(intent);
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
