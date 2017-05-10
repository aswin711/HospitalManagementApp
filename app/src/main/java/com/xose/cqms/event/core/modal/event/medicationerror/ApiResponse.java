package com.xose.cqms.event.core.modal.event.medicationerror;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.List;

public class ApiResponse implements Serializable {

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
    private RecordKeyValue record;

    @Expose
    private List<RecordKeyValue> records;

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

    public RecordKeyValue getRecord() {
        return record;
    }

    public void setRecord(RecordKeyValue record) {
        this.record = record;
    }

    public List<RecordKeyValue> getRecords() {
        return records;
    }

    public void setRecords(List<RecordKeyValue> records) {
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
