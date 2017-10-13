package com.synnefx.cqms.event.core.modal;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

/**
 * Created by hsrii on 10/13/2017.
 */

public class ApiAuthResponse implements Serializable {

    @Expose
    private String userToken;

    @Expose
    private String deviceToken;

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }
}
