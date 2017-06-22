package com.synnefx.cqms.event.core.modal;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.List;


//import com.fasterxml.jackson.annotation.JsonInclude;
//import com.fasterxml.jackson.annotation.JsonInclude.Include;

//@JsonInclude(Include.NON_EMPTY)
public class ApiRequest<T> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public ApiRequest() {
    }

    @Expose
    private AuthHeader authHeader;

    @Expose
    private T record;

    @Expose
    private List<T> records;

    public AuthHeader getAuthHeader() {
        return authHeader;
    }

    public void setAuthHeader(AuthHeader authHeader) {
        this.authHeader = authHeader;
    }

    public T getRecord() {
        return record;
    }

    public void setRecord(T record) {
        this.record = record;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    @Override
    public String toString() {
        return "ApiRequest [authHeader=" + authHeader + ", record=" + record
                + ", records=" + records + "]";
    }

}
