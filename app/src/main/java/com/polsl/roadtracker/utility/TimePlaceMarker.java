

package com.polsl.roadtracker.utility;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by Wojciech Paczula on 20.05.2017.
 */

public class TimePlaceMarker implements Comparable<TimePlaceMarker>{
    private Marker marker;
    private long time;
    private int index;

    public TimePlaceMarker(Marker marker, long time, int index){
        this.marker = marker;
        this.time = time;
        this.index = index;
    }

    public TimePlaceMarker(TimePlaceMarker marker){
        this.marker = marker.getMarker();
        this.time = marker.getTime();
        this.index = marker.getIndex();
    }

    public int getIndex(){
        return index;
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

    @Override
    public int compareTo(@NonNull TimePlaceMarker o) {
        return index<=o.getIndex() ? -1 : 1;
    }
}
