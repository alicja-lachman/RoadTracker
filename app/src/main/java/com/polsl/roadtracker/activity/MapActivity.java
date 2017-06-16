package com.polsl.roadtracker.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
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
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.polsl.roadtracker.R;
import com.polsl.roadtracker.database.entity.LocationData;
import com.polsl.roadtracker.database.entity.RouteData;
import com.polsl.roadtracker.database.entity.RouteDataDao;
import com.polsl.roadtracker.utility.PositionInfo;
import com.polsl.roadtracker.utility.TimePlaceMarker;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, SeekBar.OnSeekBarChangeListener, GoogleMap.OnPolylineClickListener {

    RouteDataDao routeDataDao;
    @BindView(R.id.sb_change_range)
    SeekBar rangeBar;
    @BindView(R.id.tv_seek_bar_start)
    TextView startValue;
    @BindView(R.id.tv_seek_bar_finish)
    TextView finishValue;
    @BindView(R.id.btn_cut_beginning)
    Button cutBeginningButton;
    @BindView(R.id.btn_cut_ending)
    Button cutEndingButton;
    @BindView(R.id.btn_cancel)
    Button cancelButton;
    @BindView(R.id.btn_confirm)
    Button confirmButton;
    @BindView(R.id.path_edit_toolbar)
    LinearLayout pathEditLayout;

    private Toast toast;
    private long id;
    private Polyline path;
    private Polyline newPath;
    private GoogleMap mMap;
    private PositionInfo[] places;
    private PositionInfo[] updatedPlaces;
    private List<TimePlaceMarker> editableMarkersList;
    private List<TimePlaceMarker> drawnMarkersList;
    private boolean editMode = false;
    private TimePlaceMarker visibleMarker;
    private int visibleMarkersIndex;
    private int firstIndex, lastIndex;
    private List<TimePlaceMarker> baseMarkers;
    private float zoom = 0;
    private int pathStartIndex, pathEndIndex;
    private TimePlaceMarker pathStartMarker, pathEndMarker;
    private boolean changed = false;



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
        Intent intent = getIntent();
        setTitle(intent.getCharSequenceExtra("ROUTE_DESCRIPTION"));
        //Get route id
        id = intent.getLongExtra("ROUTE_ID", 0L);
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
        rangeBar.setEnabled(false);
        setPlaces();
        setUpMap();
    }

    public void setUpMap() {
        //clear the map before redraw to them
        //mMap.clear();
        mMap.setOnPolylineClickListener(this);
        mMap.setOnCameraIdleListener(getOnCameraIdleListener());
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        //Place markers
        editableMarkersList = getMarkers(places, 0, places.length - 1);
        drawnMarkersList = new ArrayList<>(editableMarkersList);
        baseMarkers = new ArrayList<>();
        for (TimePlaceMarker m : editableMarkersList) {
            baseMarkers.add(new TimePlaceMarker(m));
        }
        //Create path
        path = mMap.addPolyline(createPath(editableMarkersList, ContextCompat.getColor(this, R.color.colorOldPath)));
        firstIndex = 0;
        lastIndex = places.length - 1;
//        step = (lastIndex - firstIndex + 1) / 100;
        pathStartIndex = 0;
        pathStartMarker = editableMarkersList.get(0);
        pathEndIndex = places.length - 1;
        pathEndMarker = editableMarkersList.get(editableMarkersList.size() - 1);
        rangeBar.setMax(editableMarkersList.size() - 1);
        zoomCamera(drawnMarkersList, false);
    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        if (!editMode) {
            enableEditLayout();
            //Set up editing components
            visibleMarkersIndex = editableMarkersList.size() / 2;
            visibleMarker = editableMarkersList.get(visibleMarkersIndex);
            rangeBar.setEnabled(true);
            rangeBar.setMax(editableMarkersList.size() - 1);
            rangeBar.setProgress(visibleMarkersIndex);
            TimePlaceMarker visibleMarker = editableMarkersList.get(visibleMarkersIndex);
            //Show new path and chosen marker
            visibleMarker.setVisible(true);
            visibleMarker.showInfoWindow();
            changeStartFinishValues();
            newPath = mMap.addPolyline(createPath(editableMarkersList, ContextCompat.getColor(this, R.color.colorNewPath)));
            //Set up variables for zooming
//            zoomedMarkers = new ArrayList<>();
//            zoomedMarkers.add(editableMarkersList.size() / 2);
            editMode = true;
        }
    }

    private void disableEditLayout() {
        pathEditLayout.animate()
                .translationY(pathEditLayout.getHeight())
                .alpha(0.0f)
                .setDuration(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        pathEditLayout.setVisibility(View.GONE);
                    }
                });
        zoomCamera(editableMarkersList, false);
    }

    private void enableEditLayout() {
        pathEditLayout.animate()
                .translationY(0)
                .alpha(1.0f)
                .setDuration(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        pathEditLayout.setVisibility(View.VISIBLE);
                    }
                });
        zoomCamera(editableMarkersList, true);
    }

    private void zoomCamera(List<TimePlaceMarker> markers, boolean bottomToolbar) {
        //create for loop for get the LatLngbuilder from the marker list
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (TimePlaceMarker m : markers) {
            builder.include(m.getPosition());
        }
        //initialize the padding for map boundary
        int padding = 20;
        if (bottomToolbar)
            mMap.setPadding(padding, padding, padding, padding + pathEditLayout.getHeight());
        else
            mMap.setPadding(padding, padding, padding, padding);
        //create the bounds from latlngBuilder to set into map camera
        LatLngBounds bounds = builder.build();
        //create the camera with bounds and padding to set into map
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 5);
        //call the map call back to know map is loaded or not
        mMap.setOnMapLoadedCallback(() -> {
            //set animated zoom camera into map
            mMap.animateCamera(cameraUpdate);
        });
    }

    private List<TimePlaceMarker> getMarkers(PositionInfo[] places, int firstIndex, int lastIndex) {
        //Make date format object
        DateFormat dateFormat = DateFormat.getDateTimeInstance();

        //Create dummy Markers List
        List<TimePlaceMarker> markers = new ArrayList<>();
        int interval = (lastIndex - firstIndex + 1) / 100;
        if (interval == 0)
            interval = 1;

        for (int i = firstIndex; i <= lastIndex; i += interval) {
            //Add a marker
            PositionInfo point = places[i];
            Marker marker = mMap.addMarker(new MarkerOptions().position(point.getCooridinate())
                    .title(dateFormat.format(point.getDate()))
                    .visible(false)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker)));
            markers.add(new TimePlaceMarker(marker, point.getDate().getTime()));
        }

        //Add the last marker if its not added
        long lastTime = places[lastIndex].getDate().getTime();
        if (!markers.get(markers.size() - 1).isEqualWith(lastTime)) {
            PositionInfo point = places[places.length - 1];
            Marker marker = mMap.addMarker(new MarkerOptions().position(point.getCooridinate())
                    .title(dateFormat.format(point.getDate()))
                    .visible(false)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker)));
            markers.add(new TimePlaceMarker(marker, lastTime));
        }
        return markers;
    }

    private boolean setPlaces() {
        //intent.putExtra("ROUTE_ID", tracks.get(position).getId());
        //Get locations from database
        List<LocationData> locationData = routeDataDao.load(id).getLocationDataList();
        if (locationData.isEmpty())
            return false;
        //Prepare array
        places = new PositionInfo[locationData.size()];
        //Build list of positions
        for (int i = 0; i < locationData.size(); i++) {
            LocationData data = locationData.get(i);
            LatLng position = new LatLng(data.getLatitude(), data.getLongitude());
            Timestamp time = new Timestamp(data.getTimestamp());
            places[i] = new PositionInfo(position, time);
        }

        firstIndex = 0;
        lastIndex = places.length - 1;
        return true;
//        float k = -50;
//        float m = -50;
//        places = new PositionInfo[500];
//        int index = 0;
//        for (int i = 0; i < places.length; i += 2) {
//            k += 0.05;
//            places[index] = (new PositionInfo(new LatLng(k, m), new Timestamp(i * 1000)));
//            m += 0.05;
//            places[index + 1] = (new PositionInfo(new LatLng(k, m), new Timestamp((i + 1) * 1000)));
//            index += 2;
//        }
//        firstIndex = 0;
//        lastIndex = places.length - 1;
//        return true;
    }

    private PolylineOptions createPath(List<TimePlaceMarker> markers, int color) {
        //Create options containing points of the line
        PolylineOptions polylineOptions = new PolylineOptions().clickable(true);
        for (TimePlaceMarker m : markers) {
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
            visibleMarker.setVisible(false);
            //make a proper marker visible
            visibleMarkersIndex = (int) ((float) i * (float) (editableMarkersList.size() - 1) / seekBar.getMax());
            visibleMarker = editableMarkersList.get(visibleMarkersIndex);
            visibleMarker.setVisible(true);
            visibleMarker.showInfoWindow();
            //update the view
            changeStartFinishValues();
        }
    }

    private GoogleMap.OnCameraIdleListener getOnCameraIdleListener() {
        return () -> {
            float currentZoom = mMap.getCameraPosition().zoom;
            LatLngBounds currentBounds = getCurrentBounds();
            if (currentZoom < zoom)
                onZoomLOut(currentBounds);
            else if (currentZoom > zoom) {
                onZoomIn(currentBounds);
            } else {
                onMove(currentBounds);
            }

            zoom = currentZoom;
            if(editMode){
                visibleMarker.setVisible(false);
                visibleMarkersIndex = editableMarkersList.size()/2;
                visibleMarker = editableMarkersList.get(visibleMarkersIndex);
                visibleMarker.setVisible(true);
                rangeBar.setProgress(visibleMarkersIndex);
            }
            changeStartFinishValues();
        };
    }

    private void onZoomLOut(LatLngBounds currentBounds) {
        getFirstLastVisibleIndexes(currentBounds, baseMarkers);
        fillTheView();
        zoomToNewMarkers();
    }

    private void onZoomIn(LatLngBounds currentBounds) {
        getFirstLastVisibleIndexes(currentBounds, editableMarkersList);
        fillTheView();
        zoomToNewMarkers();
    }

    private void onMove(LatLngBounds currentBounds) {
        int closestIndex = getClosestMarker(currentBounds.getCenter(), editableMarkersList);
        int step = getCurrentStep();
        findNewBeginning(currentBounds, closestIndex, step);
        findNewEnding(currentBounds, closestIndex, step);
        zoomToNewMarkers();
    }

    private void findNewEnding(LatLngBounds bounds, int closestIndex, int step) {
        int index = closestIndex;
        while(index<editableMarkersList.size()-1 && isInBounds(bounds, editableMarkersList.get(index+1).getPosition())){
            index++;
        }
        lastIndex = getTrueIndex(index, editableMarkersList);
        fillForward(step);
    }

    private void findNewBeginning(LatLngBounds bounds, int closestIndex, int step) {
        int index = closestIndex;
        while(index>0 && isInBounds(bounds, editableMarkersList.get(index-1).getPosition())){
            index--;
        }

        firstIndex = getTrueIndex(index, editableMarkersList);
        fillBackward(step);
    }

    private int getClosestMarker(LatLng viewPoint, List<TimePlaceMarker> markers) {
        double currentDistance = countDistance(viewPoint, markers.get(0).getPosition());
        double minDistance = currentDistance;
        int minDistanceIndex=0;
        for(int i=1; i<markers.size(); i++){
            currentDistance = countDistance(viewPoint, markers.get(i).getPosition());
            if(currentDistance<minDistance){
                minDistance = currentDistance;
                minDistanceIndex = i;
            }
        }
        return minDistanceIndex;
    }

    private double countDistance(LatLng first, LatLng second) {
        double x = first.latitude - second.latitude;
        double y = first.longitude - second.longitude;
        return x * x + y * y;
    }

    private void fillForward(int step) {
        int index = lastIndex;
        while (places.length > index + step && isInBounds(getCurrentBounds(), places[index + step].getCooridinate())){
            index += step;
        }
        if (index != lastIndex) {
            lastIndex = index;
        }
    }

    private void fillBackward(int step) {
        int index = firstIndex;
        while (index - step >= 0 && isInBounds(getCurrentBounds(), places[index - step].getCooridinate())) {
            index -= step;
        }
        if (index != firstIndex) {
            firstIndex = index;
        }
    }

    private void fillTheView() {
        int step = getCurrentStep();
        fillForward(step);
        fillBackward(step);
    }

    private int getCurrentStep(){
        int step = (lastIndex-firstIndex)/100;
        return step>0 ? step : 1;
    }

    private void zoomToNewMarkers() {
        if ((firstIndex != 0 || lastIndex != places.length - 1) && (firstIndex != lastIndex)) {
            if (drawnMarkersList.size() != editableMarkersList.size())
                removeMarkers(drawnMarkersList, editableMarkersList);
            List<TimePlaceMarker> zoomedMarkers = getMarkers(places, firstIndex, lastIndex);
            insertMarkers(zoomedMarkers, drawnMarkersList);
            editableMarkersList = zoomedMarkers;
            insertCutStartFinishMarkers();
            if (!contains(drawnMarkersList, baseMarkers))
                Log.e("O CHUJ", "ALE ODJEBALO");
            redrawPaths(drawnMarkersList);
        }
    }

    private void insertCutStartFinishMarkers(){
        int i=0;
        for(; i<drawnMarkersList.size(); i++){
            if(!drawnMarkersList.get(i).isBefore(pathStartMarker)){
                if(!drawnMarkersList.get(i).isEqualWith(pathStartMarker))
                    drawnMarkersList.add(i,pathStartMarker);
                break;
            }
        }

        for(; i<drawnMarkersList.size(); i++){
            if(!drawnMarkersList.get(i).isBefore(pathEndMarker)){
                if(!drawnMarkersList.get(i).isEqualWith(pathEndMarker))
                    drawnMarkersList.add(i-1, pathEndMarker);
                break;
            }
        }
    }

    private boolean contains(List<TimePlaceMarker> container, List<TimePlaceMarker> searched) {
        int j = 0;
        for (int i = 0; i < searched.size(); i++) {
            while (!searched.get(i).isEqualWith(container.get(j))) {
                if (j < container.size())
                    j++;
                else
                    return false;
            }
        }
        return true;
    }

    private void redrawPaths(List<TimePlaceMarker> markers) {
        Polyline zoomedPath = mMap.addPolyline(createPath(markers, ContextCompat.getColor(this, R.color.colorOldPath)));
        path.remove();
        path = zoomedPath;
        path.setZIndex(1f);

        if (editMode) {
            List<TimePlaceMarker> updatePathMarkers = trimMarkers(drawnMarkersList, pathStartMarker, pathEndMarker);
            Polyline editedPath = mMap.addPolyline(createPath(updatePathMarkers, ContextCompat.getColor(this, R.color.colorNewPath)));
            newPath.remove();
            newPath = editedPath;
            newPath.setZIndex(1.1f);
        }
    }

    private LatLngBounds getCurrentBounds() {
        return mMap.getProjection().getVisibleRegion().latLngBounds;
    }

    private boolean isInBounds(LatLngBounds bounds, LatLng position) {
        return bounds.contains(position);
    }

    private void getFirstLastVisibleIndexes(LatLngBounds bounds, List<TimePlaceMarker> markersList) {
        int beginning = 0;
        int ending = markersList.size() - 1;

        for (int i = 0; i <= ending; i++) {
            if (isInBounds(bounds, markersList.get(i).getPosition())) {
                beginning = i;
                break;
            }
        }

        for (int i = beginning + 1; i <= ending; i++) {
            if (!isInBounds(bounds, markersList.get(i).getPosition())) {
                ending = --i;
                break;
            }
        }
        int first = getTrueIndex(beginning, markersList);
        int last = getTrueIndex(ending, markersList);
        firstIndex = first;
        lastIndex = last;
    }

    private void showTrimmedPath(){
        List<TimePlaceMarker> updatePathMarkers = trimMarkers(drawnMarkersList, pathStartMarker, pathEndMarker);
        Polyline editedPath = mMap.addPolyline(createPath(updatePathMarkers, ContextCompat.getColor(this, R.color.colorNewPath)));
        editedPath.setZIndex(1.1f);
        newPath.remove();
        newPath = editedPath;
    }

    @OnClick(R.id.btn_cut_ending)
    public void onEndingCut(View view) {
        if (editMode) {
            //get precise index corresponding to database
            int trueIndex = getTrueIndex(visibleMarkersIndex, editableMarkersList);
            if (trueIndex > pathStartIndex) {
                //Remember end marker and its database index
                pathEndIndex = trueIndex;
                pathEndMarker = editableMarkersList.get(visibleMarkersIndex);
                //Make a new editable path
                showTrimmedPath();
            } else{
                showToast("End can't be behind the beginning");
            }

        }
    }

    private void showToast(String message){
        if(toast!=null)
            toast.cancel();
        toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    @OnClick(R.id.btn_cut_beginning)
    public void onBeginningCut(View view) {
        if (editMode) {
            int trueIndex = getTrueIndex(visibleMarkersIndex, editableMarkersList);
            if (trueIndex < pathEndIndex) {
                //get precise index coresponding to database
                pathStartIndex = trueIndex;
                pathStartMarker = editableMarkersList.get(visibleMarkersIndex);
                //Make a new editable path
               showTrimmedPath();
            } else
                showToast("Beginning can't be after the ending");
        }
    }

    private void insertMarkers(List<TimePlaceMarker> source, List<TimePlaceMarker> target) {
        for (int i = 0; i < target.size(); i++) {
            //if new points should be behind marker
            if (!target.get(i).isBefore(source.get(0))) {
                //go to the beginning of the new path
                i--;
                for (int j = 0; j < source.size(); j++) {
                    //if new point would be in front of an old one
                    if (i < target.size() - 1 && target.get(i + 1).isBefore(source.get(j)))
                        i++;
                    //add new marker and increment index
                    target.add(++i, source.get(j));
                }
                //break after all markers are added
                break;
            }
        }
    }

    private void removeMarkers(List<TimePlaceMarker> source, List<TimePlaceMarker> unnecessaryMarkers) {
        int j = 0;
        for (int i = 0; i < source.size(); i++) {
            if (source.get(i).isEqualWith(unnecessaryMarkers.get(j))) {
                source.remove(i);
                j++;
                for (; j < unnecessaryMarkers.size(); j++) {
                    if (source.get(i).isEqualWith(unnecessaryMarkers.get(j))) {
                        source.remove(i);
                    } else {
                        //compare unnecessary one with next from the source next time
                        i++;
                        j--;
                    }
                }
                break;
            }
        }
    }

    private List<TimePlaceMarker> trimMarkers(List<TimePlaceMarker> source, TimePlaceMarker first, TimePlaceMarker last) {
        List<TimePlaceMarker> output = new ArrayList<>();
        int i;
        //Get i to the start of the path
        for (i = 0; i < source.size(); i++) {
            if (source.get(i).isEqualWith(first))
                break;
        }
        //Add all markers until finding the last one
        while (!source.get(i).isEqualWith(last)) {
            output.add(source.get(i));
            i++;
        }
        //Add the last one
        output.add(source.get(i));
        return output;
    }

    /**
     * Gets index corresponding to database location index
     *
     * @param index current index from editable markers
     * @return global index of the place in database
     */
    private int getTrueIndex(int index, List<TimePlaceMarker> markers) {
        return (int) ((float) (lastIndex - firstIndex) * (float) index / (markers.size() - 1)) + firstIndex;
    }

    /**
     * Changing values beneath the bar
     */
    private void changeStartFinishValues() {
        int trueIndex = getTrueIndex(visibleMarkersIndex, editableMarkersList);
        startValue.setText(firstIndex + "  current id: " + trueIndex);
        finishValue.setText(String.valueOf(lastIndex));
    }

    private void resetPathEditing() {
        if (editMode) {
            //Hide marker if the list hasn't changed
            if (!changed) {
                visibleMarker.setVisible(false);
            }
            //Remove signs of editing path
            newPath.remove();
            //go out of editMode and disable rangeBar
            editMode = false;
            disableEditLayout();
            rangeBar.setProgress(0);
            rangeBar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorSeekDisabled));
            rangeBar.setEnabled(false);
            startValue.setText("");
            finishValue.setText("");
            //Reset all variables
            firstIndex = 0;
            lastIndex = places.length - 1;
            editableMarkersList = getMarkers(places, firstIndex, lastIndex);
            drawnMarkersList = new ArrayList<>(editableMarkersList);
            pathStartIndex = 0;
            pathStartMarker = editableMarkersList.get(0);
            pathEndIndex = places.length - 1;
            pathEndMarker = editableMarkersList.get(editableMarkersList.size() - 1);
//            step = (lastIndex - firstIndex) / 100;
            //Repaint the path
            path.remove();
            path = mMap.addPolyline(createPath(drawnMarkersList, ContextCompat.getColor(this, R.color.colorOldPath)));
            zoomCamera(drawnMarkersList, false);
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
                    .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                        //Hide marker before changing list
                        editableMarkersList.get(visibleMarkersIndex).setVisible(false);
                        changed = true;

                        //prepare new data
                        updatedPlaces = new PositionInfo[pathEndIndex - pathStartIndex + 1];
                        int index = 0;
                        for (int j = pathStartIndex; j <= pathEndIndex; j++) {
                            updatedPlaces[index] = places[j];
                            index++;
                        }
                        //Select proper places of the new path
                        places = updatedPlaces;

                        //Select proper markers of the new path
                        editableMarkersList = getMarkers(places, 0, places.length - 1);

                        //Draw a new solid path
                        Polyline newSolidPath = mMap.addPolyline(createPath(editableMarkersList, ContextCompat.getColor(context, R.color.colorOldPath)));
                        path.remove();
                        path = newSolidPath;

                        //Save changes to database
                        saveRouteData();
                        //Prepare for new changes
                        resetPathEditing();
                    })
                    .setNegativeButton(R.string.no, (dialogInterface, i) -> {
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    /**
     * Saves changes made on map to db
     */
    private void saveRouteData() {
        RouteData routeData = routeDataDao.load(id);
        List<LocationData> locationData = routeData.getLocationDataList();

        //Remove from beginning
        for (int i = 0; i < pathStartIndex; i++) {
            locationData.remove(i);
        }

        //Remove from ending
        int firstOut = pathEndIndex + 1;
        for (int i = firstOut; i < locationData.size(); i++) {
            locationData.remove(firstOut);
        }

        routeData.update();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}
