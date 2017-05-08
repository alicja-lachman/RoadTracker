package com.polsl.roadtracker.activity;

import android.content.Context;
import android.content.DialogInterface;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, SeekBar.OnSeekBarChangeListener, GoogleMap.OnPolylineClickListener {
    @Inject
    RouteDataDao routeDataDao;
    @BindView(R.id.sb_change_range)
    SeekBar rangeBar;
    @BindView(R.id.tv_seek_bar_start)
    TextView startValue;
    @BindView(R.id.tv_seek_bar_finish)
    TextView finishValue;
    @BindView(R.id.btn_plus)
    ImageButton plusButton;
    @BindView(R.id.btn_minus)
    ImageButton minusButton;
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
    private Marker pathStartMarker, pathEndMarker;
    private boolean changed = false;
    private Stack<Marker> skippedMarkers = new Stack<>();
    private List<Integer> zoomedMarkers;

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
        pathStartIndex = 0;
        pathStartMarker = editableMarkersList.get(0);
        pathEndIndex = places.size() - 1;
        pathEndMarker = editableMarkersList.get(editableMarkersList.size() - 1);
        rangeBar.setMax(editableMarkersList.size() - 1);
        zoomCamera(drawnMarkersList);
    }


    @Override
    public void onPolylineClick(Polyline polyline) {
        if (!editMode) {
            //Set up editing components
            visibleMarkersIndex = editableMarkersList.size() / 2;
            rangeBar.setEnabled(true);
            rangeBar.setMax(editableMarkersList.size() - 1);
            rangeBar.setProgress(visibleMarkersIndex);
            Marker visibleMarker = editableMarkersList.get(visibleMarkersIndex);
            //Show new path and chosen marker
            visibleMarker.setVisible(true);
            visibleMarker.showInfoWindow();
            changeStartFinishValues();
            newPath = mMap.addPolyline(createPath(editableMarkersList, ContextCompat.getColor(this, R.color.colorNewPath)));
            //Set up variables for zooming
            zoomedMarkers = new ArrayList<>();
            zoomedMarkers.add(editableMarkersList.size() / 2);
            editMode = true;
        }
    }

    private void zoomCamera(List<Marker> markers) {
        //create for loop for get the LatLngbuilder from the marker list
        builder = new LatLngBounds.Builder();
        for (Marker m : markers) {
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
//        float k = -50;
//        float m = -50;
//        for (int i = 0; i < 100000; i += 2) {
//            k += 0.001;
//            places.add(new PositionInfo(new LatLng(k, m), new Timestamp(i)));
//            m += 0.001;
//            places.add(new PositionInfo(new LatLng(k, m), new Timestamp(i)));
//        }
//        return true;
    }

    private List<Marker> getMarkers(List<PositionInfo> places, int firstIndex, int lastIndex) {
        //Make date format object
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS");

        //Create dummy Markers List
        List<Marker> markers = new ArrayList<>();
        int interval = (lastIndex - firstIndex + 1) / 100;
        if (interval == 0)
            interval = 1;
        for (int i = firstIndex; i <= lastIndex; i += interval) {
            //Add a marker
            PositionInfo point = places.get(i);
            Marker marker = mMap.addMarker(new MarkerOptions().position(point.getCooridinate())
                    .title(dateFormat.format(point.getDate()))
                    .visible(false)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker)));
            markers.add(marker);
        }
        //Add the last marker if its not added
        if (!MapComparer.Compare(markers.get(markers.size() - 1).getPosition(), places.get(lastIndex).getCooridinate())) {
            PositionInfo point = places.get(lastIndex);
            Marker marker = mMap.addMarker(new MarkerOptions().position(point.getCooridinate())
                    .title(dateFormat.format(point.getDate()))
                    .visible(false)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker)));
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
            //make last marker invisible
            editableMarkersList.get(visibleMarkersIndex).setVisible(false);
            //make a proper marker visible
            visibleMarkersIndex = (int) ((float) i * (float) (editableMarkersList.size() - 1) / seekBar.getMax());
            Marker newVisible = editableMarkersList.get(visibleMarkersIndex);
            newVisible.setVisible(true);
            newVisible.showInfoWindow();
            //update the view
            changeStartFinishValues();
        }
    }

    @OnClick(R.id.btn_cut_ending)
    public void onEndingCut(View view) {
        if (editMode) {
            //get precise index coresponding to database
            int trueIndex = getTrueIndex(visibleMarkersIndex);
            if (trueIndex > pathStartIndex) {
                //Remember end marker and its database index
                pathEndIndex = trueIndex;
                pathEndMarker = editableMarkersList.get(visibleMarkersIndex);
                //Make a new editable path
                List<Marker> updatePathMarkers = trimMarkers(drawnMarkersList, pathStartMarker, pathEndMarker);
                Polyline editedPath = mMap.addPolyline(createPath(updatePathMarkers, ContextCompat.getColor(this, R.color.colorNewPath)));
                newPath.remove();
                newPath = editedPath;
            } else
                Toast.makeText(this, "End can't be behind the beginning", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_cut_beginning)
    public void onBeginningCut(View view) {
        if (editMode) {
            int trueIndex = getTrueIndex(visibleMarkersIndex);
            if (trueIndex < pathEndIndex) {
                //get precise index coresponding to database
                pathStartIndex = trueIndex;
                pathStartMarker = editableMarkersList.get(visibleMarkersIndex);
                //Make a new editable path
                List<Marker> updatePathMarkers = trimMarkers(drawnMarkersList, pathStartMarker, pathEndMarker);
                Polyline editedPath = mMap.addPolyline(createPath(updatePathMarkers, ContextCompat.getColor(this, R.color.colorNewPath)));
                newPath.remove();
                newPath = editedPath;
            } else
                Toast.makeText(this, "Beginning can't be after the ending", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Inserts markers from the source to the target beginning at chosenMarker
     *
     * @param source       Markers to be added
     * @param target       Markers to be extended
     * @param chosenMarker Marker which must be in the target behind which source will be added
     * @return new List of combined markers
     */
    private List<Marker> insertMarkersAt(List<Marker> source, List<Marker> target, Marker chosenMarker) {
        List<Marker> output = new ArrayList<>();
        boolean added = false;
        for (int i = 0; i < target.size(); i++) {
            //While there is no chosenMarker met just add the target
            output.add(target.get(i));
            if (!added && i + 1 < target.size() && MapComparer.Compare(target.get(i + 1), chosenMarker)) {
                //Add half of the source
                for (int j = 0; j < source.size() / 2; j++) {
                    output.add(source.get(j));
                }
                //Remember the middle marker, but skipp it because it will cause the path to be painted wrong
                //It will be after deleting the source from the target
                skippedMarkers.push(target.get(++i));
                //Add the other half of the source
                for (int j = source.size() / 2; j < source.size(); j++) {
                    output.add(source.get(j));
                }
                added = true;
            }
        }
        return output;
    }

    /**
     * Removes given markers from the source and adds lastly skipped marker to the path
     *
     * @param source             markers which contain unnecessary ones
     * @param unnecessaryMarkers markers to be deleted
     * @return proper list of markers
     */
    private List<Marker> removeMarkers(List<Marker> source, List<Marker> unnecessaryMarkers) {
        //Debug variable
        int deleted = 0;
        for (int i = 0; i < source.size(); i++) {
            //If the unnecessary markers are met
            if (MapComparer.Compare(source.get(i), unnecessaryMarkers.get(0))) {
                for (int j = 0; j < unnecessaryMarkers.size(); j++) {
                    //Compare them to start/finish as we want to leave them on the path
                    if (!MapComparer.Compare(source.get(i), pathStartMarker)
                            && !MapComparer.Compare(source.get(i), pathEndMarker)) {
                        //If markers are the same delete them
                        if (MapComparer.Compare(source.get(i), unnecessaryMarkers.get(j))) {
                            deleted++;
                            source.remove(i);
                        } else {
                            //This is the skipped marker, which will be added later on
                            i++;
                            j--;
                        }
                        //If start/finish was found skipp removing it
                    } else {
                        j++;
                        i++;
                    }
                    //If the skipp caused going over the size break
                    if (i == source.size())
                        break;
                }
                //Add lastly skipped marker so there wont be data loss
                source.add(i, skippedMarkers.pop());
                //Debug
                Log.d("Deleted", deleted + "");
                break;
            }
        }
        return source;
    }

    private List<Marker> trimMarkers(List<Marker> source, Marker first, Marker last) {
        List<Marker> output = new ArrayList<>();
        int i;
        //Get i to the start of the path
        for (i = 0; i < source.size(); i++) {
            if (MapComparer.Compare(source.get(i), first))
                break;
        }
        //Add all markers until finding the last one
        while (!MapComparer.Compare(source.get(i), last)) {
            output.add(source.get(i));
            i++;
        }
        //Add the last one
        output.add(source.get(i));
        return output;
    }

    private void correctOutOfBoundsMarkers() {
        //Move first and last indexes so they are in bounds
        if (firstIndex < 0) {
            lastIndex += -firstIndex;
            firstIndex = 0;
        }
        if (lastIndex > places.size() - 1) {
            firstIndex -= lastIndex - (places.size() - 1);
            lastIndex = places.size() - 1;
        }

        //If both indexes are out of bounds it means that there is 0 zoom
        if (firstIndex <= 0 && lastIndex >= places.size() - 1) {
            firstIndex = 0;
            lastIndex = places.size() - 1;
        }
    }

    @OnClick(R.id.btn_plus)
    public void onPlusClick(View view) {
        if (editMode) {
            if (editableMarkersList.size() >= 100) {
                editMode = false;
                editableMarkersList.get(visibleMarkersIndex).setVisible(false);
                int trueIndex = getTrueIndex(visibleMarkersIndex);
                //Remember the zoom point
                zoomedMarkers.add(trueIndex);
                //Determine new boundries of database locations
                firstIndex = trueIndex - step;
                lastIndex = trueIndex + step;
                correctOutOfBoundsMarkers();
                //Get new more precise 100 markers
                List<Marker> zoomedMarkers = getMarkers(places, firstIndex, lastIndex);

                //Count a new distance between points from database
                step = (lastIndex - firstIndex + 1) / 100;
                if (step < 1)
                    step = 1;

                //Get markers for path
                drawnMarkersList = insertMarkersAt(zoomedMarkers, drawnMarkersList, editableMarkersList.get(visibleMarkersIndex));
                Polyline zoomedPath = mMap.addPolyline(createPath(drawnMarkersList, ContextCompat.getColor(this, R.color.colorOldPath)));
                path.remove();
                path = zoomedPath;

                //Use only zoomed markers to enable editing
                editableMarkersList = zoomedMarkers;

                //Set up controls and map
                visibleMarkersIndex = zoomedMarkers.size() / 2;
                rangeBar.setMax(editableMarkersList.size() - 1);
                rangeBar.setProgress(visibleMarkersIndex);
                zoomCamera(editableMarkersList);
                changeStartFinishValues();

                //Get a proper updated path with proper beginning and ending
                List<Marker> updatePathMarkers = trimMarkers(drawnMarkersList, pathStartMarker, pathEndMarker);
                Polyline editedPath = mMap.addPolyline(createPath(updatePathMarkers, ContextCompat.getColor(this, R.color.colorNewPath)));
                newPath.remove();
                newPath = editedPath;

                editMode = true;
                editableMarkersList.get(visibleMarkersIndex).setVisible(true);
                //Debug
                Log.d("Markers size", drawnMarkersList.size() + "");
            } else
                Toast.makeText(this, "Cannot zoom anymore", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Gets index corresponding to database location index
     *
     * @param index current index from editable markers
     * @return global index of the place in database
     */
    private int getTrueIndex(int index) {
        return (int) ((float) (lastIndex - firstIndex) * (float) index / (editableMarkersList.size() - 1)) + firstIndex;
    }

    @OnClick(R.id.btn_minus)
    public void onMinusClick(View view) {
        if (editMode) {
            if (firstIndex != 0 || lastIndex != places.size() - 1) {
                editableMarkersList.get(visibleMarkersIndex).setVisible(false);
                editMode = false;
                //Get proper zoom point
                int trueIndex = zoomedMarkers.get(zoomedMarkers.size() - 2);
                zoomedMarkers.remove(zoomedMarkers.size() - 1);

                //Remove markers which won't be accessible and draw a path
                drawnMarkersList = removeMarkers(drawnMarkersList, editableMarkersList);
                Polyline zoomedPath = mMap.addPolyline(createPath(drawnMarkersList, ContextCompat.getColor(this, R.color.colorOldPath)));
                path.remove();
                path = zoomedPath;

                //Get new boundaries
                int lastStep = (lastIndex - firstIndex + 1) / 2;
                firstIndex = trueIndex - (lastStep * 100) / 2;
                lastIndex = trueIndex + (lastStep * 100) / 2;
                correctOutOfBoundsMarkers();

                //Get proper editable markers
                List<Marker> zoomedMarkers = getMarkers(places, firstIndex, lastIndex);
                //Count new distance between markers indexes
                step = (lastIndex - firstIndex + 1) / 100;

                //Set up controls and map
                editableMarkersList = zoomedMarkers;
                rangeBar.setMax(editableMarkersList.size() - 1);
                visibleMarkersIndex = zoomedMarkers.size() / 2;
                rangeBar.setProgress(visibleMarkersIndex);
                zoomCamera(editableMarkersList);
                changeStartFinishValues();

                //Make a proper updated path with proper beginning and ending
                List<Marker> updatePathMarkers = trimMarkers(drawnMarkersList, pathStartMarker, pathEndMarker);
                Polyline editedPath = mMap.addPolyline(createPath(updatePathMarkers, ContextCompat.getColor(this, R.color.colorNewPath)));
                newPath.remove();
                newPath = editedPath;

                editMode = true;
                editableMarkersList.get(visibleMarkersIndex).setVisible(true);
                Log.d("Markers size", drawnMarkersList.size() + "");
            } else
                Toast.makeText(this, "Cannot zoom out any more", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Changing values beneath the bar
     */
    private void changeStartFinishValues() {
        int trueIndex = getTrueIndex(visibleMarkersIndex);
        startValue.setText(firstIndex + "  current id: " + trueIndex);
        finishValue.setText(lastIndex + "");
    }

    private void resetPathEditing() {
        if (editMode) {
            //Hide marker if the list hasn't changed
            if (!changed) {
                editableMarkersList.get(visibleMarkersIndex).setVisible(false);
            }
            //Remove signs of editing path
            newPath.remove();
            //go out of editMode and disable rangeBar
            editMode = false;
            rangeBar.setProgress(0);
            rangeBar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorSeekDisabled));
            rangeBar.setEnabled(false);
            startValue.setText("");
            finishValue.setText("");
            //Reset all variables
            firstIndex = 0;
            lastIndex = places.size() - 1;
            editableMarkersList = getMarkers(places, firstIndex, lastIndex);
            drawnMarkersList = new ArrayList<>(editableMarkersList);
            pathStartIndex = 0;
            pathStartMarker = editableMarkersList.get(0);
            pathEndIndex = places.size() - 1;
            pathEndMarker = editableMarkersList.get(editableMarkersList.size() - 1);
            step = (lastIndex - firstIndex) / 100;
            //Repaint the path
            path.remove();
            path = mMap.addPolyline(createPath(drawnMarkersList, ContextCompat.getColor(this, R.color.colorOldPath)));
            zoomCamera(editableMarkersList);
            changed = false;
        }
    }

    @OnClick(R.id.btn_confirm)
    public void onConfirmClick(View view) {
        createConfirmDialog();
    }

    @OnClick(R.id.btn_cancel)
    public void onCancelClick(View view) {
        resetPathEditing();
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

                            //prepare new data
                            updatedPlaces = new ArrayList<>();
                            for (int j = pathStartIndex; j <= pathEndIndex; j++) {
                                updatedPlaces.add(places.get(j));
                            }
                            //Select proper places of the new path
                            places = updatedPlaces;

                            //Select proper markers of the new path
                            editableMarkersList = getMarkers(places, 0, places.size() - 1);

                            //Draw a new solid path
                            Polyline newSolidPath = mMap.addPolyline(createPath(editableMarkersList, ContextCompat.getColor(context, R.color.colorOldPath)));
                            path.remove();
                            path = newSolidPath;
                            resetPathEditing();
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
