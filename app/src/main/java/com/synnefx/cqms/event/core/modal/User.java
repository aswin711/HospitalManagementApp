package com.synnefx.cqms.event.core.modal;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class User implements Serializable {

    @SerializedName("associatedHospital")
    @Expose
    private Long associatedHospital;
    @SerializedName("associatedHospitalName")
    @Expose
    private String associatedHospitalName;
    @SerializedName("tenantId")
    @Expose
    private Long tenantId;
    @SerializedName("updatedBy")
    @Expose
    private String updatedBy;
    @SerializedName("updatedByID")
    @Expose
    private Long updatedByID;
    @SerializedName("updatedDate")
    @Expose
    private Long updatedDate;
    @SerializedName("dateLocked")
    @Expose
    private Object dateLocked;
    @SerializedName("firstName")
    @Expose
    private String firstName;
    @SerializedName("fullName")
    @Expose
    private String fullName;
    @SerializedName("lastName")
    @Expose
    private String lastName;
    @SerializedName("locked")
    @Expose
    private Boolean locked;
    @SerializedName("password")
    @Expose
    private Object password;
    @SerializedName("userName")
    @Expose
    private String userName;
    @SerializedName("contact")
    @Expose

    private Contact contact;
    @SerializedName("roles")
    @Expose
    private Object roles;
    @SerializedName("id")
    @Expose
    private Long id;

    private String avatarUrl;

    /**
     * @return The associatedHospital
     */
    public Long getAssociatedHospital() {
        return associatedHospital;
    }

    /**
     * @param associatedHospital The associatedHospital
     */
    public void setAssociatedHospital(Long associatedHospital) {
        this.associatedHospital = associatedHospital;
    }

    /**
     * @return The associatedHospitalName
     */
    public String getAssociatedHospitalName() {
        return associatedHospitalName;
    }

    /**
     * @param associatedHospitalName The associatedHospitalName
     */
    public void setAssociatedHospitalName(String associatedHospitalName) {
        this.associatedHospitalName = associatedHospitalName;
    }

    /**
     * @return The tenantId
     */
    public Long getTenantId() {
        return tenantId;
    }

    /**
     * @param tenantId The tenantId
     */
    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * @return The updatedBy
     */
    public String getUpdatedBy() {
        return updatedBy;
    }

    /**
     * @param updatedBy The updatedBy
     */
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    /**
     * @return The updatedByID
     */
    public Long getUpdatedByID() {
        return updatedByID;
    }

    /**
     * @param updatedByID The updatedByID
     */
    public void setUpdatedByID(Long updatedByID) {
        this.updatedByID = updatedByID;
    }

    /**
     * @return The updatedDate
     */
    public Long getUpdatedDate() {
        return updatedDate;
    }

    /**
     * @param updatedDate The updatedDate
     */
    public void setUpdatedDate(Long updatedDate) {
        this.updatedDate = updatedDate;
    }

    /**
     * @return The dateLocked
     */
    public Object getDateLocked() {
        return dateLocked;
    }

    /**
     * @param dateLocked The dateLocked
     */
    public void setDateLocked(Object dateLocked) {
        this.dateLocked = dateLocked;
    }

    /**
     * @return The firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName The firstName
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return The fullName
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * @param fullName The fullName
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * @return The lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @param lastName The lastName
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return The locked
     */
    public Boolean getLocked() {
        return locked;
    }

    /**
     * @param locked The locked
     */
    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    /**
     * @return The password
     */
    public Object getPassword() {
        return password;
    }

    /**
     * @param password The password
     */
    public void setPassword(Object password) {
        this.password = password;
    }

    /**
     * @return The userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName The userName
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return The contact
     */
    public Contact getContact() {
        return contact;
    }

    /**
     * @param contact The contact
     */
    public void setContact(Contact contact) {
        this.contact = contact;
    }

    /**
     * @return The roles
     */
    public Object getRoles() {
        return roles;
    }

    /**
     * @param roles The roles
     */
    public void setRoles(Object roles) {
        this.roles = roles;
    }

    /**
     * @return The id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id The id
     */
    public void setId(Long id) {
        this.id = id;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
