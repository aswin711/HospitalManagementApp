package com.synnefx.cqms.event.core.modal.event.medicationerror;

import android.util.Log;

import com.google.gson.annotations.Expose;
import com.synnefx.cqms.event.core.modal.event.EventReport;
import com.synnefx.cqms.event.core.modal.event.PersonInvolved;
import com.synnefx.cqms.event.core.modal.event.ReportedBy;
import com.synnefx.cqms.event.sync.Syncable;

import java.util.Calendar;

/**
 * Created by Josekutty on 2/9/2017.
 */

public class MedicationError extends EventReport {

    @Expose
    private Integer incidentLevelCode;

    @Expose
    private String medicalReport;


    public Integer getIncidentLevelCode() {
        return incidentLevelCode;
    }

    public void setIncidentLevelCode(Integer incidentLevelCode) {
        this.incidentLevelCode = incidentLevelCode;
    }

    public String getMedicalReport() {
        return medicalReport;
    }

    public void setMedicalReport(String medicalReport) {
        this.medicalReport = medicalReport;
    }

    public MedicationError() {

    }

    public MedicationError(Long id, Long serverId, Long hospital, int statusCode, Long unitRef, String incidentNumber, String incidentLocation, Calendar updated, Calendar incidentTime, String description, String correctiveActionTaken, PersonInvolved personInvolved, ReportedBy reportedBy, Integer incidentLevelCode, String medicalReport) {
        super(id, serverId, hospital, statusCode, unitRef, incidentNumber, incidentLocation, updated, incidentTime, description, correctiveActionTaken, personInvolved, reportedBy);
        this.incidentLevelCode = incidentLevelCode;
        this.medicalReport = medicalReport;
    }

    // copy
    public MedicationError copy() {
        MedicationError result = new MedicationError(this.getId(), this.getServerId(), this.getHospital(), this.getStatusCode(), this.getUnitRef(), this.getIncidentNumber(), this.getIncidentLocation(), this.getUpdated(), this.getIncidentTime(), this.getDescription(), this.getCorrectiveActionTaken(), this.getPersonInvolved(), this.getReportedBy(), this.incidentLevelCode, this.medicalReport);
        return result;
    }

    @Override
    public void mapFromRemote(Syncable remote) {
        super.mapFromRemote(remote);
        MedicationError remoteReport = (MedicationError) remote;
        setIncidentLevelCode(remoteReport.getIncidentLevelCode());
        setMedicalReport(remoteReport.getMedicalReport());
    }

    @Override
    public void mapFromLocal(Syncable local) {
        super.mapFromLocal(local);
        MedicationError localReport = (MedicationError) local;
        Log.e("AdverseDrugEvent", "mapFromLocal" + localReport);

        setIncidentLevelCode(localReport.getIncidentLevelCode());
        setMedicalReport(localReport.getMedicalReport());
    }

}
