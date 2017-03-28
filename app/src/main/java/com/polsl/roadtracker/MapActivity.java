package com.polsl.roadtracker;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

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
import com.polsl.roadtracker.utility.PositionInfo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import timber.log.Timber;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private Polyline path;
    private LatLngBounds.Builder builder;
    private CameraUpdate cameraUpdate;
    private GoogleMap mMap;
    private List<PositionInfo> places = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Timber.plant(new Timber.DebugTree());
    }

    private void setPlaces() {
        //Test value
        LatLng[] coords = {
                new LatLng(50.288584, 18.678540),
                new LatLng(50.287687, 18.677402),
                new LatLng(50.287338, 18.677356),
                new LatLng(50.286446, 18.679541),
                new LatLng(50.284900, 18.677986),
                new LatLng(50.286131, 18.675300),
                };
        Date[] times = {
                new Date(),
                new Date(),
                new Date(),
                new Date(),
                new Date(),
                new Date(),
                };
        //Build a test values hashmap
        for (int i = 0; i < coords.length; i++) {
            places.add(new PositionInfo(coords[i], times[i]));
        }
    }

    /*
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setPlaces();
        mSetUpMap();
    }


    public void mSetUpMap() {
        //clear the map before redraw to them
        mMap.clear();
        //Place markers
        createPath(places);
        //Create path
        //initialize the padding for map boundary
        int padding = 50;
        //create the bounds from latlngBuilder to set into map camera
        LatLngBounds bounds = builder.build();
        //create the camera with bounds and padding to set into map
        cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        //call the map call back to know map is loaded or not
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                //set animated zoom camera into map
                mMap.animateCamera(cameraUpdate);
            }
        });
    }

    private void createPath(List<PositionInfo> places){
        //Make date format object
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        //Create options containing points of the line
        PolylineOptions polylineOptions = new PolylineOptions().clickable(false);

        //Create dummy Markers List
        List<Marker> markersList = new ArrayList<Marker>();
        for (PositionInfo point : places) {
            //Add a marker
            Marker marker = mMap.addMarker(new MarkerOptions().position(point.getCooridinate())
                    .title(dateFormat.format(point.getDate())));
            markersList.add(marker);
            //Add a point to line
            polylineOptions.add(point.getCooridinate());
        }

        //Create path
        path = mMap.addPolyline(polylineOptions.geodesic(true));
        //TODO: Change the tag to style the line
        path.setTag("TAG");
        //create for loop for get the LatLngbuilder from the marker list
        builder = new LatLngBounds.Builder();
        for (Marker m : markersList) {
            builder.include(m.getPosition());
        }
    }
}
