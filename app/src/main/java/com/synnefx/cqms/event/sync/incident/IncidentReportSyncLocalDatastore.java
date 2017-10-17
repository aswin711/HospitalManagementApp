package com.synnefx.cqms.event.sync.incident;

import android.util.Log;

import com.synnefx.cqms.event.core.modal.event.incident.IncidentReport;
import com.synnefx.cqms.event.sqlite.AppDao;
import com.synnefx.cqms.event.sqlite.DataAccessException;
import com.synnefx.cqms.event.sync.Datastore;
import com.synnefx.cqms.event.util.PrefUtils;

import java.util.List;

import javax.inject.Inject;

public class IncidentReportSyncLocalDatastore implements Datastore<IncidentReport> {

    private static final String TAG = "IRSyncLocalDatastore";

    @Inject
    protected AppDao dao;

    @Override
    public List<IncidentReport> get() {

        try {
            Log.e("CSLD", "get records of: "+PrefUtils.getHospitalID());
            //get all records with status code 1 (not uploaded) of a specific hospital
            return dao.findAllIncidentReportByStatusForUpload(1,PrefUtils.getHospitalID());
        } catch (DataAccessException e) {
            //
            Log.e(TAG,e.toString());
        }
        return null;
    }

    @Override
    public IncidentReport create() {
        return new IncidentReport();
    }

    @Override
    public IncidentReport add(IncidentReport localDataInstance) {
        long id = dao.addIncidentReport(localDataInstance);
        IncidentReport result = dao.getIncidentReportById(id);
        return result;
    }

    @Override
    public IncidentReport update(IncidentReport localDataInstance) {
        if(localDataInstance.getServerId() > 0){
            localDataInstance.setStatusCode(2);
        }
        dao.updateIncidentReport(localDataInstance);
        IncidentReport result = dao.getIncidentReportById(localDataInstance.getId());
        return result;
    }

    public IncidentReportSyncLocalDatastore(AppDao dao) {
        super();
        this.dao = dao;
    }

    public IncidentReportSyncLocalDatastore() {
        super();
    }

}
