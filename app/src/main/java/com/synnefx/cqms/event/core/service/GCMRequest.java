package com.synnefx.cqms.event.core.service;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

/**
 * Created by Josekutty on 8/16/2016.
 */
public class GCMRequest implements Serializable {


    @Expose
    private String key;

    @Expose
    private String deviceToken;

    @Expose
    private String hospitalID;

    public GCMRequest(String deviceToken, String key) {
        this.deviceToken = deviceToken;
        this.key = key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getHospitalID() {
        return hospitalID;
    }

    public void setHospitalID(String hospitalID) {
        this.hospitalID = hospitalID;
    }
}
