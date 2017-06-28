

package com.polsl.roadtracker.utility;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.Date;

/**
 * Wraper for a marker holding additional time and index in the whole array of route's data
 * Created by Wojciech Paczula on 20.05.2017.
 */
public class TimePlaceMarker implements Comparable<TimePlaceMarker>{
    /**
     * Wrapped marker
     */
    private Marker marker;
    /**
     * Time at which location data was collected
     */
    private long time;
    /**
     * Index of the data in the whole array
     */
    private int index;

    public TimePlaceMarker(Marker marker, long time, int index){
        this.marker = marker;
        this.time = time;
        this.index = index;
    }

    public Date getDate(){
        return new Date(time);
    }

    public int getIndex(){
        return index;
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

    /**
     * Comparable interface implementation
     */
    @Override
    public int compareTo(@NonNull TimePlaceMarker o) {
        return index<=o.getIndex() ? -1 : 1;
    }
}
