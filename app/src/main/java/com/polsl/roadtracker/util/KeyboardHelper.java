package com.polsl.roadtracker.util;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.polsl.roadtracker.activity.LoginActivity;

/**
 * Created by alachman on 30.03.2017.
 */

public class KeyboardHelper {
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

    public static void setupUI(View view, Activity activity) {
        if (!(view instanceof EditText)) {
            view.setOnTouchListener((v, event) -> {
                KeyboardHelper.hideSoftKeyboard(activity);
                return false;
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView, activity);
            }
        }
    }

}
