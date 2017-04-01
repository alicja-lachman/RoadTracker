package com.polsl.roadtracker.utility;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

/**
 * Created by Wojciech Paczuła on 28.03.2017.
 * Class describing single position on map
 */
public class PositionInfo {
    /**
     * Position from GPS
     */
    private LatLng coordinate;
    /**
     * Time of the data collection
     */
    private Date date;

    public LatLng getCooridinate(){
        return coordinate;
    }
    public Date getDate(){
        return date;
    }
    public PositionInfo(double latitude, double longitude, Date time){
        coordinate = new LatLng(latitude, longitude);
        date = time;
    }
    public PositionInfo(LatLng coord, Date time){
        coordinate = coord;
        date = time;
    }
}
