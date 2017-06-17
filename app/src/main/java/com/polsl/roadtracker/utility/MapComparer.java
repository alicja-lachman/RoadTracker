package com.polsl.roadtracker.utility;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by Wojtek on 01.05.2017.
 */

public class MapComparer {
    public static boolean Compare(Marker first, Marker second) {
        return Compare(first.getPosition(), second.getPosition());
    }

    public static boolean Compare(LatLng first, LatLng second) {
        return (first.latitude == second.latitude && first.longitude == second.longitude);
    }
}
