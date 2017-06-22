package com.synnefx.cqms.event.sync.medicationerror;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.synnefx.cqms.event.BootstrapApplication;

import javax.inject.Inject;

public class MedicationErrorSyncService extends Service {

    @Inject
    protected MedicationErrorSyncAdapter mSyncAdapter;

    private static final Object mSyncAdapterLock = new Object();

    @Override
    public void onCreate() {
        super.onCreate();
        BootstrapApplication.component().inject(this);
        Log.e("TAG", "onCreate.... is null?" + (mSyncAdapter == null));
        synchronized (mSyncAdapterLock) {
            if (mSyncAdapter == null) {
                mSyncAdapter = new MedicationErrorSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e("TAG", "onBind....");
        return mSyncAdapter.getSyncAdapterBinder();
    }

}
