package com.xose.cqms.event.core.modal;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Josekutty on 7/13/2016.
 */
public class Unit implements Serializable {

    @Expose
    private Long id;

    @SerializedName("name")
    @Expose
    private String name;

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
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getHospitalID() {
        return hospitalID;
    }

    public void setHospitalID(Long hospitalID) {
        this.hospitalID = hospitalID;
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
        if (!(o instanceof Unit)) return false;
        Unit unit = (Unit) o;
        if (getHospitalID() != null ? !getHospitalID().equals(unit.getHospitalID()) : unit.getHospitalID() != null)
            return false;
        return !(getServerId() != null ? !getServerId().equals(unit.getServerId()) : unit.getServerId() != null);
    }

    @Override
    public int hashCode() {
        int result = getHospitalID() != null ? getHospitalID().hashCode() : 0;
        result = 31 * result + (getServerId() != null ? getServerId().hashCode() : 0);
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
        return name;
    }
}
