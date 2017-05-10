package com.xose.cqms.event.sync.medicationerror;


import android.util.Log;

import com.xose.cqms.event.BootstrapServiceProvider;
import com.xose.cqms.event.core.modal.event.medicationerror.ApiResponse;
import com.xose.cqms.event.core.modal.event.medicationerror.MedicationError;
import com.xose.cqms.event.core.modal.event.medicationerror.RecordKeyValue;
import com.xose.cqms.event.sync.Datastore;

import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

public class MedicationErrorSyncRemoteDatastore implements Datastore<MedicationError> {

    private static final String TAG = "IncidentSyncRemoteDS";

    @Inject
    protected BootstrapServiceProvider serviceProvider;

    @Override
    public List<MedicationError> get() {
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
    public MedicationError create() {
        return new MedicationError();
    }

    @Override
    public MedicationError add(MedicationError item) {
        Log.d(TAG, "addRemote:" + item.toString());
        try {
            ApiResponse result = serviceProvider.getAuthenticatedService().pushMedicationError(item);
            if (null != result && null != result.getRecords()) {
                Log.d(TAG, "afterPost:" + result.toString());
                if (null != result.getRecord()) {
                    return (MedicationError) ((RecordKeyValue) result.getRecord()).getItem();
                } else {
                    List<RecordKeyValue> processedRecords = result.getRecords();
                    if (null != processedRecords && processedRecords.size() > 0) {
                        return (MedicationError) (processedRecords.get(0)).getItem();
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
    public MedicationError update(MedicationError item) {
        Timber.d(TAG, "addRemote:" + item.toString());
        try {
            ApiResponse result = serviceProvider.getAuthenticatedService().pushMedicationError(item);
            if (null != result && null != result.getRecords()) {
                Log.d(TAG, "afterPost:" + result.toString());
                return (MedicationError) ((RecordKeyValue) result.getRecord()).getItem();
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

    public MedicationErrorSyncRemoteDatastore(BootstrapServiceProvider bootstrapServiceProvider) {
        super();
        this.serviceProvider = bootstrapServiceProvider;
    }

    public MedicationErrorSyncRemoteDatastore() {
        super();
    }
}
