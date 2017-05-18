package com.polsl.roadtracker.api;

import com.polsl.roadtracker.model.SensorSettings;

/**
 * Created by alachman on 18.05.2017.
 */

public class SensorSettingsResponse extends BasicResponse {

    private SensorSettings sensorSettings;

    public SensorSettingsResponse(SensorSettings sensorSettings, String result) {
        super(result);
        this.sensorSettings = sensorSettings;

    }

    public SensorSettingsResponse() {

    }

    public SensorSettingsResponse(String result, String reason) {
        super(result, reason);
    }

    public SensorSettings getSensorSettings() {
        return sensorSettings;
    }

    public void setSensorSettings(SensorSettings sensorSettings) {
        this.sensorSettings = sensorSettings;
    }


}