package com.polsl.roadtracker.util;

import android.util.Base64;

import java.io.UnsupportedEncodingException;

import timber.log.Timber;

/**
 * Created by alachman on 18.05.2017.
 */

public class PasswordEncoder {
    public static String encodePassword(String password){
        try {
            byte[] data = password.getBytes("UTF-8");
            return Base64.encodeToString(data, Base64.DEFAULT);
        }catch (UnsupportedEncodingException e) {
            Timber.e(e.getMessage());
            return null;
        }
    }
}
