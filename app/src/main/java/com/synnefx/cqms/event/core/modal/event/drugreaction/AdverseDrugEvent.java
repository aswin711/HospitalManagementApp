package com.synnefx.cqms.event.core.modal.event.drugreaction;

import android.util.Log;

import com.google.gson.annotations.Expose;
import com.synnefx.cqms.event.core.modal.event.EventReport;
import com.synnefx.cqms.event.core.modal.event.PersonInvolved;
import com.synnefx.cqms.event.core.modal.event.ReportedBy;
import com.synnefx.cqms.event.sync.Syncable;

import java.util.Calendar;
import java.util.List;

/**
 * Created by Josekutty on 2/9/2017.
 */

public class AdverseDrugEvent extends EventReport {


    @Expose
    private Calendar reactionDate;

    private String reactionDateStr;

    @Expose
    private Integer actionOutcomeCode;

    @Expose
    private DrugInfo suspectedDrug;

    @Expose
    private List<DrugInfo> otherDrugsTaken;

    @Expose
    private Calendar dateOfRecovery;

    private String dateOfRecoveryStr;

    @Expose
    private Calendar dateOfDeath;

    private String dateOfDeathStr;

    @Expose
    private boolean admittedPostReaction;

    @Expose
    private boolean reactionAddedToCasesheet;

    @Expose
    private String comments;



    public AdverseDrugEvent() {

    }

    public AdverseDrugEvent(Long id, Long serverId, Long hospital, int statusCode, Long unitRef, String incidentNumber, String incidentLocation, Calendar updated, Calendar incidentTime, String description, String correctiveActionTaken, PersonInvolved personInvolved, ReportedBy reportedBy, Integer actionOutcomeCode, DrugInfo suspectedDrug, List<DrugInfo> otherDrugsTaken, Calendar dateOfRecovery, Calendar dateOfDeath, boolean admittedPostReaction, boolean reactionAddedToCasesheet, String comments) {
        super(id, serverId, hospital, statusCode, unitRef, incidentNumber, incidentLocation, updated, incidentTime, description, correctiveActionTaken, personInvolved, reportedBy);
        this.reactionDate = incidentTime;
        this.actionOutcomeCode = actionOutcomeCode;
        this.suspectedDrug = suspectedDrug;
        this.otherDrugsTaken = otherDrugsTaken;
        this.dateOfRecovery = dateOfRecovery;
        this.dateOfDeath = dateOfDeath;
        this.admittedPostReaction = admittedPostReaction;
        this.reactionAddedToCasesheet = reactionAddedToCasesheet;
        this.comments = comments;
    }

    // copy
    public AdverseDrugEvent copy() {
        AdverseDrugEvent result = new AdverseDrugEvent(this.getId(), this.getServerId(), this.getHospital(), this.getStatusCode(), this.getUnitRef(), this.getIncidentNumber(), this.getIncidentLocation(), this.getUpdated(), this.getIncidentTime(), this.getDescription(), this.getCorrectiveActionTaken(), this.getPersonInvolved(), this.getReportedBy(), this.actionOutcomeCode, this.suspectedDrug, this.otherDrugsTaken, this.dateOfRecovery, this.dateOfDeath, this.admittedPostReaction, this.reactionAddedToCasesheet, this.comments);
        return result;
    }

    @Override
    public void mapFromRemote(Syncable remote) {
        super.mapFromRemote(remote);
        AdverseDrugEvent remoteReport = (AdverseDrugEvent) remote;
        setReactionDate(remoteReport.getReactionDate());
        setActionOutcomeCode(remoteReport.getActionOutcomeCode());
        setAdmittedPostReaction(remoteReport.isAdmittedPostReaction());
        setComments(remoteReport.getComments());
        setDateOfRecovery(remoteReport.getDateOfRecovery());
        setDateOfDeath(remoteReport.getDateOfDeath());
        setSuspectedDrug(remoteReport.getSuspectedDrug());
        setOtherDrugsTaken(remoteReport.getOtherDrugsTaken());
    }

    @Override
    public void mapFromLocal(Syncable local) {
        super.mapFromLocal(local);
        AdverseDrugEvent localReport = (AdverseDrugEvent) local;
        Log.e("AdverseDrugEvent", "mapFromLocal" + localReport);
        setReactionDate(localReport.getReactionDate());
        setActionOutcomeCode(localReport.getActionOutcomeCode());
        setAdmittedPostReaction(localReport.isAdmittedPostReaction());
        setComments(localReport.getComments());
        setDateOfRecovery(localReport.getDateOfRecovery());
        setDateOfDeath(localReport.getDateOfDeath());
        setSuspectedDrug(localReport.getSuspectedDrug());
        setOtherDrugsTaken(localReport.getOtherDrugsTaken());
    }


    public Calendar getReactionDate() {
        return reactionDate;
    }

    public void setReactionDate(Calendar reactionDate) {
        this.reactionDate = reactionDate;
    }

    public String getReactionDateStr() {
        return reactionDateStr;
    }

    public void setReactionDateStr(String reactionDateStr) {
        this.reactionDateStr = reactionDateStr;
    }

    public Integer getActionOutcomeCode() {
        return actionOutcomeCode;
    }

    public void setActionOutcomeCode(Integer actionOutcomeCode) {
        this.actionOutcomeCode = actionOutcomeCode;
    }

    public DrugInfo getSuspectedDrug() {
        return suspectedDrug;
    }

    public void setSuspectedDrug(DrugInfo suspectedDrug) {
        this.suspectedDrug = suspectedDrug;
    }

    public List<DrugInfo> getOtherDrugsTaken() {
        return otherDrugsTaken;
    }

    public void setOtherDrugsTaken(List<DrugInfo> otherDrugsTaken) {
        this.otherDrugsTaken = otherDrugsTaken;
    }

    public Calendar getDateOfRecovery() {
        return dateOfRecovery;
    }

    public void setDateOfRecovery(Calendar dateOfRecovery) {
        this.dateOfRecovery = dateOfRecovery;
    }

    public String getDateOfRecoveryStr() {
        return dateOfRecoveryStr;
    }

    public void setDateOfRecoveryStr(String dateOfRecoveryStr) {
        this.dateOfRecoveryStr = dateOfRecoveryStr;
    }

    public Calendar getDateOfDeath() {
        return dateOfDeath;
    }

    public void setDateOfDeath(Calendar dateOfDeath) {
        this.dateOfDeath = dateOfDeath;
    }

    public String getDateOfDeathStr() {
        return dateOfDeathStr;
    }

    public void setDateOfDeathStr(String dateOfDeathStr) {
        this.dateOfDeathStr = dateOfDeathStr;
    }

    public boolean isAdmittedPostReaction() {
        return admittedPostReaction;
    }

    public void setAdmittedPostReaction(boolean admittedPostReaction) {
        this.admittedPostReaction = admittedPostReaction;
    }

    public boolean isReactionAddedToCasesheet() {
        return reactionAddedToCasesheet;
    }

    public void setReactionAddedToCasesheet(boolean reactionAddedToCasesheet) {
        this.reactionAddedToCasesheet = reactionAddedToCasesheet;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

}
