package com.polsl.roadtracker.api;

import com.polsl.roadtracker.model.SensorSettings;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by alachman on 23.04.2017.
 */

public interface RoadtrackerEndpoint {
    @POST("/api/users")
    Observable<Long> registerUser(@Query("email") String email, @Query("password") String password);

    @POST("/api/users/login")
    Observable<Long> login(@Query("email") String email, @Query("password") String password);

    @GET("api/users/{id}/settings")
    Observable<SensorSettings> getSensorSettings(@Path("id") Long userId);
}
