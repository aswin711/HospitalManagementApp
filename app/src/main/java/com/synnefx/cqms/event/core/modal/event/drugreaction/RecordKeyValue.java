package com.synnefx.cqms.event.core.modal.event.drugreaction;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Josekutty on 8/16/2016.
 */
public class RecordKeyValue implements Serializable {

    @Expose
    private Long id;

    @Expose
    private String status;

    @Expose
    private List<String> messages;

    @Expose
    private AdverseDrugEvent item;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public AdverseDrugEvent getItem() {
        return item;
    }

    public void setItem(AdverseDrugEvent item) {
        this.item = item;
    }

    @Override
    public String toString() {
        return "RecordKeyValue{" +
                "id=" + id +
                ", status='" + status + '\'' +
                ", messages=" + messages +
                ", item=" + item +
                '}';
    }
}
