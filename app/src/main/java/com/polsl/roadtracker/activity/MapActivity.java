package com.polsl.roadtracker.activity;

import android.content.Context;
import android.content.DialogInterface;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.polsl.roadtracker.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.polsl.roadtracker.dagger.di.component.DaggerDatabaseComponent;
import com.polsl.roadtracker.dagger.di.component.DatabaseComponent;
import com.polsl.roadtracker.dagger.di.module.DatabaseModule;
import com.polsl.roadtracker.database.entity.LocationData;
import com.polsl.roadtracker.database.entity.RouteDataDao;
import com.polsl.roadtracker.utility.PositionInfo;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, SeekBar.OnSeekBarChangeListener {
//TODO: get routeID from intent, get points from db and show on map
    @Inject
    RouteDataDao routeDataDao;
    @BindView(R.id.sb_change_range)
    SeekBar rangeBar;
    @BindView(R.id.tv_closest_time)
    TextView timeTextView;

    private DatabaseComponent databaseComponent;
    private Polyline path;
    private Polyline newPath;
    private LatLngBounds.Builder builder;
    private GoogleMap mMap;
    private List<PositionInfo> places;
    private int firstIndex, lastIndex;
    private int firstProgress, lastProgress;
    private List<Marker> markersList;
    private Boolean firstMarkerChosen = null;
    private boolean editMode = false;

    private void injectDependencies() {
        databaseComponent = DaggerDatabaseComponent.builder()
                .databaseModule(new DatabaseModule())
                .build();
        databaseComponent.inject(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Timber.plant(new Timber.DebugTree());
        ButterKnife.bind(this);
        injectDependencies();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int clickedItemInd = item.getItemId();
        switch (clickedItemInd) {
            //Info icon
            case R.id.btn_info:
                Toast.makeText(this, R.string.usage_information, Toast.LENGTH_LONG).show();
                break;
            //Back icon
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
        }
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        rangeBar.setOnSeekBarChangeListener(this);
        places = new ArrayList<>();
        setPlaces();
        setUpMap();
        setStopStartTime();
        rangeBar.setEnabled(false);
    }


    public void setUpMap() {
        //clear the map before redraw to them
        mMap.clear();
        //Place markers
        markersList = getMarkers(places);
        //Create path
        path = mMap.addPolyline(createPath(markersList, 0, markersList.size() - 1, ContextCompat.getColor(this, R.color.colorOldPath)));
        //TODO: Change the tag to style the line
        path.setTag("TAG");
        zoomCamera();
    }

    private void zoomCamera() {
        //create for loop for get the LatLngbuilder from the marker list
        builder = new LatLngBounds.Builder();
        for (Marker m : markersList) {
            builder.include(m.getPosition());
        }
        //initialize the padding for map boundary
        int padding = 150;
        //create the bounds from latlngBuilder to set into map camera
        LatLngBounds bounds = builder.build();
        //create the camera with bounds and padding to set into map
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        //call the map call back to know map is loaded or not
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                /**set animated zoom camera into map*/
                mMap.animateCamera(cameraUpdate);
            }
        });
    }

    private void setPlaces() {
        //Get route id
        Intent intent = getIntent();
        long id = intent.getLongExtra("ROUTE_ID", 0L);
        //intent.putExtra("ROUTE_ID", tracks.get(position).getId());
        //Get locations from database
        List<LocationData> locationData = routeDataDao.load(id).getLocationDataList();

        //Build list of positions
        for (int i = 0; i < locationData.size(); i++) {
            LocationData data = locationData.get(i);
            LatLng position = new LatLng(data.getLatitude(), data.getLongitude());
            Timestamp time = new Timestamp(data.getTimestamp());
            places.add(new PositionInfo(position, time));
        }
//        //Set up start values of path
//        LatLng[] coords = {
//                new LatLng(50.288584, 18.678540),
//                new LatLng(50.287687, 18.677402),
//                new LatLng(50.287338, 18.677356),
//                new LatLng(50.286446, 18.679541),
//                new LatLng(50.284900, 18.677986),
//                new LatLng(50.286131, 18.675300),
//        };
//        Timestamp[] times = {
//                new Timestamp(10000),
//                new Timestamp(20000),
//                new Timestamp(30000),
//                new Timestamp(40000),
//                new Timestamp(50000),
//                new Timestamp(60000),
//        };
//        //Build a test values hashmap
//        for (int i = 0; i < coords.length; i++) {
//            places.add(new PositionInfo(coords[i], times[i]));
//        }
        firstIndex = 0;
        firstProgress = 0;
        lastIndex = places.size() - 1;
        lastProgress = 100;
    }

    private List<Marker> getMarkers(List<PositionInfo> places) {
        //Make date format object
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        //Create dummy Markers List
        List<Marker> markers = new ArrayList<>();
        for (PositionInfo point : places) {
            //Add a marker
            Marker marker = mMap.addMarker(new MarkerOptions().position(point.getCooridinate())
                    .title(dateFormat.format(point.getDate())));
            markers.add(marker);
        }
        return markers;
    }


    private PolylineOptions createPath(List<Marker> markers, int first, int last, int color) {

        //Create options containing points of the line
        PolylineOptions polylineOptions = new PolylineOptions().clickable(false);
        for (int i = first; i <= last; i++) {
            //Add a point to line
            polylineOptions.add(markers.get(i).getPosition());
            polylineOptions.color(color);
        }
        polylineOptions.width(20.0f);
        //Set first and last marker visible
        showProperMarkers(markers, first, last);
        //Create path
        return polylineOptions;
    }


    private void showProperMarkers(List<Marker> markers, int first, int last) {
        //if the list is empty
        if (markers.size() == 0)
            return;
        //set all markers invisible
        for (Marker m : markers) {
            m.setVisible(false);
        }
        if (!editMode) {
            markers.get(first).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            markers.get(last).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }
        //make first and last visible
        markers.get(first).setVisible(true);
        markers.get(last).setVisible(true);

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (!editMode) {
            editMode = true;
        }
        //If its the first marker
        if (marker.getPosition().equals(places.get(firstIndex).getCooridinate())) {
            firstMarkerChosen = true;
            rangeBar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorSeekLastMarkerChosen));
            rangeBar.setProgress(firstProgress);
        }
        //If its the last marker
        else if (marker.getPosition().equals(places.get(lastIndex).getCooridinate())) {
            firstMarkerChosen = false;
            rangeBar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorSeekFirstMarkerChosen));
            rangeBar.setProgress(lastProgress);
        }

        rangeBar.setEnabled(true);
        return true;
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (editMode) {
            int closestIndex;
            //If start point is being changed
            if (firstMarkerChosen) {
                //Choose index from proper range
                closestIndex = (int) ((double) i / 100.0 * (lastIndex - 1));
                //Choose first point
                firstIndex = closestIndex;
                firstProgress = i;
            }
            //If finish point is being changed
            else {
                //Choose index from proper range
                closestIndex = (int) ((double) i / 100.0 * (markersList.size() - 2 - firstIndex)) + firstIndex + 1;
                //Choose finish point
                lastIndex = closestIndex;
                lastProgress = i;
            }
            //Draw proper new change path
            Polyline path = mMap.addPolyline(createPath(markersList, firstIndex, lastIndex, ContextCompat.getColor(this, R.color.colorNewPath)));
            //Remove old path revealing the new one
            if (newPath != null)
                newPath.remove();
            //save edit path
            newPath = path;
            setStopStartTime();
        }
    }

    @OnClick(R.id.btn_cut)
    public void onCutClick(View v) {
        //If editing path is performed
        if (editMode) {
            createConfirmDialog();
        } else {
            Toast toast = Toast.makeText(this, R.string.no_edit_mode_cut, Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private void setStopStartTime() {
        timeTextView.setText("Start at " + markersList.get(firstIndex).getTitle() +
                "\n" + "Finish at " + markersList.get(lastIndex).getTitle());
    }

    private void stopPathEditing() {
        //Reset first and last point index
        firstIndex = 0;
        firstProgress = 0;
        lastIndex = markersList.size() - 1;
        lastProgress = 100;

        //Remove signs of editing path
        newPath.remove();

        //go out of editMode and disable rangeBar
        editMode = false;
        rangeBar.setProgress(0);
        rangeBar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorSeekDisabled));
        firstMarkerChosen = null;
        rangeBar.setEnabled(false);
    }

    @OnClick(R.id.btn_cancel)
    public void onCancelClick(View v) {
        if (editMode) {
            stopPathEditing();
            setStopStartTime();
            //Show old path
            showProperMarkers(markersList, firstIndex, lastIndex);
        }
    }

    private void createConfirmDialog() {
        Context context = this;
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_cut)
                .setMessage(R.string.confirm_cut_description)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Select proper places of the new path
                        List<PositionInfo> newPlaces = new ArrayList<>();
                        for (int j = firstIndex; j <= lastIndex; j++) {
                            newPlaces.add(places.get(j));
                        }
                        places = newPlaces;

                        //Draw a new solid path
                        Polyline newSolidPath = mMap.addPolyline(createPath(markersList, firstIndex, lastIndex, ContextCompat.getColor(context, R.color.colorOldPath)));
                        path.remove();
                        path = newSolidPath;

                        //Select proper markers of the new path
                        List<Marker> newMarkers = new ArrayList<>();
                        for (int j = firstIndex; j <= lastIndex; j++) {
                            newMarkers.add(markersList.get(j));
                        }
                        markersList = newMarkers;

                        stopPathEditing();

                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}
