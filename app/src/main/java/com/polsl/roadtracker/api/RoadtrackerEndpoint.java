package com.polsl.roadtracker.api;

import com.polsl.roadtracker.model.Credentials;
import com.polsl.roadtracker.model.LogoutData;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Created by alachman on 23.04.2017.
 */

public interface RoadtrackerEndpoint {
    @PUT("/api/users/auth")
    Observable<AuthResponse> registerUser(@Body Credentials credentials);

    @POST("/api/users/auth")
    Observable<AuthResponse> login(@Body Credentials credentials);

    @GET("/api/users/intervals/{authToken}")
    Observable<SensorSettingsResponse> getSensorSettings(@Path("authToken") String authToken);

    @POST("/api/users/readings")
    Observable<BasicResponse> sendRouteData(@Body RoutePartData routePartData);

    @POST("/api/users/auth/logout")
    Observable<BasicResponse> logout(@Body LogoutData logoutData);
}
