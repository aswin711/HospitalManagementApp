package com.synnefx.cqms.event.core.modal.event;

import android.util.Log;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.synnefx.cqms.event.core.modal.Unit;
import com.synnefx.cqms.event.core.modal.event.incident.IncidentReport;
import com.synnefx.cqms.event.sync.Syncable;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by Josekutty on 2/9/2017.
 */
public class EventReport implements Serializable, Syncable {


    @SerializedName("id")
    @Expose
    private Long id;

    @SerializedName("serverId")
    @Expose
    private Long serverId;

    private String hospital;

    @SerializedName("statusCode")
    @Expose
    private int statusCode;

    @Expose
    private String incidentNumber;

    @SerializedName("time")
    @Expose
    private Calendar incidentTime;

    @Expose
    private String incidentLocation;

    @Expose
    private String description;

    @Expose
    private String correctiveActionTaken;

    @Expose
    private Unit unit = new Unit();

    private Long unitRef;

    private String department;

    @Expose
    private PersonInvolved personInvolved;

    private Long personInvolvedRef;

    @Expose
    private ReportedBy reportedBy;

    private Long reportedByRef;

    @Expose
    private Calendar createdOn;

    @Expose
    private Calendar updated;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getServerId() {
        return serverId;
    }

    public void setServerId(Long serverId) {
        this.serverId = serverId;
    }

    public String getHospital() {
        return hospital;
    }

    public void setHospital(String hospital) {
        this.hospital = hospital;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getIncidentNumber() {
        return incidentNumber;
    }

    public void setIncidentNumber(String incidentNumber) {
        this.incidentNumber = incidentNumber;
    }

    public Calendar getIncidentTime() {
        return incidentTime;
    }

    public void setIncidentTime(Calendar incidentTime) {
        this.incidentTime = incidentTime;
    }

    public String getIncidentLocation() {
        return incidentLocation;
    }

    public void setIncidentLocation(String incidentLocation) {
        this.incidentLocation = incidentLocation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCorrectiveActionTaken() {
        return correctiveActionTaken;
    }

    public void setCorrectiveActionTaken(String correctiveActionTaken) {
        this.correctiveActionTaken = correctiveActionTaken;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public void setUnit(Long serverId) {
        this.unit = new Unit();
        this.unit.setId(serverId);
        this.unit.setServerId(serverId);
    }

    public Long getUnitRef() {
        return unitRef;
    }

    public void setUnitRef(Long unitRef) {
        this.unitRef = unitRef;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public PersonInvolved getPersonInvolved() {
        return personInvolved;
    }

    public void setPersonInvolved(PersonInvolved personInvolved) {
        this.personInvolved = personInvolved;
    }

    public ReportedBy getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(ReportedBy reportedBy) {
        this.reportedBy = reportedBy;
    }

    public Long getPersonInvolvedRef() {
        return personInvolvedRef;
    }

    public void setPersonInvolvedRef(Long personInvolvedRef) {
        this.personInvolvedRef = personInvolvedRef;
    }

    public Long getReportedByRef() {
        return reportedByRef;
    }

    public void setReportedByRef(Long reportedByRef) {
        this.reportedByRef = reportedByRef;
    }

    public Calendar getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Calendar createdOn) {
        this.createdOn = createdOn;
    }

    public Calendar getUpdated() {
        return updated;
    }

    public void setUpdated(Calendar updated) {
        this.updated = updated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IncidentReport)) return false;

        EventReport that = (EventReport) o;

        if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) return false;
        if (getServerId() != null ? !getServerId().equals(that.getServerId()) : that.getServerId() != null)
            return false;
        return !(getHospital() != null ? !getHospital().equals(that.getHospital()) : that.getHospital() != null);

    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getServerId() != null ? getServerId().hashCode() : 0);
        result = 31 * result + (getHospital() != null ? getHospital().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "IncidentReportKey{" +
                "serverId=" + serverId +
                ", incidentNumber=" + incidentNumber +
                ", department='" + department + '\'' +
                ", id=" + id +
                '}';
    }

    public EventReport() {

    }

    public EventReport(Long id, Long serverId, String hospital, int statusCode, Long unitRef, String incidentNumber, String incidentLocation, Calendar updated, Calendar incidentTime, String description, String correctiveActionTaken, PersonInvolved personInvolved, ReportedBy reportedBy) {
        this.id = id;
        this.serverId = serverId;
        this.hospital = hospital;
        this.incidentNumber = incidentNumber;
        this.incidentLocation = incidentLocation;
        this.description = description;
        this.unitRef = unitRef;
        this.correctiveActionTaken = correctiveActionTaken;
        this.personInvolved = personInvolved;
        this.statusCode = statusCode;
        this.updated = updated;
        this.incidentTime = incidentTime;
        this.reportedBy = reportedBy;
    }

    // copy
    public EventReport copy() {
        EventReport result = new EventReport(this.id, this.serverId, this.hospital, this.statusCode, this.unitRef, this.incidentNumber, this.incidentLocation, this.updated, this.incidentTime, this.description, this.correctiveActionTaken, this.personInvolved, this.reportedBy);
        return result;
    }

    // Implement Syncable...
    @Override
    public Long getRemoteId() {
        return serverId;
    }

    @Override
    public void setRemoteId(Long id) {
        this.serverId = id;
    }

    @Override
    public Long getLastUpdatedSequence() {
        return null != updated ? updated.getTimeInMillis() : 0l;
    }

    @Override
    public void setLastUpdatedSequence(Long value) {
        this.updated = Calendar.getInstance();
        this.updated.setTimeInMillis(value);
    }

    @Override
    public void mapFromRemote(Syncable remote) {
        EventReport remoteReport = (EventReport) remote;
        setId(remoteReport.getId());
        setRemoteId(remoteReport.getRemoteId());
        setLastUpdatedSequence(remoteReport.getLastUpdatedSequence());
        setHospital(remoteReport.getHospital());
        setStatusCode(remoteReport.getStatusCode());

        setIncidentNumber(remoteReport.getIncidentNumber());
        setUnitRef(remoteReport.getUnitRef());
        setIncidentLocation(remoteReport.getIncidentLocation());
        setIncidentTime(remoteReport.getIncidentTime());
        setDescription(remoteReport.getDescription());
        setCorrectiveActionTaken(remoteReport.getCorrectiveActionTaken());
        setReportedBy(remoteReport.getReportedBy());
        setPersonInvolved(remoteReport.getPersonInvolved());
    }

    @Override
    public void mapFromLocal(Syncable local) {
        EventReport localReport = (EventReport) local;
        Log.e("Audit", "mapFromLocal" + localReport);
        setId(localReport.getId());
        setRemoteId(localReport.getRemoteId());
        setLastUpdatedSequence(localReport.getLastUpdatedSequence());
        setHospital(localReport.getHospital());
        setStatusCode(localReport.getStatusCode());
        setIncidentNumber(localReport.getIncidentNumber());
        setIncidentNumber(localReport.getIncidentNumber());
        setUnitRef(localReport.getUnitRef());
        setUnit(localReport.getUnitRef());

        setIncidentLocation(localReport.getIncidentLocation());
        setIncidentTime(localReport.getIncidentTime());
        setDescription(localReport.getDescription());
        setCorrectiveActionTaken(localReport.getCorrectiveActionTaken());
        setReportedBy(localReport.getReportedBy());
        setPersonInvolved(localReport.getPersonInvolved());

    }


    public boolean canEdit() {
        return 1 == this.getStatusCode() || 0 == this.getStatusCode();
    }

    public boolean canDelete() {
        return 3 != this.getStatusCode();
    }

}
