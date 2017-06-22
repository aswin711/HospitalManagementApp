package com.synnefx.cqms.event.core.modal;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.List;

public class ApiResponse<T> implements Serializable {

    public enum Status {
        SUCCESS, ERROR, NOTAUTHENTICATED, NOTAUTHORIZED,
    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public ApiResponse() {
    }

    @Expose
    private Status status;

    @Expose
    private String error;

    @Expose
    private List<String> errors;

    @Expose
    private String message;

    @Expose
    private List<String> messages;

    @Expose
    private T record;

    @Expose
    private List<T> records;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
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
        return "ApiResponse{" +
                "status=" + status +
                ", error='" + error + '\'' +
                ", errors=" + errors +
                ", message='" + message + '\'' +
                ", messages=" + messages +
                ", record=" + record +
                ", records=" + records +
                '}';
    }
}
