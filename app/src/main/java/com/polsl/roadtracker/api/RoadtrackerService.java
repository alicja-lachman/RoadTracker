package com.polsl.roadtracker.api;

import android.content.Context;

import com.polsl.roadtracker.model.Credentials;
import com.polsl.roadtracker.model.LogoutData;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import rx.functions.Action1;
import timber.log.Timber;

/**
 * Class used for communicating with server.
 * Created by alachman on 23.04.2017.
 */

public class RoadtrackerService {
    /**
     * Instance of Retrofit connection.
     */
    private Retrofit apiConnection;
    /**
     * Description of available endpoints.
     */
    private RoadtrackerEndpoint apiService;

    /**
     * Creating service class.
     *
     * @param context
     */
    public RoadtrackerService(Context context) {
        apiConnection = new ApiConnection(context).getRetrofitInstance();
        apiService = apiConnection.create(RoadtrackerEndpoint.class);
    }

    /**
     * Method used for registering user on server.
     *
     * @param credentials
     * @return
     */
    public Observable<AuthResponse> register(Credentials credentials) {
        return apiService.registerUser(credentials);
    }

    /**
     * Method used for logging in to server.
     *
     * @param credentials
     * @return
     */
    public Observable<AuthResponse> login(Credentials credentials) {
        return apiService.login(credentials);

    }

    /**
     * Method used for logging out.
     *
     * @param logoutData
     * @param afterCall
     * @return
     */
    public Observable logout(LogoutData logoutData, Action1<BasicResponse> afterCall) {
        Observable call = apiService.logout(logoutData);
        return callInNewThread(call, afterCall);
    }

    /**
     * Method used for getting sensor settings.
     *
     * @param authToken
     * @param afterCall
     * @return
     */
    public Observable getSensorSettings(String authToken, Action1<SensorSettingsResponse> afterCall) {
        Observable call = apiService.getSensorSettings(authToken);
        return callInNewThread(call, afterCall);
    }

    /**
     * Method used for sending part of database file to server.
     *
     * @param data
     * @return
     */
    public Observable<BasicResponse> sendRoutePartData(RoutePartData data) {
        return apiService.sendRouteData(data);
    }

    /**
     * Method used for communicating with server on separate thread.
     *
     * @param apiCall
     * @param onSuccess
     * @param <T>
     * @return
     */
    private <T> Observable<T> callInNewThread(Observable<T> apiCall, Action1<T> onSuccess) {
        Observable<T> call = apiCall
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        call.subscribe(response -> {

                    if (onSuccess == null)
                        return;
                    onSuccess.call(response);
                }, throwable ->
                        Timber.d(throwable.getMessage())

        );

        call.onErrorResumeNext(Observable.empty());
        return call;
    }

}
