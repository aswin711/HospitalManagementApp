package com.xose.cqms.event.sync.drugreaction;


import android.util.Log;

import com.xose.cqms.event.BootstrapServiceProvider;
import com.xose.cqms.event.core.modal.event.drugreaction.AdverseDrugEvent;
import com.xose.cqms.event.core.modal.event.drugreaction.ApiResponse;
import com.xose.cqms.event.core.modal.event.drugreaction.RecordKeyValue;
import com.xose.cqms.event.sync.Datastore;

import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

public class DrugReactionSyncRemoteDatastore implements Datastore<AdverseDrugEvent> {

    private static final String TAG = "IncidentSyncRemoteDS";

    @Inject
    protected BootstrapServiceProvider serviceProvider;

    @Override
    public List<AdverseDrugEvent> get() {
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
    public AdverseDrugEvent create() {
        return new AdverseDrugEvent();
    }

    @Override
    public AdverseDrugEvent add(AdverseDrugEvent item) {
        Log.d(TAG, "addRemote:" + item.toString());
        try {
            ApiResponse result = serviceProvider.getAuthenticatedService().pushDrugReaction(item);
            if (null != result && null != result.getRecords()) {
                Log.d(TAG, "afterPost:" + result.toString());
                if (null != result.getRecord()) {
                    return (AdverseDrugEvent) ((RecordKeyValue) result.getRecord()).getItem();
                } else {
                    List<RecordKeyValue> processedRecords = result.getRecords();
                    if (null != processedRecords && processedRecords.size() > 0) {
                        return (AdverseDrugEvent) (processedRecords.get(0)).getItem();
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
    public AdverseDrugEvent update(AdverseDrugEvent item) {
        Timber.d(TAG, "addRemote:" + item.toString());
        try {
            ApiResponse result = serviceProvider.getAuthenticatedService().pushDrugReaction(item);
            if (null != result && null != result.getRecords()) {
                Log.d(TAG, "afterPost:" + result.toString());
                return (AdverseDrugEvent) ((RecordKeyValue) result.getRecord()).getItem();
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

    public DrugReactionSyncRemoteDatastore(BootstrapServiceProvider bootstrapServiceProvider) {
        super();
        this.serviceProvider = bootstrapServiceProvider;
    }

    public DrugReactionSyncRemoteDatastore() {
        super();
    }
}
