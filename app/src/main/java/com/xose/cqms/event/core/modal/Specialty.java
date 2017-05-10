package com.xose.cqms.event.core.modal;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Josekutty on 7/13/2016.
 */
public class Specialty implements Serializable {


    @Expose
    private Long id;

    @SerializedName("specialityName")
    @Expose
    private String specialityName;

    @SerializedName("hospitalID")
    @Expose
    private Long hospitalID;

    @SerializedName("serverId")
    @Expose
    private Long serverId;

    @Expose
    private Integer statusCode;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return specialityName;
    }

    public void setName(String specialityName) {
        this.specialityName = specialityName;
    }

    public Long getHospitalID() {
        return hospitalID;
    }

    public void setHospitalID(Long hospitalID) {
        this.hospitalID = hospitalID;
    }

    public String getSpecialityName() {
        return specialityName;
    }

    public void setSpecialityName(String specialityName) {
        this.specialityName = specialityName;
    }

    public Long getServerId() {
        return serverId;
    }

    public void setServerId(Long serverId) {
        this.serverId = serverId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Specialty)) return false;

        Specialty that = (Specialty) o;

        if (!getHospitalID().equals(that.getHospitalID())) return false;
        return getServerId().equals(that.getServerId());

    }

    @Override
    public int hashCode() {
        int result = getHospitalID().hashCode();
        result = 31 * result + getServerId().hashCode();
        return result;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public String toString() {
        return specialityName;
    }
}
