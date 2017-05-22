package com.polsl.roadtracker.util;

import android.util.Base64;

import java.io.UnsupportedEncodingException;

import timber.log.Timber;

/**
 * Created by alachman on 18.05.2017.
 */

public class Base64Encoder {
    public static String encodeData(String data){
        try {
            byte[] byteData = data.getBytes("UTF-8");
            return Base64.encodeToString(byteData, Base64.DEFAULT);
        }catch (UnsupportedEncodingException e) {
            Timber.e(e.getMessage());
            return null;
        }
    }

}
