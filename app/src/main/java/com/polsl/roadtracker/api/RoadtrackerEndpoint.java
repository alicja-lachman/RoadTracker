package com.polsl.roadtracker.api;

import com.polsl.roadtracker.model.Credentials;
import com.polsl.roadtracker.model.LogoutData;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by alachman on 23.04.2017.
 */

public interface RoadtrackerEndpoint {
    @PUT("/auth")
    Observable<AuthResponse> registerUser(@Body Credentials credentials);

    @POST("/auth")
    Observable<AuthResponse> login(@Body Credentials credentials);

    @GET("/intervals")
    Observable<SensorSettingsResponse> getSensorSettings(@Query("token") String authToken);

    @POST("/readings")
    Observable<BasicResponse> sendRouteData(@Body RoutePartData routePartData);

    @PATCH("/auth")
    Observable<BasicResponse> logout(@Body LogoutData logoutData);
}
