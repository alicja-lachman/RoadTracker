package com.polsl.roadtracker.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.polsl.roadtracker.R;
import com.polsl.roadtracker.adapter.RouteListAdapter;
import com.polsl.roadtracker.api.BasicResponse;
import com.polsl.roadtracker.api.RoadtrackerService;
import com.polsl.roadtracker.api.RoutePartData;
import com.polsl.roadtracker.database.RoadtrackerDatabaseHelper;
import com.polsl.roadtracker.database.entity.DaoSession;
import com.polsl.roadtracker.database.entity.DatabaseData;
import com.polsl.roadtracker.database.entity.DatabaseDataDao;
import com.polsl.roadtracker.database.entity.RouteData;
import com.polsl.roadtracker.database.entity.RouteDataDao;
import com.polsl.roadtracker.model.ApiResult;
import com.polsl.roadtracker.model.LogoutData;
import com.polsl.roadtracker.util.Constants;
import com.polsl.roadtracker.util.FileHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;


public class RouteListActivity extends AppCompatActivity {

    DatabaseDataDao databaseDataDao;
    RouteDataDao routeDataDao;
    @BindView(R.id.navigation_view_route_list)
    NavigationView navigationView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.select_all_checkbox)
    CheckBox checkBox;
    @BindView(R.id.status_tv)
    TextView statusTv;

    private List<RouteData> tracks = new ArrayList<>();
    private RouteListAdapter tAdapter;
    private RecyclerView routeListView;
    private Toast message;
    private RoadtrackerService apiService;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private ProgressDialog progressDialog;
    private ArrayList<RouteData> routesToSend;
    private int sendingRoutesCounter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_list);
        ButterKnife.bind(this);

        prepareRoutes();

    }

    private void prepareRoutes() {
        
        databaseDataDao = RoadtrackerDatabaseHelper.getMainDaoSession().getDatabaseDataDao();
        prepareNavigationDrawer();
        apiService = new RoadtrackerService(this);
        List<DatabaseData> databases = databaseDataDao.loadAll();
        for (DatabaseData data : databases) {
            RoadtrackerDatabaseHelper.initialiseDbForRide(this, data.getDatabaseName());
            DaoSession daoSession = RoadtrackerDatabaseHelper.getDaoSessionForDb(data.getDatabaseName());
            routeDataDao = daoSession.getRouteDataDao();
            tracks.addAll(routeDataDao.loadAll());
        }
        for (int i = tracks.size() - 1; i >= 0; i--) {
            if (tracks.get(i).getEndDate() == null) {
                tracks.remove(i);
            }
        }
        routeListView = (RecyclerView) findViewById(R.id.route_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        routeListView.setLayoutManager(layoutManager);
        tAdapter = new RouteListAdapter(tracks, RouteListActivity.this);
        routeListView.setAdapter(tAdapter);
        routeListView.invalidate();
        int count = 0;
        for (RouteData data : tracks) {
            if (!data.getSetToSend()) {
                checkBox.setChecked(false);
                break;
            } else
                count++;
        }
        if (count == tracks.size())
            checkBox.setChecked(true);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (RouteData data : tracks) {
                data.setSetToSend(isChecked);
// && !(data.getUploadStatus() == UploadStatus.UPLOADED));
            }
            tAdapter.notifyDataSetChanged();
        });
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        int clickedItemInd = item.getItemId();
        switch (clickedItemInd) {
            //Info icon
            case R.id.btn_info:
                Toast.makeText(this, R.string.list_info, Toast.LENGTH_LONG).show();
                break;
            //Back icon
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
        }
        return true;
    }

    public void onSendButtonClick(View v) {
        runOnUiThread(() -> progressDialog = ProgressDialog.show(RouteListActivity.this, "Please wait",
                "Sending routes", true));
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                handleSendingRoutes()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe();
            }
        }, 500);

    }

    private Observable<Object> handleSendingRoutes() {
        routesToSend = new ArrayList<>();
        sendingRoutesCounter = 0;
        for (RouteData d : tracks) {
            if (d.isSetToSend())
                routesToSend.add(d);
        }
        if (routesToSend.isEmpty()) {
            message = Toast.makeText(this, R.string.no_selected_routes, Toast.LENGTH_SHORT);
            message.show();

        }
        statusTv.setText("Sending " + routesToSend.size() + " routes");

        Timber.d("routes to send: " + routesToSend.size());
        sendRoute(routesToSend.get(0));
        return Observable.just("");
    }


    public void sendRoute(RouteData routeDatas) {

        RouteData routeData = routeDatas;

        int i = 0;

        String currentDBPath = "/data/data/" + getPackageName() + "/databases/" + routeData.getDbName();
        File dbFile = new File(currentDBPath);
        try {
            List<String> zipPaths = FileHelper.splitFile(dbFile.getPath());
            Timber.d("Parts to send: " + zipPaths.size());
            String authToken = getSharedPreferences(getPackageName(),
                    Context.MODE_PRIVATE)
                    .getString(Constants.AUTH_TOKEN, null);

            sending(routeData, i, zipPaths, authToken);

        } catch (IOException e) {
            Timber.e("IO exception " + e.getMessage());
        }
    }


    private void sending(RouteData routeData, int i, List<String> zipPaths, String authToken) throws IOException {
        runOnUiThread(() -> statusTv.setText("Sending part: " + i + " of route: " + routeData.getDescription()));
        Timber.d("sending part no: " + i);
        if (i == zipPaths.size()) {
            handleAllDataSent();
            return;
        }
        sendRoutePart(i, zipPaths.get(i), zipPaths.size(), authToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(basicResponse -> {
                    if (basicResponse.getResult().equals(ApiResult.RESULT_OK.getInfo())) {
                        sending(routeData, i + 1, zipPaths, authToken);
                    } else
                        handleSendingError(basicResponse.getReason(), routeData.getDescription());

                }, throwable -> {
                    Timber.e("Error " + throwable.getMessage());
                    handleSendingError(throwable.getMessage(), routeData.getDescription());
                });
    }

    private void handleSendingError(String error, String routeDataDesc) {
        Timber.e("Error while sending " + routeDataDesc);
        if (error != null)
            Timber.e("Error reason: " + error);
        runOnUiThread(() -> {
            statusTv.setText("There was an error while sending " + routeDataDesc);
            if (progressDialog.isShowing())
                progressDialog.dismiss();
        });

    }

    private void handleAllDataSent() {
        Timber.d("Handle all data sent");
        statusTv.setText("Route  was sent successfully");
        // routeData.setSetToSend(false);
        // routeData.setUploadStatus(UploadStatus.UPLOADED);
        //  RouteDataDao routeDataDao = RoadtrackerDatabaseHelper.getDaoSessionForDb(routeData.getDbName()).getRouteDataDao();
        //  routeDataDao.update(routeData);
        sendingRoutesCounter++;
        if (sendingRoutesCounter < routesToSend.size()) {
            sendRoute(routesToSend.get(sendingRoutesCounter));
        } else
            runOnUiThread(() -> {
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
                statusTv.setText("Finished sending");
            });


    }

    private Observable<BasicResponse> sendRoutePart(int i, String s, int size, String authToken) throws IOException {
        int packageNumber = i + 1;
        boolean isLast = (packageNumber == size);
        Timber.d("calling api service");
        RoutePartData routePartData = new RoutePartData(authToken, String.valueOf(packageNumber), FileHelper.convertFileToString(s), isLast);
        return apiService.sendRoutePartData(routePartData);
    }

    public void onMenuItemMainClick(MenuItem w) {
        Intent intent = new Intent(RouteListActivity.this, MainActivity.class);
        startActivity(intent);
    }

    public void onMenuItemLogoutClick(MenuItem w) {
        SharedPreferences preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        String authToken = preferences.getString(Constants.AUTH_TOKEN, null);
        preferences.edit().putString(Constants.AUTH_TOKEN, null).apply();
        preferences.edit().putString(Constants.URL, null).apply();
         apiService.logout(new LogoutData(authToken), basicResponse -> {
        Intent intent = new Intent(RouteListActivity.this, LoginActivity.class);
        startActivity(intent);
         });
    }

    public void onMenuItemSettingsClick(MenuItem item) {
        Intent intent = new Intent(RouteListActivity.this, SettingsActivity.class);
        startActivity(intent);
    }
}
