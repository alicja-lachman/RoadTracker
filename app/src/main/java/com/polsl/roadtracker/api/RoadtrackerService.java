package com.polsl.roadtracker.api;

import com.polsl.roadtracker.model.Credentials;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import rx.functions.Action1;
import timber.log.Timber;

/**
 * Created by alachman on 23.04.2017.
 */

public class RoadtrackerService {
    private Retrofit apiConnection;
    private RoadtrackerEndpoint apiService;

    public RoadtrackerService() {
        apiConnection = new ApiConnection().getRetrofitInstance();
        apiService = apiConnection.create(RoadtrackerEndpoint.class);
    }

    public Observable register(Credentials credentials, Action1<AuthResponse> afterCall) {
        Observable call = apiService.registerUser(credentials);
        return callInNewThread(call, afterCall);
    }

    public Observable login(Credentials credentials, Action1<AuthResponse> afterCall) {
        Observable call = apiService.login(credentials);
        return callInNewThread(call, afterCall);
    }

    public Observable getSensorSettings(String authToken, Action1<SensorSettingsResponse> afterCall) {
        Observable call = apiService.getSensorSettings(authToken);
        return callInNewThread(call, afterCall);
    }

    public Observable sendRoutePartData(RoutePartData partData, Action1<BasicResponse> afterCall) {
        Observable call = apiService.sendRouteData(partData);
        return callInNewThread(call, afterCall);
    }

    private <T> Observable<T> callInNewThread(Observable<T> apiCall, Action1<T> onSuccess) {
        Observable<T> call = apiCall
                .subscribeOn(Schedulers.newThread())
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
