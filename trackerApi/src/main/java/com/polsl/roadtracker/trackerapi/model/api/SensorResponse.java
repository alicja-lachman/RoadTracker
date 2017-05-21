/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.polsl.roadtracker.trackerapi.model.api;

import com.polsl.roadtracker.trackerapi.model.SensorSettings;

/**
 *
 * @author alachman
 */
public class SensorResponse extends BasicResponse {

    private SensorSettings sensorSettings;

    public SensorResponse(SensorSettings sensorSettings, String apiResult) {
        super(apiResult);
        this.sensorSettings = sensorSettings;

    }

    public SensorResponse() {

    }

    public SensorResponse(String apiResult, String reason) {
        super(apiResult, reason);
    }

    public SensorSettings getSensorSettings() {
        return sensorSettings;
    }

    public void setSensorSettings(SensorSettings sensorSettings) {
        this.sensorSettings = sensorSettings;
    }



}
