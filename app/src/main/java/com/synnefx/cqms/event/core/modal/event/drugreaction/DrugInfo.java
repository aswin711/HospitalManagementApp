package com.synnefx.cqms.event.core.modal.event.drugreaction;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by Josekutty on 2/28/2017.
 */
public class DrugInfo implements Serializable {

    @Expose
    private Long id;

    private Long eventRef;

    @Expose
    private String drug;

    @Expose
    private String dose;

    @Expose
    private String frequency;

    @Expose
    private String route;

    @Expose
    private Calendar dateStarted;

    private String dateStartedStr;

    @Expose
    private Calendar dateCeased;

    private String dateCeasedStr;

    private boolean isSuspectedDrug;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEventRef() {
        return eventRef;
    }

    public void setEventRef(Long eventRef) {
        this.eventRef = eventRef;
    }

    public String getDrug() {
        return drug;
    }

    public void setDrug(String drug) {
        this.drug = drug;
    }

    public String getDose() {
        return dose;
    }

    public void setDose(String dose) {
        this.dose = dose;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public Calendar getDateStarted() {
        return dateStarted;
    }

    public void setDateStarted(Calendar dateStarted) {
        this.dateStarted = dateStarted;
    }

    public String getDateStartedStr() {
        return dateStartedStr;
    }

    public void setDateStartedStr(String dateStartedStr) {
        this.dateStartedStr = dateStartedStr;
    }

    public Calendar getDateCeased() {
        return dateCeased;
    }

    public void setDateCeased(Calendar dateCeased) {
        this.dateCeased = dateCeased;
    }

    public String getDateCeasedStr() {
        return dateCeasedStr;
    }

    public void setDateCeasedStr(String dateCeasedStr) {
        this.dateCeasedStr = dateCeasedStr;
    }

    public boolean isSuspectedDrug() {
        return isSuspectedDrug;
    }

    public void setIsSuspectedDrug(boolean isSuspectedDrug) {
        this.isSuspectedDrug = isSuspectedDrug;
    }
}
