package com.xose.cqms.event.core.service;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

/**
 * Created by Josekutty on 8/16/2016.
 */
public class GCMRequest implements Serializable {

    @Expose
    private String appID;
    @Expose
    private String platform;
    @Expose
    private String key;
    @Expose
    private Long hospitalID;

    public GCMRequest(String platform, String key) {
        this.platform = platform;
        this.key = key;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setHospitalID(Long hospitalID) {
        this.hospitalID = hospitalID;
    }

    public String getAppID() {
        return appID;
    }

    public void setAppID(String appID) {
        this.appID = appID;
    }

    public String getPlatform() {
        return platform;
    }

    public String getKey() {
        return key;
    }

    public Long getHospitalID() {
        return hospitalID;
    }
}
