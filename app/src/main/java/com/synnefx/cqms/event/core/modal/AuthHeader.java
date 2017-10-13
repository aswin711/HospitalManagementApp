package com.synnefx.cqms.event.core.modal;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

public class AuthHeader implements Serializable {

    private static final long serialVersionUID = 1L;

    public AuthHeader() {
    }

    @Expose
    private String userId;

    @Expose
    private String token;

    @Expose
    private String deviceID;

    @Expose
    private Integer platform;

    @Expose
    private Integer appCode;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Integer getPlatform() {
        return platform;
    }

    public void setPlatform(Integer platform) {
        this.platform = platform;
    }

    public Integer getAppCode() {
        return appCode;
    }

    public void setAppCode(Integer appCode) {
        this.appCode = appCode;
    }
}
