package com.synnefx.cqms.event.core.modal;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Josekutty on 1/24/2017.
 */
public class IncidentType implements Serializable {

    @Expose
    private Long id;

    @SerializedName("serverId")
    @Expose
    private Long serverId;


    @SerializedName("incidentType")
    @Expose
    private String incidentType;

    @SerializedName("hospitalID")
    @Expose
    private Long hospitalID;

    @Expose
    private Integer statusCode;

    @SerializedName("sortOrder")
    private Integer sortOrder = 0;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Long getServerId() {
        return serverId;
    }

    public void setServerId(Long serverId) {
        this.serverId = serverId;
    }

    public Long getHospitalID() {
        return hospitalID;
    }

    public void setHospitalID(Long hospitalID) {
        this.hospitalID = hospitalID;
    }

    public String getIncidentType() {
        return incidentType;
    }

    public void setIncidentType(String incidentType) {
        this.incidentType = incidentType;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IncidentType)) return false;

        IncidentType that = (IncidentType) o;

        if (getServerId() != null ? !getServerId().equals(that.getServerId()) : that.getServerId() != null)
            return false;
        return !(getHospitalID() != null ? !getHospitalID().equals(that.getHospitalID()) : that.getHospitalID() != null);

    }

    @Override
    public int hashCode() {
        int result = getServerId() != null ? getServerId().hashCode() : 0;
        result = 31 * result + (getHospitalID() != null ? getHospitalID().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return this.incidentType;
    }
}
