package com.polsl.roadtracker.activity;

import android.content.Context;
import android.content.DialogInterface;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.polsl.roadtracker.utility.MapComparer;
import com.polsl.roadtracker.utility.PositionInfo;

import org.w3c.dom.Text;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, SeekBar.OnSeekBarChangeListener, GoogleMap.OnPolylineClickListener {
    //TODO: get routeID from intent, get points from db and show on map
    @Inject
    RouteDataDao routeDataDao;
    @BindView(R.id.sb_change_range)
    SeekBar rangeBar;
    @BindView(R.id.tv_seek_bar_start)
    TextView startValue;
    @BindView(R.id.tv_seek_bar_finish)
    TextView finishValue;
    @BindView(R.id.btn_plus)
    Button plusButton;
    @BindView(R.id.btn_minus)
    Button minusButton;
    @BindView(R.id.btn_cut_beginning)
    Button cutBeginningButton;
    @BindView(R.id.btn_cut_ending)
    Button cutEndingButton;
    @BindView(R.id.btn_cancel)
    Button cancelButton;
    @BindView(R.id.btn_confirm)
    Button confirmButton;

    private DatabaseComponent databaseComponent;
    private Polyline path;
    private Polyline newPath;
    private LatLngBounds.Builder builder;
    private GoogleMap mMap;
    private List<PositionInfo> places;
    private List<PositionInfo> updatedPlaces;
    private List<Marker> editableMarkersList;
    private List<Marker> drawnMarkersList;
    private boolean editMode = false;
    private int visibleMarkersIndex;
    private int firstIndex, lastIndex;
    private int step;
    private int pathStartIndex, pathEndIndex;
    private int pathStartMarker, pathEndMarker;
    private boolean changed = false;

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
        rangeBar.setOnSeekBarChangeListener(this);
        places = new ArrayList<>();
        rangeBar.setEnabled(false);
        setPlaces();
        setUpMap();
    }

    public void setUpMap() {
        //clear the map before redraw to them
        //mMap.clear();
        mMap.setOnPolylineClickListener(this);
        //Place markers
        editableMarkersList = getMarkers(places, 0, places.size() - 1);
        drawnMarkersList = new ArrayList<>(editableMarkersList);
        //Create path
        path = mMap.addPolyline(createPath(editableMarkersList, ContextCompat.getColor(this, R.color.colorOldPath)));
        firstIndex = 0;
        lastIndex = places.size() - 1;
        step = (lastIndex - firstIndex + 1) / 100;
        if(step==0)
            step=1;
        pathStartIndex = 0;
        pathStartMarker = 0;
        pathEndIndex = places.size() - 1;
        pathEndMarker = editableMarkersList.size() - 1;
        rangeBar.setMax(editableMarkersList.size() - 1);
        zoomCamera();
    }


    @Override
    public void onPolylineClick(Polyline polyline) {
        if (!editMode) {
            visibleMarkersIndex = editableMarkersList.size() / 2;
            rangeBar.setEnabled(true);
            rangeBar.setMax(editableMarkersList.size() - 1);
            rangeBar.setProgress(visibleMarkersIndex);
            Marker visibleMarker = editableMarkersList.get(visibleMarkersIndex);
            visibleMarker.setVisible(true);
            visibleMarker.showInfoWindow();
            changeStartFinishValues();
            newPath = mMap.addPolyline(createPath(editableMarkersList, ContextCompat.getColor(this, R.color.colorNewPath)));
            editMode = true;
        }
    }

    private void zoomCamera() {
        //create for loop for get the LatLngbuilder from the marker list
        builder = new LatLngBounds.Builder();
        for (Marker m : editableMarkersList) {
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

    private boolean setPlaces() {
        //Get route id
        Intent intent = getIntent();
        long id = intent.getLongExtra("ROUTE_ID", 0L);
        //intent.putExtra("ROUTE_ID", tracks.get(position).getId());
        //Get locations from database
        List<LocationData> locationData = routeDataDao.load(id).getLocationDataList();
        if (locationData.isEmpty())
            return false;
        //Build list of positions
        for (int i = 0; i < locationData.size(); i++) {
            LocationData data = locationData.get(i);
            LatLng position = new LatLng(data.getLatitude(), data.getLongitude());
            Timestamp time = new Timestamp(data.getTimestamp());
            places.add(new PositionInfo(position, time));
        }

        firstIndex = 0;
        lastIndex = places.size() - 1;
        return true;
//        for (int i = 0; i < 100000; i++) {
//            places.add(new PositionInfo(new LatLng(0f, 0.0001 * (float) i), new Timestamp(i)));
//        }
//        return true;
    }

    private List<Marker> getMarkers(List<PositionInfo> places, int firstIndex, int lastIndex) {
        //Make date format object
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        //Create dummy Markers List
        List<Marker> markers = new ArrayList<>();
        int interval = (lastIndex - firstIndex - 1) / 100;
        if (interval == 0)
            interval = 1;
        for (int i = firstIndex; i <= lastIndex; i += interval) {
            //Add a marker
            PositionInfo point = places.get(i);
            Marker marker = mMap.addMarker(new MarkerOptions().position(point.getCooridinate())
                    .title(dateFormat.format(point.getDate()))
                    .visible(false)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_cut)));
            markers.add(marker);
        }
        //Add the last marker if its not added
        if (!MapComparer.Compare(markers.get(markers.size() - 1).getPosition(), places.get(lastIndex).getCooridinate())) {
            PositionInfo point = places.get(lastIndex);
            Marker marker = mMap.addMarker(new MarkerOptions().position(point.getCooridinate())
                    .title(dateFormat.format(point.getDate()))
                    .visible(false)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_cut)));
            markers.add(marker);
        }
        return markers;
    }

    private PolylineOptions createPath(List<Marker> markers, int color) {
        //Create options containing points of the line
        PolylineOptions polylineOptions = new PolylineOptions().clickable(true);
        for (Marker m : markers) {
            //Add a point to line
            polylineOptions.add(m.getPosition());
            polylineOptions.color(color);
        }
        polylineOptions.width(20.0f);
        //Create path
        return polylineOptions;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (editMode) {
            editableMarkersList.get(visibleMarkersIndex).setVisible(false);
            visibleMarkersIndex = (int) ((float) i * (float) (editableMarkersList.size() - 1) / seekBar.getMax());
            Marker newVisible = editableMarkersList.get(visibleMarkersIndex);
            newVisible.setVisible(true);
            newVisible.showInfoWindow();
            changeStartFinishValues();
        }
    }

    @OnClick(R.id.btn_cut_ending)
    public void onEndingCut(View view) {
        if (editMode) {
            int trueIndex = getTrueIndex();
            if (trueIndex > pathStartIndex) {
                updatedPlaces = new ArrayList<>();
                pathEndIndex = trueIndex;
                pathEndMarker = getIndexOnDrawnPath(visibleMarkersIndex);
                for (int i = pathStartIndex; i <= pathEndIndex; i++) {
                    updatedPlaces.add(places.get(i));
                }
                if (newPath != null)
                    newPath.remove();
                newPath = mMap.addPolyline(createPath(getMarkers(updatedPlaces, 0, updatedPlaces.size() - 1), ContextCompat.getColor(this, R.color.colorNewPath)));
            } else
                Toast.makeText(this, "End can't be behind the beginning", Toast.LENGTH_SHORT).show();
        }
    }

    private int getIndexOnDrawnPath(int localIndex) {
        for (int i = 0; i < drawnMarkersList.size(); i++) {
            if (MapComparer.Compare(drawnMarkersList.get(i), editableMarkersList.get(localIndex)))
                return i;
        }
        return -1;
    }

    @OnClick(R.id.btn_cut_beginning)
    public void onBeginningCut(View view) {
        if (editMode) {
            int trueIndex = getTrueIndex();
            if (trueIndex < pathEndIndex) {
                updatedPlaces = new ArrayList<>();
                pathStartIndex = trueIndex;
                pathStartMarker = getIndexOnDrawnPath(visibleMarkersIndex);
                for (int i = pathStartIndex; i <= pathEndIndex; i++) {
                    updatedPlaces.add(places.get(i));
                }
                Polyline p = mMap.addPolyline(createPath(getMarkers(updatedPlaces, 0, updatedPlaces.size() - 1), ContextCompat.getColor(this, R.color.colorNewPath)));
                if (newPath != null)
                    newPath.remove();
                newPath = p;
            } else
                Toast.makeText(this, "Beginning can't be after the ending", Toast.LENGTH_SHORT).show();
        }
    }


    private List<Marker> insertMarkersAt(List<Marker> source, List<Marker> target, int index) {
        List<Marker> output = new ArrayList<>();
        for (int i = 0; i < target.size(); i++) {
            output.add(target.get(i));
            if (i == index) {
                for (int j = 0; j < source.size(); j++) {
                    output.add(source.get(j));
                }
            }
        }
        return output;
    }

    private List<Marker> removeMarkers(List<Marker> source, List<Marker> unneceseryMarkers) {
        for (int i = 0; i < source.size(); i++) {
            if (MapComparer.Compare(source.get(i), unneceseryMarkers.get(0))) {
                source.remove(i);
                for (int j = 1; j < unneceseryMarkers.size(); j++) {
                    if (MapComparer.Compare(source.get(i), unneceseryMarkers.get(j))) {
                        source.remove(i);
                    }
                }
                break;
            }
        }
        return source;
    }

    private List<Marker> trimMarkers(List<Marker> source, int first, int last) {
        List<Marker> output = new ArrayList<>();
        for (int i = first; i <= last; i++) {
            output.add(source.get(i));
        }
        return output;
    }

    private void correctOutOfBoundsMarkers() {
        if (firstIndex < 0 && lastIndex > places.size() - 1) {
            firstIndex = 0;
            lastIndex = places.size() - 1;
        }

        if (firstIndex < 0) {
            lastIndex += -firstIndex;
            firstIndex = 0;
        }
        if (lastIndex > places.size() - 1) {
            firstIndex -= lastIndex - (places.size() - 1);
            lastIndex = places.size() - 1;
        }
    }

    @OnClick(R.id.btn_plus)
    public void onPlusClick(View view) {
        if (editMode) {
            if (editableMarkersList.size() >= 100) {
                editMode = false;
                editableMarkersList.get(visibleMarkersIndex).setVisible(false);
                int trueIndex = getTrueIndex();
                firstIndex = trueIndex - step;
                lastIndex = trueIndex + step;


                correctOutOfBoundsMarkers();


                List<Marker> zoomedMarkers = getMarkers(places, firstIndex, lastIndex);
                if(trueIndex == pathStartIndex)
                    pathStartMarker += zoomedMarkers.size()/2;
                if (trueIndex < pathStartIndex)
                    pathStartMarker += zoomedMarkers.size();
                if(trueIndex == pathEndIndex)
                    pathEndMarker += zoomedMarkers.size()/2;
                if (trueIndex < pathEndIndex)
                    pathEndMarker += zoomedMarkers.size();

                step = zoomedMarkers.size() / 100;
                if (step < 1)
                    step = 1;


                drawnMarkersList = insertMarkersAt(zoomedMarkers, drawnMarkersList, visibleMarkersIndex);
                Polyline zoomedPath = mMap.addPolyline(createPath(drawnMarkersList, ContextCompat.getColor(this, R.color.colorOldPath)));
                path.remove();
                path = zoomedPath;

                editableMarkersList = zoomedMarkers;
                visibleMarkersIndex = zoomedMarkers.size() / 2;

                rangeBar.setMax(editableMarkersList.size() - 1);
                rangeBar.setProgress(visibleMarkersIndex);
                zoomCamera();
                changeStartFinishValues();

                List<Marker> updatePathMarkers = trimMarkers(drawnMarkersList, pathStartMarker, pathEndMarker);
                Polyline editedPath = mMap.addPolyline(createPath(updatePathMarkers, ContextCompat.getColor(this, R.color.colorNewPath)));
                newPath.remove();
                newPath = editedPath;

                editMode = true;
                editableMarkersList.get(visibleMarkersIndex).setVisible(true);
            } else
                Toast.makeText(this, "Cannot zoom anymore", Toast.LENGTH_SHORT).show();
        }
    }

    private int getTrueIndex() {
        return (int) ((float) (lastIndex - firstIndex) * (float) visibleMarkersIndex / (editableMarkersList.size() - 1)) + firstIndex;
    }

    @OnClick(R.id.btn_minus)
    public void onMinusClick(View view) {
        if (editMode) {
            if (firstIndex != 0 || lastIndex != places.size() - 1) {
                editableMarkersList.get(visibleMarkersIndex).setVisible(false);
                editMode = false;
                int trueIndex = getTrueIndex();

                drawnMarkersList = removeMarkers(drawnMarkersList, editableMarkersList);
                Polyline zoomedPath = mMap.addPolyline(createPath(drawnMarkersList, ContextCompat.getColor(this, R.color.colorOldPath)));
                path.remove();
                path = zoomedPath;


                if (pathStartIndex >= firstIndex && pathStartIndex <= lastIndex) {
                    pathStartMarker -= editableMarkersList.size()/2;
                } else if (trueIndex <= pathStartIndex)
                    pathStartMarker -= editableMarkersList.size();
                if (pathEndIndex >= firstIndex && pathEndIndex <= lastIndex) {
                    pathEndMarker -= editableMarkersList.size()/2;
                } else if (trueIndex <= pathEndIndex)
                    pathEndMarker -= editableMarkersList.size();


                int lastStep = (lastIndex - firstIndex + 1) / 2;
                firstIndex = trueIndex - (lastStep * 1000);
                lastIndex = trueIndex + (lastStep * 1000);

                correctOutOfBoundsMarkers();

                List<Marker> zoomedMarkers = getMarkers(places, firstIndex, lastIndex);

                step = (lastIndex - firstIndex + 1) / 100;


                editableMarkersList = zoomedMarkers;
                rangeBar.setMax(editableMarkersList.size() - 1);
                visibleMarkersIndex = zoomedMarkers.size() / 2;
                rangeBar.setProgress(visibleMarkersIndex);


                List<Marker> updatePathMarkers = trimMarkers(drawnMarkersList, pathStartMarker, pathEndMarker);
                Polyline editedPath = mMap.addPolyline(createPath(updatePathMarkers, ContextCompat.getColor(this, R.color.colorNewPath)));
                newPath.remove();
                newPath = editedPath;

                zoomCamera();
                changeStartFinishValues();
                editMode = true;
                editableMarkersList.get(visibleMarkersIndex).setVisible(true);
            } else
                Toast.makeText(this, "Cannot zoom out any more", Toast.LENGTH_SHORT).show();
        }
    }


    private void changeStartFinishValues() {
        int trueIndex = getTrueIndex();
        startValue.setText(firstIndex + "  current id: " + trueIndex);
        finishValue.setText(lastIndex + "");
    }

    private void stopPathEditing() {
        if (editMode) {
            //Hide marker if the list hasn't changed
            if (!changed) {
                editableMarkersList.get(visibleMarkersIndex).setVisible(false);
            }
            //Remove signs of editing path
            newPath.remove();
            //go out of editMode and disable rangeBar
            editMode = false;
            firstIndex = 0;
            lastIndex = places.size() - 1;
            editableMarkersList = getMarkers(places, firstIndex, lastIndex);
            drawnMarkersList = new ArrayList<>(editableMarkersList);
            pathStartIndex = firstIndex;
            pathStartMarker = 0;
            pathEndIndex = lastIndex;
            pathEndMarker = drawnMarkersList.size() - 1;
            step = (lastIndex - firstIndex) / 100;
            rangeBar.setProgress(0);
            rangeBar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorSeekDisabled));
            rangeBar.setEnabled(false);
            startValue.setText("");
            finishValue.setText("");
            zoomCamera();
            changed = false;
        }
    }

    @OnClick(R.id.btn_confirm)
    public void onConfirmClick(View view) {
        createConfirmDialog();
    }

    @OnClick(R.id.btn_cancel)
    public void onCancelClick(View view) {
        stopPathEditing();
    }

    private void createConfirmDialog() {
        if (editMode) {
            Context context = this;
            new AlertDialog.Builder(this)
                    .setTitle(R.string.confirm_cut)
                    .setMessage(R.string.confirm_cut_description)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Hide marker before changing list
                            editableMarkersList.get(visibleMarkersIndex).setVisible(false);
                            changed = true;

                            //Select proper places of the new path
                            places = updatedPlaces;

                            //Select proper markers of the new path
                            editableMarkersList = getMarkers(places, 0, places.size() - 1);

                            //Draw a new solid path
                            Polyline newSolidPath = mMap.addPolyline(createPath(editableMarkersList, ContextCompat.getColor(context, R.color.colorOldPath)));
                            path.remove();
                            path = newSolidPath;
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
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }


}
