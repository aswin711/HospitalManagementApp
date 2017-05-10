package com.xose.cqms.event.sync.incident;

import android.util.Log;

import com.xose.cqms.event.core.modal.event.incident.IncidentReport;
import com.xose.cqms.event.sqlite.AppDao;
import com.xose.cqms.event.sqlite.DataAccessException;
import com.xose.cqms.event.sync.Datastore;

import java.util.List;

import javax.inject.Inject;

public class IncidentReportSyncLocalDatastore implements Datastore<IncidentReport> {

    @Inject
    protected AppDao dao;

    @Override
    public List<IncidentReport> get() {
        try {
            Log.e("CSLD", "get");
            return dao.findAllIncidentReportByStatusForUpload(1);
        } catch (DataAccessException e) {
            //TODO
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
