package com.synnefx.cqms.event.sqlite;

import android.content.Context;

import com.synnefx.cqms.event.core.modal.Unit;
import com.synnefx.cqms.event.core.modal.event.drugreaction.AdverseDrugEvent;
import com.synnefx.cqms.event.core.modal.event.incident.IncidentReport;
import com.synnefx.cqms.event.core.modal.event.medicationerror.MedicationError;

import java.util.List;

/**
 * Created by Josekutty on 8/18/2016.
 */
public class AppDao {

    private DatabaseHelper databaseHelper;

    public AppDao(Context context) {
        this.databaseHelper = DatabaseHelper.getInstance(context);
    }


    public void addUnits(List<Unit> units, String hospitalRef) {
        databaseHelper.insertOrUpdateUnits(units, hospitalRef);
    }

    public void updateUnits(List<Unit> units, String hospitalRef) {
        databaseHelper.insertOrUpdateUnits(units, hospitalRef);
    }

    public void syncUnits(List<Unit> units, String hospitalRef) {
        databaseHelper.syncUnits(units, hospitalRef);
    }

    public List<Unit> getAllUnitsTypesByStatus(String hospitalID, int statusCode) {
        return databaseHelper.getAllUnitsTypesByStatus(hospitalID, statusCode);
    }

    public List<Unit> getAllUnitsTypes(String hospitalID) {
        return databaseHelper.getAllUnitsTypes(hospitalID);
    }

    //Incident Report
    public List<IncidentReport> findAllIncidentReportByStatusForUpload(Integer statusCode) throws DataAccessException {
        return databaseHelper.getFullyLoadedIncidentReportsByStatus(statusCode, 0, 0);
    }

    public long addIncidentReport(IncidentReport report) {
        return databaseHelper.insertOrUpdateIncidentReport(report);
    }

    public IncidentReport getIncidentReportById(Long auditRef) {
        return databaseHelper.getIncidentReportById(auditRef);
    }

    public long updateIncidentReport(IncidentReport report) {
        return databaseHelper.insertOrUpdateIncidentReport(report);
    }

    public long updateIncidentReportStatus(long clientId, long serverId, int status) {
        return databaseHelper.updateIncidentReportStatus(clientId, serverId, status);
    }

    //Medication Error
    public List<MedicationError> findAllMedicationErrorByStatusForUpload(Integer statusCode) throws DataAccessException {
        return databaseHelper.getFullyLoadedMedicationErrorsByStatus(statusCode, 0, 0);
    }

    public long addIncidentReport(MedicationError report) {
        return databaseHelper.insertOrUpdateMedicationError(report);
    }

    public MedicationError getMedicationErrorById(Long auditRef) {
        return databaseHelper.getMedicationErrorById(auditRef);
    }

    public long updateMedicationError(MedicationError report) {
        return databaseHelper.insertOrUpdateMedicationError(report);
    }

    public long updateMedicationErrortStatus(long clientId, long serverId, int status) {
        return databaseHelper.updateMedicationErrorStatus(clientId, serverId, status);
    }

    //Advewrse Drug reaction

    public List<AdverseDrugEvent> findAllAdverseDrugEventsByStatusForUpload(Integer statusCode) throws DataAccessException {
        return databaseHelper.getFullyLoadedAdverseDrugEventsByStatus(statusCode, 0, 0);
    }

    public long addAdverseDrugEvent(AdverseDrugEvent report) {
        return databaseHelper.insertOrUpdateAdverseDrugReaction(report);
    }

    public AdverseDrugEvent getAdverseDrugEventById(Long auditRef) {
        return databaseHelper.getAdverseDrugEventById(auditRef);
    }

    public long updateAdverseDrugEvent(AdverseDrugEvent report) {
        return databaseHelper.insertOrUpdateAdverseDrugReaction(report);
    }

    public long updateAdverseDrugEventStatus(long clientId, long serverId, int status) {
        return databaseHelper.updateAdverseDrugEventStatus(clientId, serverId, status);
    }
}
