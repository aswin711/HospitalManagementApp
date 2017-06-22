package com.synnefx.cqms.event.core.modal;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;


public class Contact implements Serializable {

    @SerializedName("tenantID")
    @Expose
    private Long tenantID;
    @SerializedName("addressLine1")
    @Expose
    private Object addressLine1;
    @SerializedName("addressLine2")
    @Expose
    private Object addressLine2;
    @SerializedName("addressLine3")
    @Expose
    private Object addressLine3;
    @SerializedName("addressType")
    @Expose
    private Object addressType;
    @SerializedName("city")
    @Expose
    private Object city;
    @SerializedName("countryCode")
    @Expose
    private Object countryCode;
    @SerializedName("district")
    @Expose
    private Object district;
    @SerializedName("emailID")
    @Expose
    private String emailID;
    @SerializedName("firstName")
    @Expose
    private Object firstName;
    @SerializedName("lastName")
    @Expose
    private Object lastName;
    @SerializedName("middleName")
    @Expose
    private Object middleName;
    @SerializedName("phoneNumber")
    @Expose
    private String phoneNumber;
    @SerializedName("phoneType")
    @Expose
    private String phoneType;
    @SerializedName("pinCode")
    @Expose
    private Long pinCode;
    @SerializedName("state")
    @Expose
    private Object state;
    @SerializedName("id")
    @Expose
    private Long id;

    /**
     * @return The tenantID
     */
    public Long getTenantID() {
        return tenantID;
    }

    /**
     * @param tenantID The tenantID
     */
    public void setTenantID(Long tenantID) {
        this.tenantID = tenantID;
    }

    /**
     * @return The addressLine1
     */
    public Object getAddressLine1() {
        return addressLine1;
    }

    /**
     * @param addressLine1 The addressLine1
     */
    public void setAddressLine1(Object addressLine1) {
        this.addressLine1 = addressLine1;
    }

    /**
     * @return The addressLine2
     */
    public Object getAddressLine2() {
        return addressLine2;
    }

    /**
     * @param addressLine2 The addressLine2
     */
    public void setAddressLine2(Object addressLine2) {
        this.addressLine2 = addressLine2;
    }

    /**
     * @return The addressLine3
     */
    public Object getAddressLine3() {
        return addressLine3;
    }

    /**
     * @param addressLine3 The addressLine3
     */
    public void setAddressLine3(Object addressLine3) {
        this.addressLine3 = addressLine3;
    }

    /**
     * @return The addressType
     */
    public Object getAddressType() {
        return addressType;
    }

    /**
     * @param addressType The addressType
     */
    public void setAddressType(Object addressType) {
        this.addressType = addressType;
    }

    /**
     * @return The city
     */
    public Object getCity() {
        return city;
    }

    /**
     * @param city The city
     */
    public void setCity(Object city) {
        this.city = city;
    }

    /**
     * @return The countryCode
     */
    public Object getCountryCode() {
        return countryCode;
    }

    /**
     * @param countryCode The countryCode
     */
    public void setCountryCode(Object countryCode) {
        this.countryCode = countryCode;
    }

    /**
     * @return The district
     */
    public Object getDistrict() {
        return district;
    }

    /**
     * @param district The district
     */
    public void setDistrict(Object district) {
        this.district = district;
    }

    /**
     * @return The emailID
     */
    public String getEmailID() {
        return emailID;
    }

    /**
     * @param emailID The emailID
     */
    public void setEmailID(String emailID) {
        this.emailID = emailID;
    }

    /**
     * @return The firstName
     */
    public Object getFirstName() {
        return firstName;
    }

    /**
     * @param firstName The firstName
     */
    public void setFirstName(Object firstName) {
        this.firstName = firstName;
    }

    /**
     * @return The lastName
     */
    public Object getLastName() {
        return lastName;
    }

    /**
     * @param lastName The lastName
     */
    public void setLastName(Object lastName) {
        this.lastName = lastName;
    }

    /**
     * @return The middleName
     */
    public Object getMiddleName() {
        return middleName;
    }

    /**
     * @param middleName The middleName
     */
    public void setMiddleName(Object middleName) {
        this.middleName = middleName;
    }

    /**
     * @return The phoneNumber
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * @param phoneNumber The phoneNumber
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * @return The phoneType
     */
    public String getPhoneType() {
        return phoneType;
    }

    /**
     * @param phoneType The phoneType
     */
    public void setPhoneType(String phoneType) {
        this.phoneType = phoneType;
    }

    /**
     * @return The pinCode
     */
    public Long getPinCode() {
        return pinCode;
    }

    /**
     * @param pinCode The pinCode
     */
    public void setPinCode(Long pinCode) {
        this.pinCode = pinCode;
    }

    /**
     * @return The state
     */
    public Object getState() {
        return state;
    }

    /**
     * @param state The state
     */
    public void setState(Object state) {
        this.state = state;
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

}
