package com.synnefx.cqms.event.sync.incident;

import android.accounts.Account;
import android.app.NotificationManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.synnefx.cqms.event.R;
import com.synnefx.cqms.event.core.Constants;
import com.synnefx.cqms.event.core.modal.event.incident.IncidentReport;
import com.synnefx.cqms.event.sync.SyncManager;
import com.synnefx.cqms.event.util.ConnectionUtils;
import com.synnefx.cqms.event.util.NotificationUtils;

import javax.inject.Inject;

import static com.synnefx.cqms.event.core.Constants.Notification.UPLOAD_NOTIFICATION_ID;


public class IncidentReportSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "IncidentSync";

    /**
     * URL to fetch content from during a sync.
     * <p/>
     * <p>This points to the Android Developers Blog. (Side note: We highly recommend reading the
     * Android Developer Blog to stay up to date on the latest Android platform developments!)
     */
    private static final String FEED_URL = "http://android-developers.blogspot.com/atom.xml";

    /**
     * Network connection timeout, in milliseconds.
     */
    private static final int NET_CONNECT_TIMEOUT_MILLIS = 15000;  // 15 seconds

    /**
     * Network read timeout, in milliseconds.
     */
    private static final int NET_READ_TIMEOUT_MILLIS = 10000;  // 10 seconds


    /**
     * Content resolver, for performing database operations.
     */
    private final ContentResolver mContentResolver;

    @Inject
    protected NotificationManager notificationManager;

    //  @Inject
    // protected Bus eventBus;

    @Inject
    protected Context mContext;

    @Inject
    protected IncidentReportSyncLocalDatastore auditSyncLocalDatastore;

    @Inject
    protected IncidentReportSyncRemoteDatastore auditSyncRemoteDatastore;

    private String notificationTitle;

    public IncidentReportSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        Log.d(TAG, "DrugReactionSyncAdapter constructor... " + autoInitialize);
        mContentResolver = context.getContentResolver();
        notificationTitle = context.getString(R.string.app_full_name);
        // Register the bus so we can send notifications.
        //eventBus.register(this);

    }

    public IncidentReportSyncAdapter(Context context, boolean autoInitialize,
                                     boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContext = context;
        mContentResolver = context.getContentResolver();
        notificationTitle = context.getString(R.string.app_full_name);
        // Register the bus so we can send notifications.
        //eventBus.register(this);
    }

    public IncidentReportSyncAdapter(Context context, NotificationManager notificationManager, IncidentReportSyncLocalDatastore localDatastore, IncidentReportSyncRemoteDatastore remoteDatastore) {
        super(context, true);
        mContext = context;
        Log.d(TAG, "DrugReactionSyncAdapter constructor... " + true);
        this.notificationManager = notificationManager;
        mContentResolver = context.getContentResolver();
        this.auditSyncLocalDatastore = localDatastore;
        this.auditSyncRemoteDatastore = remoteDatastore;
        notificationTitle = context.getString(R.string.app_full_name);
        // Register the bus so we can send notifications.
        // eventBus.register(this);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {

       Boolean manualSync = extras.getBoolean(Constants.Intent.SYNC_TYPE,false);
        Log.e(TAG, "onPerformSync");
        try {
            if (ConnectionUtils.isInternetAvaialable(getContext())) {
                //TaskApi api = new GoogleTaskApi(getGoogleAuthToken());
                //TaskDb db = new TaskDb(mContext);
                //db.open();
                try {
                    Log.e(TAG, "auditSyncLocalDatastore" + (null == auditSyncLocalDatastore));
                    Log.e(TAG, "auditSyncRemoteDatastore" + (null == auditSyncRemoteDatastore));
                    SyncManager<IncidentReport, IncidentReport> syncManager = new SyncManager<IncidentReport, IncidentReport>(auditSyncLocalDatastore, auditSyncRemoteDatastore);
                    if (syncManager.dataAvailForSync()){
                        updateNotification("Data sync in progress",manualSync);
                        syncManager.sync();
                        updateNotification("Data sync completed",manualSync);
                    }else{
                        updateNotification("No data to sync",manualSync);
                    }
                } finally {
                    //db.close();
                }
            }
            //TODO What does the below code actually do???
            getContext().getContentResolver().notifyChange(IncidentReportSyncContentProvider.CONTENT_URI, null);
        } catch (Exception e) {
            Log.e(TAG, "syncFailed:", e);
            updateNotification("Data sync failed!",manualSync);
        }
    }

    @Override
    public void onSyncCanceled() {
        //eventBus.unregister(this);
        notificationManager.cancel(UPLOAD_NOTIFICATION_ID);
        super.onSyncCanceled();
    }

    @Override
    public void onSyncCanceled(Thread thread) {
        //eventBus.unregister(this);
        notificationManager.cancel(UPLOAD_NOTIFICATION_ID);
        super.onSyncCanceled(thread);
    }

    private void updateNotification(String message,Boolean syncType) {
        if (null != notificationManager && syncType) {
            notificationManager.notify(UPLOAD_NOTIFICATION_ID, NotificationUtils.getNotification(getContext(), notificationTitle+": Data Sync", message));
        }
    }
}
