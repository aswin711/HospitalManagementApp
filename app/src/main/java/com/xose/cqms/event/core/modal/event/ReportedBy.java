package com.xose.cqms.event.core.modal.event;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by Josekutty on 2/9/2017.
 */
public class ReportedBy implements Serializable {

    @Expose
    private Long id;

    private Long eventRef;

    @Expose
    private String firstName;

    @Expose
    private String lastName;

    @Expose
    private String designation;

    @Expose
    private String department;

    @Expose
    private Calendar reportedOn;

    @Expose
    private String fullName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Calendar getReportedOn() {
        return reportedOn;
    }

    public void setReportedOn(Calendar reportedOn) {
        this.reportedOn = reportedOn;
    }

    public String getFullName() {
        return String.format("%s %s", null != this.getFirstName() ? this.getFirstName() : "", this.getLastName());
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }


    public Long getEventRef() {
        return eventRef;
    }

    public void setEventRef(Long eventRef) {
        this.eventRef = eventRef;
    }
}
