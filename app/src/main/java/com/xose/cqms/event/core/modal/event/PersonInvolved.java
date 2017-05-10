package com.xose.cqms.event.core.modal.event;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by Josekutty on 2/9/2017.
 */
public class PersonInvolved implements Serializable {

    @Expose
    private Long id;

    private Long eventRef;

    @Expose
    private Integer personnelTypeCode;

    @Expose
    private String name;

    @Expose
    private String designation;

    @Expose
    private String staffId;

    @Expose
    private String hospitalNumber;

    @Expose
    private Integer genderCode;

    @Expose
    private Calendar dateOfBirthIndividual;

    @Expose
    private Integer patientTypeCode;

    @Expose
    private Double height;

    @Expose
    private Double weight;

    @Expose
    private String consultantName;

    @Expose
    private String diagnosis;

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

    public String getConsultantName() {
        return consultantName;
    }

    public void setConsultantName(String consultantName) {
        this.consultantName = consultantName;
    }

    public Calendar getDateOfBirthIndividual() {
        return dateOfBirthIndividual;
    }

    public void setDateOfBirthIndividual(Calendar dateOfBirthIndividual) {
        this.dateOfBirthIndividual = dateOfBirthIndividual;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public Integer getGenderCode() {
        return genderCode;
    }

    public void setGenderCode(Integer genderCode) {
        this.genderCode = genderCode;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public String getHospitalNumber() {
        return hospitalNumber;
    }

    public void setHospitalNumber(String hospitalNumber) {
        this.hospitalNumber = hospitalNumber;
    }


    public Integer getPatientTypeCode() {
        return patientTypeCode;
    }

    public void setPatientTypeCode(Integer patientTypeCode) {
        this.patientTypeCode = patientTypeCode;
    }

    public Integer getPersonnelTypeCode() {
        return personnelTypeCode;
    }

    public void setPersonnelTypeCode(Integer personnelTypeCode) {
        this.personnelTypeCode = personnelTypeCode;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Long getEventRef() {
        return eventRef;
    }

    public void setEventRef(Long eventRef) {
        this.eventRef = eventRef;
    }
}
