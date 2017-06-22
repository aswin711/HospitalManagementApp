package com.synnefx.cqms.event.sync.incident;


import android.util.Log;

import com.synnefx.cqms.event.BootstrapServiceProvider;
import com.synnefx.cqms.event.core.modal.event.incident.ApiResponse;
import com.synnefx.cqms.event.core.modal.event.incident.IncidentReport;
import com.synnefx.cqms.event.core.modal.event.incident.RecordKeyValue;
import com.synnefx.cqms.event.sync.Datastore;

import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

public class IncidentReportSyncRemoteDatastore implements Datastore<IncidentReport> {

    private static final String TAG = "IncidentSyncRemoteDS";

    @Inject
    protected BootstrapServiceProvider serviceProvider;

    @Override
    public List<IncidentReport> get() {
    /*	try{
			ApiResponse<HHAuditSession> result = serviceProvider.getAuthenticatedService().createAuthenticatedService(HHAuditService.class).getRecords();
			if(null != result){
				return result.getRecords();
			}
		}catch(Exception e){
			Timber.e("Error ", e);
		}
		*/
        return null;
    }

    @Override
    public IncidentReport create() {
        return new IncidentReport();
    }

    @Override
    public IncidentReport add(IncidentReport item) {
        Log.d(TAG, "addRemote:" + item.toString());
        try {
            ApiResponse result = serviceProvider.getAuthenticatedService().pushIncident(item);
            if (null != result && null != result.getRecords()) {
                Log.d(TAG, "afterPost:" + result.toString());
                if (null != result.getRecord()) {
                    return (IncidentReport) ((RecordKeyValue) result.getRecord()).getItem();
                } else {
                    List<RecordKeyValue> processedRecords = result.getRecords();
                    if (null != processedRecords && processedRecords.size() > 0) {
                        return (IncidentReport) (processedRecords.get(0)).getItem();
                    }
                }
            } else {
                if (null != result && null != result.getErrors()) {
                    //TODO throw custom error
                    Timber.e(TAG, result.getErrors());
                }
            }
        } catch (Exception e) {
            Timber.e(TAG, e);
        }
        return null;
    }

    @Override
    public IncidentReport update(IncidentReport item) {
        Timber.d(TAG, "addRemote:" + item.toString());
        try {
            ApiResponse result = serviceProvider.getAuthenticatedService().pushIncident(item);
            if (null != result && null != result.getRecords()) {
                Log.d(TAG, "afterPost:" + result.toString());
                return (IncidentReport) ((RecordKeyValue) result.getRecord()).getItem();
            } else {
                if (null != result && null != result.getErrors()) {
                    //TODO throw custom error
                    Timber.e(TAG, result.getErrors());
                }
            }
        } catch (Exception e) {

        }
        return null;
    }

    public IncidentReportSyncRemoteDatastore(BootstrapServiceProvider bootstrapServiceProvider) {
        super();
        this.serviceProvider = bootstrapServiceProvider;
    }

    public IncidentReportSyncRemoteDatastore() {
        super();
    }
}
