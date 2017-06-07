package com.polsl.roadtracker.utility;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.Comparator;

/**
 * Created by Wojciech Paczula on 20.05.2017.
 */

public class TimePlaceMarker{
    private Marker marker;
    private long time;

    public TimePlaceMarker(Marker marker, long time){
        this.marker = marker;
        this.time = time;
    }

    public TimePlaceMarker(TimePlaceMarker marker){
        this.marker = marker.getMarker();
        this.time = marker.getTime();
    }

    private Marker getMarker(){
        return marker;
    }

    public long getTime(){
        return time;
    }

    public boolean isEqualWith(TimePlaceMarker compared){
        return time==compared.getTime();
    }

    public boolean isEqualWith(long comparedTime){
        return time==comparedTime;
    }

    public boolean isBefore(TimePlaceMarker compared){
        return time<compared.getTime();
    }

    public LatLng getPosition(){
        return marker.getPosition();
    }

    public void setVisible(boolean value){
        marker.setVisible(value);
    }

    public void showInfoWindow(){
        marker.showInfoWindow();
    }
}