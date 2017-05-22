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
import com.polsl.roadtracker.util.Base64Encoder;
import com.polsl.roadtracker.util.Constants;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
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
        progressDialog = ProgressDialog.show(this, "Please wait",
                "Sending route " + routeData.getId(), true);
        String json = new Gson().toJson(routeData);
        json = Base64Encoder.encodeData(json);
        //  String file = FileHelper.saveRouteToFile(json, routeData.getId(), this);
        File externalFilesDir = getExternalFilesDir(null);
        if (externalFilesDir != null) {
            STORE_DIRECTORY = externalFilesDir.getAbsolutePath() + "/routes/";
            //   zip(file, STORE_DIRECTORY + "route.zip");
        }

     //   zipMethod("example.json");

        String authToken = getSharedPreferences(getPackageName(),
                Context.MODE_PRIVATE)
                .getString(Constants.AUTH_TOKEN, null);
        RoutePartData routePartData = new RoutePartData(authToken, "1", json, true);
        apiService.sendRoutePartData(routePartData, basicResponse -> {
            progressDialog.dismiss();
            if (basicResponse.getResult().equals(ApiResult.RESULT_OK.getInfo()))
                statusTv.setText("Route was sent succesffully");
            else
                statusTv.setText("An error occured while sending route data!");
        });


    }


    final long MAX_LIMIT = 10 * 100 * 1024; //10MB limit - hopefully this


    private void zipMethod(String thisFileName) {

        try {
            int i = 0;
            boolean needNewFile = false;
            long overallSize = 0;
            ZipOutputStream out = getOutputStream(i);
            byte[] buffer = new byte[1024];


            if (overallSize > MAX_LIMIT) {
                out.close();
                i++;
                out = getOutputStream(i);
                overallSize = 0;
            }
            InputStream ipp = getAssets().open(thisFileName);
            // FileInputStream in = new FileInputStream(new File("//android_asset/" + thisFileName));
            ZipEntry ze = new ZipEntry(thisFileName);
            out.putNextEntry(ze);
            int len;
            while ((len = ipp.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            out.closeEntry();
            ipp.close();
            overallSize += ze.getCompressedSize();
            Timber.d("Overall size: " + overallSize);
            Timber.d("Compressed size: " + ze.getCompressedSize());

            out.close();
        } catch (Exception e) {
            Timber.e("Exception while zipping: " + e.getMessage());
        }
    }

    public ZipOutputStream getOutputStream(int i) throws IOException {
        File externalFilesDir = getExternalFilesDir(null);
        String path = externalFilesDir.getAbsolutePath() + "/";
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(path + "bigfile" + i + ".zip"));
        out.setLevel(Deflater.DEFAULT_COMPRESSION);
        return out;
    }

    public void zip(String file, String zipFile) {


        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(zipFile);

            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            byte data[] = new byte[10000];

            FileInputStream fi = new FileInputStream(file);
            origin = new BufferedInputStream(fi, 10000);
            ZipEntry entry = new ZipEntry(file.substring(file.lastIndexOf("/") + 1));
            out.putNextEntry(entry);
            int count;
            while ((count = origin.read(data, 0, 10000)) != -1) {
                out.write(data, 0, count);
            }
            origin.close();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
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
