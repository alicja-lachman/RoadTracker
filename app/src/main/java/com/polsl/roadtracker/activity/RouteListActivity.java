package com.polsl.roadtracker.activity;

import android.Manifest;
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

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.polsl.roadtracker.R;
import com.polsl.roadtracker.adapter.RouteListAdapter;
import com.polsl.roadtracker.api.BasicResponse;
import com.polsl.roadtracker.api.RoadtrackerService;
import com.polsl.roadtracker.api.RoutePartData;
import com.polsl.roadtracker.database.RoadtrackerDatabaseHelper;
import com.polsl.roadtracker.database.UploadStatus;
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

/**
 * Activity responsible for displaying list of saved routes.
 */
public class RouteListActivity extends AppCompatActivity {
    /**
     * DAO for retrieving all databases.
     */
    DatabaseDataDao databaseDataDao;
    /**
     * DAO for retrieving route from given database.
     */
    RouteDataDao routeDataDao;
    @BindView(R.id.navigation_view_route_list)
    NavigationView navigationView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.select_all_checkbox)
    CheckBox checkBox;
    @BindView(R.id.status_tv)
    TextView statusTv;
    /**
     * List of all saved routes.
     */
    private List<RouteData> tracks = new ArrayList<>();
    /**
     * Adapter used for recycler view with routes list.
     */
    private RouteListAdapter tAdapter;
    /**
     * Recycler view used for displaying routes list.
     */
    private RecyclerView routeListView;
    /**
     * Toast message displayed to user.
     */
    private Toast message;
    /**
     * instance of RoadtrackerService, used to communicate with server.
     */
    private RoadtrackerService apiService;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private ProgressDialog progressDialog;
    /**
     * List of routes selected to be sent.
     */
    private ArrayList<RouteData> routesToSend;
    /**
     * Counter used for counting routes to be sent.
     */
    private int sendingRoutesCounter;

    /**
     * Lifecycle method invoked after creating activity, responsible for preparing the view and setup.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_list);
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        RoadtrackerDatabaseHelper.initialise(RouteListActivity.this);
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
                })
                .check();

        ButterKnife.bind(this);
        prepareRoutes();
    }

    /**
     * Method used for retrieving all routes from all databases and displaying them in recyclier view.
     */
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
                data.setSetToSend(isChecked && !(data.getUploadStatus() == UploadStatus.UPLOADED));
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

    /**
     * Method invoked after clicking on "Send" button, it starts sending routes to server.
     *
     * @param v
     */
    public void onSendButtonClick(View v) {
        runOnUiThread(() -> progressDialog = ProgressDialog.show(RouteListActivity.this, "Please wait",
                "Sending routes", true));
        Handler handler = new Handler();
        handler.postDelayed(() -> handleSendingRoutes()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(), 500);

    }

    /**
     * MMethod handling preparing routesToSend list.
     *
     * @return
     */
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

    /**
     * Method used for dividing database file into parts.
     *
     * @param routeDatas
     */
    public void sendRoute(RouteData routeDatas) {

        RouteData routeData = routeDatas;

        int i = 0;

        String currentDBPath = "/data/data/" + getPackageName() + "/databases/" + routeData.getDbName();
        File dbFile = new File(currentDBPath);
        try {
            List<String> filePaths = FileHelper.splitFile(dbFile.getPath());
            Timber.d("Parts to send: " + filePaths.size());
            String authToken = getSharedPreferences(getPackageName(),
                    Context.MODE_PRIVATE)
                    .getString(Constants.AUTH_TOKEN, null);

            sending(routeData, i, filePaths, authToken);

        } catch (IOException e) {
            Timber.e("IO exception " + e.getMessage());
        }
    }

    /**
     * Method used for sending database file parts to server.
     *
     * @param routeData
     * @param i
     * @param zipPaths
     * @param authToken
     * @throws IOException
     */
    private void sending(RouteData routeData, int i, List<String> zipPaths, String authToken) throws IOException {
        runOnUiThread(() -> statusTv.setText("Sending part: " + i + " of route: " + routeData.getDescription()));
        Timber.d("sending part no: " + i);
        if (i == zipPaths.size()) {
            handleAllDataSent(routeData);
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

    /**
     * Method handling errors that may occur during sending database file to server.
     *
     * @param error
     * @param routeDataDesc
     */
    private void handleSendingError(String error, String routeDataDesc) {
        Timber.e("Error while sending " + routeDataDesc);
        if (error != null)
            Timber.e("Error reason: " + error);
        runOnUiThread(() -> {
            if (error != null)
                statusTv.setText("There was an error while sending " + routeDataDesc + ": " + error);
            else
                statusTv.setText("There was an error while sending " + routeDataDesc);
            if (progressDialog.isShowing())
                progressDialog.dismiss();
        });

    }

    /**
     * Method invoked when all database parts were sent successfully.
     *
     * @param routeData
     */
    private void handleAllDataSent(RouteData routeData) {
        Timber.d("Handle all data sent");
        statusTv.setText("Route  was sent successfully");
        routeData.setSetToSend(false);
        routeData.setUploadStatus(UploadStatus.UPLOADED);
        RouteDataDao routeDataDao = RoadtrackerDatabaseHelper.getDaoSessionForDb(routeData.getDbName()).getRouteDataDao();
        routeDataDao.delete(routeData);
        RoadtrackerDatabaseHelper.deleteDatabase(this, routeData.getDbName());
        tracks.remove(routeData);
        tAdapter.notifyDataSetChanged();
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

    /**
     * Method sending database part to server.
     *
     * @param i
     * @param s
     * @param size
     * @param authToken
     * @return
     * @throws IOException
     */
    private Observable<BasicResponse> sendRoutePart(int i, String s, int size, String authToken) throws IOException {
        int packageNumber = i + 1;
        boolean isLast = (packageNumber == size);
        Timber.d("calling api service");
        RoutePartData routePartData = new RoutePartData(authToken, String.valueOf(packageNumber), FileHelper.convertFileToString(s), isLast);
        return apiService.sendRoutePartData(routePartData);
    }

    /**
     * Method used for going to MainActivity.
     *
     * @param w
     */
    public void onMenuItemMainClick(MenuItem w) {
        Intent intent = new Intent(RouteListActivity.this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * Method used for logging out.
     *
     * @param w
     */
    public void onMenuItemLogoutClick(MenuItem w) {
        SharedPreferences preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        String authToken = preferences.getString(Constants.AUTH_TOKEN, null);
        preferences.edit().putString(Constants.AUTH_TOKEN, null).apply();
        preferences.edit().putString(Constants.URL, null).apply();
        apiService.logout(new LogoutData(authToken), basicResponse -> {
        });
        Intent intent = new Intent(RouteListActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    /**
     * Method used for going to SettingsActivity.
     *
     * @param item
     */
    public void onMenuItemSettingsClick(MenuItem item) {
        Intent intent = new Intent(RouteListActivity.this, SettingsActivity.class);
        startActivity(intent);
    }
}
