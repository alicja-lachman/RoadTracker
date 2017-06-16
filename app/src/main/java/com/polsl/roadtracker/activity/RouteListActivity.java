package com.polsl.roadtracker.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
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

import com.google.gson.Gson;
import com.polsl.roadtracker.R;
import com.polsl.roadtracker.adapter.RouteListAdapter;
import com.polsl.roadtracker.api.RoadtrackerService;
import com.polsl.roadtracker.api.RoutePartData;
import com.polsl.roadtracker.dagger.di.component.DaggerDatabaseComponent;
import com.polsl.roadtracker.dagger.di.component.DatabaseComponent;
import com.polsl.roadtracker.dagger.di.module.DatabaseModule;
import com.polsl.roadtracker.database.UploadStatus;
import com.polsl.roadtracker.database.entity.RouteData;
import com.polsl.roadtracker.database.entity.RouteDataDao;
import com.polsl.roadtracker.model.ApiResult;
import com.polsl.roadtracker.model.LogoutData;
import com.polsl.roadtracker.util.Constants;
import com.polsl.roadtracker.util.FileHelper;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;


public class RouteListActivity extends AppCompatActivity {


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
    private DatabaseComponent databaseComponent;
    private Toast message;
    private RoadtrackerService apiService;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Observable<RouteData> routeObservable;
    private Observer<RouteData> routeObserver;
    private ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_list);

        ButterKnife.bind(this);
        prepareNavigationDrawer();
        apiService = new RoadtrackerService();

        tracks = routeDataDao.loadAll();
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
                data.update();
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
        ArrayList<RouteData> routesToSend = new ArrayList<>();
        for (RouteData d : tracks) {
            if (d.isSetToSend())
                routesToSend.add(d);
        }
        if (routesToSend.isEmpty()) {
            message = Toast.makeText(this, R.string.no_selected_routes, Toast.LENGTH_SHORT);
            message.show();
            return;
        }
        sendRoute(routesToSend);
    }

    public void sendRoute(List<RouteData> routeDatas) {
        statusTv.setText("Sending " + routeDatas.size() + " routes");
        progressDialog = ProgressDialog.show(RouteListActivity.this, "Please wait",
                "Sending " + routeDatas.size() + " routes", true);
        routeObservable = Observable.create((ObservableOnSubscribe<RouteData>) e -> {
                    for (RouteData routeData : routeDatas)
                        e.onNext(routeData);
                    e.onComplete();
                }


        )
                .flatMap(routes -> Observable.just(routes))
                .subscribeOn(Schedulers.io());

        routeObserver = new Observer<RouteData>() {

            @Override
            public void onError(Throwable e) {
                Timber.d("Route on error " + e.getMessage());
            }

            @Override
            public void onComplete() {
                runOnUiThread(() -> {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    statusTv.setText("Finished sending");
                });

            }

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(RouteData routeData) {
                routeData.fetchAllData();
                String json = new Gson().toJson(routeData);
                try {
                    String filePath = FileHelper.saveRouteToFile(json, routeData.getId(), RouteListActivity.this);
                    File file = new File(getExternalFilesDir(null), filePath);
                    Uri uri = Uri.fromFile(file);
                    File routeFile = new File(uri.getPath());
                    ArrayList<String> zipPaths = createSplitZipFile(routeFile, routeData.getId());
                    String authToken = getSharedPreferences(getPackageName(),
                            Context.MODE_PRIVATE)
                            .getString(Constants.AUTH_TOKEN, null);

                    for (int i = 0; i < zipPaths.size(); i++) {
                        int packageNumber = i + 1;
                        boolean isLast = (packageNumber == zipPaths.size());
                        String data = FileHelper.convertFileToString(zipPaths.get(i));
                        RoutePartData routePartData = new RoutePartData(authToken, String.valueOf(packageNumber), data, isLast);
                        apiService.sendRoutePartData(routePartData, basicResponse -> {
                            runOnUiThread(() -> {
                                Timber.d("Result of sending: " + basicResponse.getResult());
                                if (basicResponse.getResult().equals(ApiResult.RESULT_OK.getInfo()))
                                    statusTv.setText("Route " + routeData.getId() + " was sent successfully");
                                else
                                    statusTv.setText("An error occured while sending route data!");
                            });
                        });
                    }

                } catch (Exception e) {
                    Timber.e(e.getMessage());
                }
                Timber.d("About to delete files...");
                // FileHelper.deleteResultFiles(RouteListActivity.this);
                routeData.setSetToSend(false);
                routeData.setUploadStatus(UploadStatus.UPLOADED);
                routeData.update();
                runOnUiThread(() -> tAdapter.notifyDataSetChanged());

            }

        };

        routeObservable.subscribe(routeObserver);

    }


    public ArrayList<String> createSplitZipFile(File file, long id) {
        final int MAX_ZIP_SIZE = 10 * 1000 * 1024; //10MB max size
        try {
            File externalFilesDir = getExternalFilesDir(null);
            String path = externalFilesDir.getAbsolutePath() + "/routes/result" + id + ".zip";
            ZipFile zipFile = new ZipFile(path);

            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
            zipFile.createZipFile(file, parameters, true, MAX_ZIP_SIZE); //100kB
            return zipFile.getSplitZipFiles();
        } catch (ZipException e) {
            e.printStackTrace();
        }
        return null;
    }

    //TODO match with actual activity(currently: matching with MainActivity)

    public void onMenuItemListClick(MenuItem w) {
        Intent intent = new Intent(RouteListActivity.this, MainActivity.class);
        startActivity(intent);
    }

    public void onMenuItemLogoutClick(MenuItem w) {
        SharedPreferences preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        String authToken = preferences.getString(Constants.AUTH_TOKEN, null);
        preferences.edit().putString(Constants.AUTH_TOKEN, null).apply();
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
