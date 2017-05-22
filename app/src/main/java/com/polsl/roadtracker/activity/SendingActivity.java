package com.polsl.roadtracker.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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

import com.google.gson.Gson;
import com.polsl.roadtracker.R;
import com.polsl.roadtracker.adapter.SendingListAdapter;
import com.polsl.roadtracker.api.RoadtrackerService;
import com.polsl.roadtracker.api.RoutePartData;
import com.polsl.roadtracker.dagger.di.component.DaggerDatabaseComponent;
import com.polsl.roadtracker.dagger.di.component.DatabaseComponent;
import com.polsl.roadtracker.dagger.di.module.DatabaseModule;
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

    private Observable<RouteData> routeObservable;
    private Observer<RouteData> routeObserver;

    private Intent intent;
    private DatabaseComponent databaseComponent;
    private RoadtrackerService apiService;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private SendingListAdapter adapter;
    private List<RouteData> routes = new ArrayList<>();
    private ProgressDialog progressDialog;
    private String STORE_DIRECTORY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sending);
        injectDependencies();
        ButterKnife.bind(this);
        prepareNavigationDrawer();
        prepareRouteDatas();
        prepareRecyclerView();
        apiService = new RoadtrackerService();


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
        progressDialog = ProgressDialog.show(SendingActivity.this, "Please wait",
                "Sending route " + routeData.getId(), true);
        routeObservable = Observable.create((ObservableOnSubscribe<RouteData>) e -> {
                    e.onNext(routeData);
                }
        )
                .flatMap(bitmaps -> Observable.just(bitmaps))
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
                });

            }

            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(RouteData routeData) {
                String json = new Gson().toJson(routeData);
                try {
                    String filePath = FileHelper.saveRouteToFile(json, routeData.getId(), SendingActivity.this);
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
                                if (basicResponse.getResult().equals(ApiResult.RESULT_OK.getInfo()))
                                    statusTv.setText("Route " + routeData.getId() + " was sent succesffully");
                                else
                                    statusTv.setText("An error occured while sending route data!");
                            });
                        });
                    }

                } catch (Exception e) {
                    Timber.e(e.getMessage());
                }
                Timber.d("About to delete files...");
                //   FileHelper.deleteResultFiles(SendingActivity.this);
                routeData.setSetToSend(false);
                runOnUiThread(() -> adapter.notifyDataSetChanged());
                onComplete();
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
