package com.polsl.roadtracker.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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
import com.polsl.roadtracker.database.RoadtrackerDatabaseHelper;
import com.polsl.roadtracker.database.SensorData;
import com.polsl.roadtracker.database.entity.AccelerometerData;
import com.polsl.roadtracker.database.entity.AccelerometerDataDao;
import com.polsl.roadtracker.database.entity.AmbientTemperatureData;
import com.polsl.roadtracker.database.entity.AmbientTemperatureDataDao;
import com.polsl.roadtracker.database.entity.DaoSession;
import com.polsl.roadtracker.database.entity.GyroscopeData;
import com.polsl.roadtracker.database.entity.GyroscopeDataDao;
import com.polsl.roadtracker.database.entity.LocationData;
import com.polsl.roadtracker.database.entity.LocationDataDao;
import com.polsl.roadtracker.database.entity.MagneticFieldData;
import com.polsl.roadtracker.database.entity.MagneticFieldDataDao;
import com.polsl.roadtracker.database.entity.RpmData;
import com.polsl.roadtracker.database.entity.RpmDataDao;
import com.polsl.roadtracker.database.entity.RouteData;
import com.polsl.roadtracker.database.entity.RouteDataDao;
import com.polsl.roadtracker.database.entity.SpeedData;
import com.polsl.roadtracker.database.entity.SpeedDataDao;
import com.polsl.roadtracker.database.entity.ThrottlePositionData;
import com.polsl.roadtracker.database.entity.ThrottlePositionDataDao;
import com.polsl.roadtracker.utility.PositionInfo;
import com.polsl.roadtracker.utility.TimePlaceMarker;
import org.greenrobot.greendao.query.DeleteQuery;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * Activity of showing picked route on the map, enables editing the route by picking the right start and finish time.
 */
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, SeekBar.OnSeekBarChangeListener, GoogleMap.OnPolylineClickListener {

    //Database classes
    DaoSession daoSession;
    AccelerometerDataDao accelerometerDataDao;
    AmbientTemperatureDataDao ambientTemperatureDataDao;
    GyroscopeDataDao gyroscopeDataDao;
    MagneticFieldDataDao magneticFieldDataDao;
    RpmDataDao rpmDataDao;
    SpeedDataDao speedDataDao;
    ThrottlePositionDataDao throttlePositionDataDao;
    LocationDataDao locationDataDao;
    RouteDataDao routeDataDao;

    /**
     * Slider used to pick specified marker
     */
    @BindView(R.id.sb_change_range)
    SeekBar rangeBar;
    /**
     *
     */
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

    /**
     * Wake lock ensuring that the data will be saved even after phone being blocked
     */
    private PowerManager.WakeLock wakeLock;
    /**
     * Dialog showing when changes are saved to database
     */
    private ProgressDialog progressDialog;
    /**
     * Number of points which are seen at map, can be decreased for performance reasons
     */
    private int numberOfPoints = 500;
    /**
     * Toast showing warnings/information to the user
     */
    private Toast toast;
    /**
     * Line showing the route
     */
    private Polyline path;
    /**
     * Line showing the route if changes are confirmed
     */
    private Polyline newPath;
    /**
     * Google map showing route
     */
    private GoogleMap mMap;
    /**
     * LocationData wraper array for better performance (index access better than in a list)
     */
    private PositionInfo[] places;
    /**
     * Markers which are currently being able to be chosen
     */
    private List<TimePlaceMarker> editableMarkersList;
    /**
     * Markers which are drawn on the map
     */
    private List<TimePlaceMarker> drawnMarkersList;
    /**
     * Currently picked marker
     */
    private TimePlaceMarker visibleMarker;
    /**
     * Marker indicating currently chosen start of the route
     */
    private TimePlaceMarker pathStartMarker;
    /**
     * Marker indicating currently chosen end of the route
     */
    private TimePlaceMarker pathEndMarker;
    /**
     * Index of visible marker on editableMarkersList used to be imitated on the slider
     */
    private int visibleMarkersIndex;
    /**
     * Index of places array. Marker with the same location and time is currently
     * the first visible one on the map.
     */
    private int firstIndex;
    /**
     * Index of places array. Marker with the same location and time is currently
     * the last visible one on the map.
     */
    private int lastIndex;
    /**
     * Index of places array. Marker with the same location and time is currently
     * chosen as the start of the path.
     */
    private int pathStartIndex;
    /**
     * Index of places array. Marker with the same location and time is currently
     * chosen as the finish of the path.
     */
    private int pathEndIndex;
    /**
     * Variable indicating if the route was changed or changes were canceled
     */
    private boolean changed = false;
    /**
     * Variable indicating if the user is currently editing the path
     */
    private boolean editMode = false;

    /**
     * Method used when activity is created. Used to initialize database variables, map and libraries
     */
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
        //Get route dbName
        String dbName = intent.getStringExtra("ROUTE_ID");
        daoSession = RoadtrackerDatabaseHelper.getDaoSessionForDb(dbName);
        locationDataDao = daoSession.getLocationDataDao();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
    }

    /**
     * Method of creating menu on the top of the activity
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Listener method used to handle "back" and "info" buttons on menu
     */
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

    /**
     * Listener method invoked when the map is ready to use. Initialization of path and markers
     * @param googleMap reference to map object
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        rangeBar.setOnSeekBarChangeListener(this);
        rangeBar.setEnabled(false);
        setUpPositionInfo();
        setUpMap();
    }

    /**
     * Shows the toast
     * @param message message which is showed on the toast
     */
    private void showToast(String message) {
        if (toast != null)
            toast.cancel();
        toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * Methods used to create path and initialize information about markers.
     * Causing zoom to the path.
     */
    public void setUpMap() {
        //clear the map before redraw to them
        mMap.setOnPolylineClickListener(this);
        mMap.setOnCameraIdleListener(getOnCameraIdleListener());
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        //Place markers
        editableMarkersList = getMarkers(places, 0, places.length - 1, numberOfPoints);
        drawnMarkersList = new ArrayList<>(editableMarkersList);
        //Create path
        path = mMap.addPolyline(createPath(editableMarkersList, ContextCompat.getColor(this, R.color.colorOldPath)));
        firstIndex = 0;
        lastIndex = places.length - 1;
        pathStartIndex = 0;
        pathStartMarker = editableMarkersList.get(0);
        pathEndIndex = places.length - 1;
        pathEndMarker = editableMarkersList.get(editableMarkersList.size() - 1);
        rangeBar.setMax(editableMarkersList.size() - 1);
        zoomCamera(drawnMarkersList, false);
    }

    /**
     * Method giving the user ability to edit the path. Creates another path on top which symbolize
     * the edited one, which would become the original after confirmation. It shows the edit panel
     * @param polyline reference to the line which was clicked
     */
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
            editMode = true;
        }
    }

    /**
     * Hides edit panel
     */
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

    /**
     * Shows edit panel
     */
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

    /**
     * Zooms camera so it shows all markers specified in the list
     * @param markers list of markers which will be seen after the zoom
     * @param bottomToolbar boolean which indicates if edit panel is visible
     */
    private void zoomCamera(List<TimePlaceMarker> markers, boolean bottomToolbar) {
        //create for loop for get the LatLngbuilder from the marker list
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (TimePlaceMarker m : markers) {
            builder.include(m.getPosition());
        }
        //initialize the padding for map boundary
        int padding = 10;
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

    /**
     * Method used to get markers from array between specified indexes
     * @param places array holding all data about the markers from database
     * @param firstIndex indicates start of section from which markers will be selected
     * @param lastIndex indicates end of section from which markers will be selected
     * @param numberOfReturnedPoints max number of points which will be selected from the section
     * @return list of markers from section of size specified by the param
     */
    private List<TimePlaceMarker> getMarkers(PositionInfo[] places, int firstIndex, int lastIndex, int numberOfReturnedPoints) {
        //Make date format object
        DateFormat dateFormat = DateFormat.getDateTimeInstance();

        //Create dummy Markers List
        List<TimePlaceMarker> markers = new ArrayList<>();
        int interval = (lastIndex - firstIndex + 1) / numberOfReturnedPoints;
        if (interval == 0)
            interval = 1;

        for (int i = firstIndex; i <= lastIndex; i += interval) {
            //Add a marker
            PositionInfo point = places[i];
            Marker marker = mMap.addMarker(new MarkerOptions().position(point.getCooridinate())
                    .title(dateFormat.format(point.getDate()))
                    .visible(false)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker)));
            markers.add(new TimePlaceMarker(marker, point.getDate().getTime(), i));
        }

        //Add the last marker if its not added
        long lastTime = places[lastIndex].getDate().getTime();
        if (!markers.get(markers.size() - 1).isEqualWith(lastTime)) {
            PositionInfo point = places[lastIndex];
            Marker marker = mMap.addMarker(new MarkerOptions().position(point.getCooridinate())
                    .title(dateFormat.format(point.getDate()))
                    .visible(false)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker)));
            markers.add(new TimePlaceMarker(marker, lastTime, lastIndex));
        }
        return markers;
    }

    /**
     * Method converting LocationData to PlaceInfo enabling different LocationData formats
     * @return true if operation was successful or false if there is no data
     */
    private boolean setUpPositionInfo() {
        //Get locations from database
        List<LocationData> locationData = locationDataDao.loadAll();
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
    }

    /**
     * Creates object which keep information about line
     * @param markers markers which should be included in path
     * @param color color of the path (passed by reference to resources)
     * @return object containing information about location, width and color
     */
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

    /**
     * Handles change on the slider
     * @param seekBar reference to the slider
     * @param i current value chosen on the slider
     * @param b is from user
     */
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

    /**
     * Handles zooms and camera moves. Showing proper number of markers regardless of the zoom.
     * @return returns the listener of camera move
     */
    private GoogleMap.OnCameraIdleListener getOnCameraIdleListener() {
        return () -> {
            LatLngBounds bounds = getCurrentBounds();
            getFirstLastVisibleIndexes(bounds, drawnMarkersList);
            fillTheView();
            zoomToNewMarkers();
            Collections.sort(drawnMarkersList);
            changeStartFinishValues();
        };
    }

    /**
     * Trying to find end marker out of now-editable markers
     * @param step distance on places array between markers
     */
    private void fillForward(int step) {
        int index = lastIndex;
        while (places.length > index + step && isInBounds(getCurrentBounds(), places[index + step].getCooridinate())) {
            index += step;
        }
        if (index != lastIndex) {
            lastIndex = index;
        }
    }

    /**
     * Trying to find begin marker out of now-editable markers
     * @param step distance on places array between markers
     */
    private void fillBackward(int step) {
        int index = firstIndex;
        while (index - step >= 0 && isInBounds(getCurrentBounds(), places[index - step].getCooridinate())) {
            index -= step;
        }
        if (index != firstIndex) {
            firstIndex = index;
        }
    }

    /**
     * Filling visible part of the map with markers
     */
    private void fillTheView() {
        int step = getCurrentStep();
        fillForward(step);
        fillBackward(step);
    }

    /**
     * Gets distance from places array between markers
     * @return index distance between consecutive markers
     */
    private int getCurrentStep() {
        int step = (lastIndex - firstIndex) / numberOfPoints;
        return step > 0 ? step : 1;
    }

    /**
     * Gets new markers and inserts them on the right place on the path
     */
    private void zoomToNewMarkers() {
        if (drawnMarkersList.size() != editableMarkersList.size())
            removeMarkers(drawnMarkersList, editableMarkersList);
        List<TimePlaceMarker> zoomedMarkers = getMarkers(places, firstIndex, lastIndex, numberOfPoints);
        insertMarkers(zoomedMarkers, drawnMarkersList);
        editableMarkersList = zoomedMarkers;
        insertCutStartFinishMarkers();
        redrawPaths(drawnMarkersList);
    }

    /**
     * Keeps start and finish markers on the map even if they were deleted on zooming process
     */
    private void insertCutStartFinishMarkers() {
        int i = 0;
        for (; i < drawnMarkersList.size(); i++) {
            if (!drawnMarkersList.get(i).isBefore(pathStartMarker)) {
                if (!drawnMarkersList.get(i).isEqualWith(pathStartMarker))
                    drawnMarkersList.add(i, pathStartMarker);
                break;
            }
        }

        for (; i < drawnMarkersList.size(); i++) {
            if (!drawnMarkersList.get(i).isBefore(pathEndMarker)) {
                if (!drawnMarkersList.get(i).isEqualWith(pathEndMarker))
                    drawnMarkersList.add(i - 1, pathEndMarker);
                break;
            }
        }
    }

    /**
     * Redraws original path so it matches the param and redraws edit path if in edit mode
     * @param markers markers creating original path
     */
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

    /**
     * Get bounds of the current view
     * @return LatLngBounds containing location information about current view
     */
    private LatLngBounds getCurrentBounds() {
        return mMap.getProjection().getVisibleRegion().latLngBounds;
    }

    /**
     * Indicates if position is in the bounds
     * @param bounds bounds of the view
     * @param position position on the map
     * @return true if position is inside the bounds or false otherwise
     */
    private boolean isInBounds(LatLngBounds bounds, LatLng position) {
        return bounds.contains(position);
    }

    /**
     * Updates firstIndex and lastIndex. They indicate which marker from the list is the first and last
     * visible on the current view
     * @param bounds bounds of the current view
     * @param markersList list of markers which should be iterated
     */
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
        firstIndex = markersList.get(beginning).getIndex();
        lastIndex = markersList.get(ending).getIndex();
    }

    /**
     * Shows original path trimmed from new start to new finish
     */
    private void showTrimmedPath() {
        List<TimePlaceMarker> updatePathMarkers = trimMarkers(drawnMarkersList, pathStartMarker, pathEndMarker);
        Polyline editedPath = mMap.addPolyline(createPath(updatePathMarkers, ContextCompat.getColor(this, R.color.colorNewPath)));
        editedPath.setZIndex(1.1f);
        newPath.remove();
        newPath = editedPath;
    }

    /**
     * On choosing marker to become a new ending
     */
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
            } else {
                showToast("End can't be behind the beginning");
            }

        }
    }

    /**
     * On choosing marker to become a new beginning
     */
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

    /**
     * Inserts markers from source to target. Works like adding to a set
     * @param source markers to be added
     * @param target place to add markers
     */
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

    /**
     * Removes unnecessaryMarkers from the source
     * @param source the whole container of markers
     * @param unnecessaryMarkers markers from the container to be deleted
     */
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

    /**
     * Get markers between first and last specified by params
     * @param source the whole list of markers
     * @param first the first which should be returned
     * @param last the last which should be returned
     * @return list of markers from first to last
     */
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
     * @param index current index from editable markers
     * @return global index of the place in database
     */
    private int getTrueIndex(int index, List<TimePlaceMarker> markers) {
        return markers.get(index).getIndex();
    }

    /**
     * Changing values beneath the bar
     */
    private void changeStartFinishValues() {
        rangeBar.setMax(editableMarkersList.size()-1);
        startValue.setText(pathStartMarker.getDate().toString() + "  picked marker: " + visibleMarker.getDate().toString());
        finishValue.setText(pathEndMarker.getDate().toString());
    }

    /**
     * Resets variables and edit panel
     */
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
            visibleMarkersIndex = 0;
            visibleMarker = null;
            firstIndex = 0;
            lastIndex = places.length - 1;
            editableMarkersList = getMarkers(places, firstIndex, lastIndex, numberOfPoints);
            rangeBar.setMax(editableMarkersList.size()-1);
            drawnMarkersList = new ArrayList<>(editableMarkersList);
            pathStartIndex = 0;
            pathStartMarker = editableMarkersList.get(0);
            pathEndIndex = places.length - 1;
            pathEndMarker = editableMarkersList.get(editableMarkersList.size() - 1);
            //Repaint the path
            path.remove();
            path = mMap.addPolyline(createPath(drawnMarkersList, ContextCompat.getColor(this, R.color.colorOldPath)));
            zoomCamera(drawnMarkersList, false);
            changed = false;
        }
    }

    /**
     * On confirm button click.
     */
    @OnClick(R.id.btn_confirm)
    public void onConfirmClick(View view) {
        confirmChanges();
    }

    /**
     * On cancel button click.
     */
    @OnClick(R.id.btn_cancel)
    public void onCancelClick(View view) {
        resetPathEditing();
    }

    /**
     * Cut markers from the original path, saves data to the base and resets the variables
     */
    private void confirmChanges(){
        //Hide marker before changing list
        visibleMarker.setVisible(false);
        changed = true;

        //prepare new data
        PositionInfo[] updatedPlaces = new PositionInfo[pathEndIndex - pathStartIndex + 1];
        int index = 0;
        for (int j = pathStartIndex; j <= pathEndIndex; j++) {
            updatedPlaces[index] = places[j];
            index++;
        }
        //Select proper places of the new path
        places = updatedPlaces;

        //Select proper markers of the new path
        editableMarkersList = getMarkers(places, 0, places.length - 1, numberOfPoints);

        //Draw a new solid path
        Polyline newSolidPath = mMap.addPolyline(createPath(editableMarkersList, ContextCompat.getColor(this, R.color.colorOldPath)));
        path.remove();
        path = newSolidPath;

        Long startTime = pathStartMarker.getTime();
        Long endTime = pathEndMarker.getTime();

        progressDialog = ProgressDialog.show(this, "Please wait", "Database is being updated", true);
        //Save changes to database
        new Thread(() -> {
            wakeLock.acquire();
            deleteSensorData(startTime, endTime);
            wakeLock.release();
            progressDialog.dismiss();
        }).start();

        //Change start/finish dates
        changeStartFinishDates(startTime, endTime);
        //Prepare for new changes
        resetPathEditing();
    }

    /**
     * Changes dates of the route in database
     * @param startTime start time of the route
     * @param endTime finish time of the route
     */
    private void changeStartFinishDates(Long startTime, Long endTime){
        routeDataDao = daoSession.getRouteDataDao();
        List<RouteData> routeData = routeDataDao.loadAll();
        RouteData route = routeData.get(0);
        route.setStartDate(new Date(startTime));
        route.setEndDate(new Date(endTime));
        routeDataDao.update(route);
    }

    /**
     * Deletes all the sensor data which are now irrelevant
     * @param startTime new start of the route
     * @param endTime new end of the route
     */
    private void deleteSensorData(Long startTime, Long endTime){
        accelerometerDataDao = daoSession.getAccelerometerDataDao();
        ambientTemperatureDataDao = daoSession.getAmbientTemperatureDataDao();
        gyroscopeDataDao = daoSession.getGyroscopeDataDao();
        magneticFieldDataDao = daoSession.getMagneticFieldDataDao();
        rpmDataDao = daoSession.getRpmDataDao();
        speedDataDao = daoSession.getSpeedDataDao();
        throttlePositionDataDao = daoSession.getThrottlePositionDataDao();

        List<LocationData> locationData = locationDataDao.loadAll();
        if(locationData.size()>0) {
            filterDataList(locationData, startTime, endTime);
            updateDao(locationData.get(0), startTime, endTime, daoSession);
        }

        List<AccelerometerData> accelerometerData = accelerometerDataDao.loadAll();
        if(accelerometerData.size()>0) {
            filterDataList(accelerometerData, startTime, endTime);
            updateDao(accelerometerData.get(0), startTime, endTime, daoSession);
        }

        List<AmbientTemperatureData> ambientTemperatureData = ambientTemperatureDataDao.loadAll();
        if(ambientTemperatureData.size()>0) {
            filterDataList(ambientTemperatureData, startTime, endTime);
            updateDao(ambientTemperatureData.get(0), startTime, endTime, daoSession);
        }

        List<GyroscopeData> gyroscopeData = gyroscopeDataDao.loadAll();
        if(gyroscopeData.size()>0) {
            filterDataList(gyroscopeData, startTime, endTime);
            updateDao(gyroscopeData.get(0), startTime, endTime, daoSession);
        }

        List<MagneticFieldData> magneticFieldData = magneticFieldDataDao.loadAll();
        if(magneticFieldData.size()>0) {
            filterDataList(magneticFieldData, startTime, endTime);
            updateDao(magneticFieldData.get(0), startTime, endTime, daoSession);
        }

        List<RpmData> rpmData = rpmDataDao.loadAll();
        if(rpmData.size()>0) {
            filterDataList(rpmData, startTime, endTime);
            updateDao(rpmData.get(0), startTime, endTime, daoSession);
        }

        List<SpeedData> speedData = speedDataDao.loadAll();
        if(speedData.size()>0) {
            filterDataList(speedData, startTime, endTime);
            updateDao(speedData.get(0), startTime, endTime, daoSession);
        }

        List<ThrottlePositionData> throttlePositionData = throttlePositionDataDao.loadAll();
        if(throttlePositionData.size()>0) {
            filterDataList(throttlePositionData, startTime, endTime);
            updateDao(throttlePositionData.get(0), startTime, endTime, daoSession);
        }
    }

    /**
     * Deletes data from the database
     * @param data data to be filtered
     * @param startTime start time of relevant data
     * @param finishTime finish time of relevant data
     * @param session database session
     */
    private void updateDao(SensorData data, Long startTime, Long finishTime, DaoSession session){
        DeleteQuery tableDeleteQuery = data.getQuery(session, startTime, finishTime);
        tableDeleteQuery.executeDeleteWithoutDetachingEntities();
        session.clear();
    }

    /**
     * Deleting data from list which are not in start - end section
     * @param dataList list of data
     * @param startTime start time of relevant data
     * @param endTime end time of relevant data
     */
    private void filterDataList(List<? extends SensorData> dataList, long startTime, long endTime){
        ListIterator<? extends SensorData> iter;
        for(iter = dataList.listIterator(); iter.hasNext();){
            SensorData data = iter.next();
            if(data.getTimestamp()<startTime)
                iter.remove();
            else
                break;
        }

        for(iter = dataList.listIterator(dataList.size()); iter.hasPrevious();){
            SensorData data = iter.previous();
            if(data.getTimestamp()>endTime)
                iter.remove();
            else
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}

