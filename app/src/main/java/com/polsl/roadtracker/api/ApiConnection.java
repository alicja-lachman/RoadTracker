package com.polsl.roadtracker.api;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.polsl.roadtracker.util.Constants;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by alachman on 23.04.2017.
 */

public class ApiConnection {
    //private static String BASE_URL = "http://10.0.3.2:8085";
    private static ApiConnection instance;
    private Retrofit retrofit;

    /*
    instrukcja instalacji
    jesli chce to dac komus innemu
    jesli chce to odpalic
    wersja android studio, jakie biblioteki
    diagram klas

     */
    public ApiConnection(Context context) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder client = new OkHttpClient.Builder()
                .addInterceptor(interceptor);

        this.retrofit = new Retrofit.Builder()
                .baseUrl(getURLAddress(context))
                .addConverterFactory(buildGsonConverter())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client.build())
                .build();
    }

    private static String getURLAddress(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        return sharedPreferences.getString(Constants.URL, Constants.BASIC_URL);

    }


    private static ApiConnection getInstance(Context context) {
        if (instance == null) {
            instance = new ApiConnection(context);
        }
        return instance;
    }

    public Retrofit getRetrofitInstance() {
        return retrofit;
    }

    private GsonConverterFactory buildGsonConverter() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        return GsonConverterFactory.create(gson);
    }
}
