package com.synnefx.cqms.event.sync.medicationerror;

import android.content.Context;
import android.util.Log;

import com.synnefx.cqms.event.R;
import com.synnefx.cqms.event.core.modal.event.medicationerror.MedicationError;
import com.synnefx.cqms.event.sqlite.AppDao;
import com.synnefx.cqms.event.sqlite.DataAccessException;
import com.synnefx.cqms.event.sync.Datastore;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import javax.inject.Inject;

public class MedicationErrorSyncLocalDatastore implements Datastore<MedicationError> {

    @Inject
    protected AppDao dao;
    @Inject
    protected Context context;


    @Override
    public List<MedicationError> get() {
        try {
            Log.e("CSLD", "get");
            return dao.findAllMedicationErrorByStatusForUpload(1);
        } catch (DataAccessException e) {
            //TODO
        }
        return null;
    }

    @Override
    public MedicationError create() {
        return new MedicationError();
    }

    @Override
    public MedicationError add(MedicationError localDataInstance) {
        long id = dao.addIncidentReport(localDataInstance);
        MedicationError result = dao.getMedicationErrorById(id);
        return result;
    }

    @Override
    public MedicationError update(MedicationError localDataInstance) {
        if(localDataInstance.getServerId()>0){
            localDataInstance.setStatusCode(2);
        }
        dao.updateMedicationError(localDataInstance);
        MedicationError result = dao.getMedicationErrorById(localDataInstance.getId());
        return result;
    }

    public MedicationErrorSyncLocalDatastore(AppDao dao) {
        super();
        this.dao = dao;
    }

    public MedicationErrorSyncLocalDatastore() {
        super();
    }

}
