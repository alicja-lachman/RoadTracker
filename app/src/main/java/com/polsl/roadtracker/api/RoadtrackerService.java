package com.polsl.roadtracker.api;

import com.polsl.roadtracker.model.SensorSettings;

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

    public Observable register(String email, String password, Action1<Long> afterCall) {
        Observable call = apiService.registerUser(email, password);
        return callInNewThread(call, afterCall);

    }

    public Observable login(String email, String password, Action1<Long> afterCall) {
        Observable call = apiService.login(email, password);
        return callInNewThread(call, afterCall);
    }

    public Observable getSensorSettings(Long id, Action1<SensorSettings> afterCall) {
        Observable call = apiService.getSensorSettings(id);
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
