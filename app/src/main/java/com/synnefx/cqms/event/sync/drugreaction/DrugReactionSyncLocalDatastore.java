package com.synnefx.cqms.event.sync.drugreaction;

import android.util.Log;

import com.synnefx.cqms.event.core.modal.event.drugreaction.AdverseDrugEvent;
import com.synnefx.cqms.event.sqlite.AppDao;
import com.synnefx.cqms.event.sqlite.DataAccessException;
import com.synnefx.cqms.event.sync.Datastore;
import com.synnefx.cqms.event.util.PrefUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class DrugReactionSyncLocalDatastore implements Datastore<AdverseDrugEvent> {

    @Inject
    protected AppDao dao;

    @Override
    public List<AdverseDrugEvent> get() {
        List<AdverseDrugEvent> events = new ArrayList<>();
        try {
            Log.e("CSLD", "get");
            events =  dao.findAllAdverseDrugEventsByStatusForUpload(1, PrefUtils.getHospitalID());
        } catch (DataAccessException e) {
            //TODO
            Log.e("CSLD", e.toString());
        }
        return events;
    }

    @Override
    public AdverseDrugEvent create() {
        return new AdverseDrugEvent();
    }

    @Override
    public AdverseDrugEvent add(AdverseDrugEvent localDataInstance) {
        long id = dao.addAdverseDrugEvent(localDataInstance);
        AdverseDrugEvent result = dao.getAdverseDrugEventById(id);
        return result;
    }

    @Override
    public AdverseDrugEvent update(AdverseDrugEvent localDataInstance) {
        if(localDataInstance.getServerId()>0){
            localDataInstance.setStatusCode(2);
        }
        dao.updateAdverseDrugEvent(localDataInstance);
        AdverseDrugEvent result = dao.getAdverseDrugEventById(localDataInstance.getId());
        return result;
    }

    public DrugReactionSyncLocalDatastore(AppDao dao) {
        super();
        this.dao = dao;
    }

    public DrugReactionSyncLocalDatastore() {
        super();
    }

}
