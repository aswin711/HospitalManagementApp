package com.synnefx.cqms.event.core.modal.event.incident;

import android.util.Log;

import com.google.gson.annotations.Expose;
import com.synnefx.cqms.event.core.modal.IncidentType;
import com.synnefx.cqms.event.core.modal.event.EventReport;
import com.synnefx.cqms.event.core.modal.event.PersonInvolved;
import com.synnefx.cqms.event.core.modal.event.ReportedBy;
import com.synnefx.cqms.event.sync.Syncable;

import java.util.Calendar;

/**
 * Created by Josekutty on 2/9/2017.
 */

public class IncidentReport extends EventReport {

    @Expose
    private IncidentType incidentType;

    private Long incidentTypeRef;

    private String incidentTypeName;

    @Expose
    private Integer incidentLevelCode;

    @Expose
    private String medicalReport;

    public IncidentType getIncidentType() {
        return incidentType;
    }

    public void setIncidentType(IncidentType incidentType) {
        this.incidentType = incidentType;
    }

    public void setIncidentType(Long serverId) {
        this.incidentType = new IncidentType();
        this.incidentType.setId(serverId);
        this.incidentType.setServerId(serverId);
    }


    public Long getIncidentTypeRef() {
        return incidentTypeRef;
    }

    public void setIncidentTypeRef(Long incidentTypeRef) {
        this.incidentTypeRef = incidentTypeRef;
    }

    public Integer getIncidentLevelCode() {
        return incidentLevelCode;
    }

    public void setIncidentLevelCode(Integer incidentLevelCode) {
        this.incidentLevelCode = incidentLevelCode;
    }

    public String getIncidentTypeName() {
        return incidentTypeName;
    }

    public void setIncidentTypeName(String incidentTypeName) {
        this.incidentTypeName = incidentTypeName;
    }

    public String getMedicalReport() {
        return medicalReport;
    }

    public void setMedicalReport(String medicalReport) {
        this.medicalReport = medicalReport;
    }

    public IncidentReport() {

    }

    public IncidentReport(Long id, Long serverId, Long hospital, int statusCode, Long unitRef, String incidentNumber, String incidentLocation, Calendar updated, Calendar incidentTime, String description, String correctiveActionTaken, PersonInvolved personInvolved, ReportedBy reportedBy, Long incidentTypeRef, Integer incidentLevelCode, String medicalReport) {
        super(id, serverId, hospital, statusCode, unitRef, incidentNumber, incidentLocation, updated, incidentTime, description, correctiveActionTaken, personInvolved, reportedBy);
        this.incidentTypeRef = incidentTypeRef;
        this.incidentLevelCode = incidentLevelCode;
        this.medicalReport = medicalReport;
    }

    // copy
    public EventReport copy() {
        IncidentReport result = new IncidentReport(this.getId(), this.getServerId(), this.getHospital(), this.getStatusCode(), this.getUnitRef(), this.getIncidentNumber(), this.getIncidentLocation(), this.getUpdated(), this.getIncidentTime(), this.getDescription(), this.getCorrectiveActionTaken(), this.getPersonInvolved(), this.getReportedBy(), this.incidentTypeRef, this.incidentLevelCode, this.medicalReport);
        return result;
    }


    @Override
    public void mapFromRemote(Syncable remote) {
        super.mapFromRemote(remote);
        IncidentReport remoteReport = (IncidentReport) remote;
        setIncidentTypeRef(remoteReport.getIncidentTypeRef());
        setIncidentLevelCode(remoteReport.getIncidentLevelCode());
        setMedicalReport(remoteReport.getMedicalReport());
    }

    @Override
    public void mapFromLocal(Syncable local) {
        super.mapFromLocal(local);
        IncidentReport localReport = (IncidentReport) local;
        Log.e("Incident", "mapFromLocal" + localReport);

        setIncidentTypeRef(localReport.getIncidentTypeRef());
        setIncidentType(localReport.getIncidentTypeRef());
        setIncidentLevelCode(localReport.getIncidentLevelCode());
        setMedicalReport(localReport.getMedicalReport());
    }


}
