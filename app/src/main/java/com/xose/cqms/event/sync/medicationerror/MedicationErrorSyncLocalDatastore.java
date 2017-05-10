package com.xose.cqms.event.sync.medicationerror;

import android.util.Log;

import com.xose.cqms.event.core.modal.event.medicationerror.MedicationError;
import com.xose.cqms.event.sqlite.AppDao;
import com.xose.cqms.event.sqlite.DataAccessException;
import com.xose.cqms.event.sync.Datastore;

import java.util.List;

import javax.inject.Inject;

public class MedicationErrorSyncLocalDatastore implements Datastore<MedicationError> {

    @Inject
    protected AppDao dao;

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
