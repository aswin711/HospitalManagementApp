package com.synnefx.cqms.event.sync.conf;

import android.accounts.Account;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.synnefx.cqms.event.BootstrapServiceProvider;
import com.synnefx.cqms.event.core.modal.IncidentType;
import com.synnefx.cqms.event.core.modal.Unit;
import com.synnefx.cqms.event.sqlite.DatabaseHelper;
import com.synnefx.cqms.event.ui.SettingsActivity;
import com.synnefx.cqms.event.util.ConnectionUtils;
import com.synnefx.cqms.event.util.NotificationUtils;
import com.synnefx.cqms.event.util.PrefUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static com.synnefx.cqms.event.core.Constants.Notification.IMPORT_INCIDENTTYPE_NOTIFICATION_ID;
import static com.synnefx.cqms.event.core.Constants.Notification.IMPORT_NOTIFICATION_ID;
import static com.synnefx.cqms.event.core.Constants.Notification.IMPORT_UNIT_NOTIFICATION_ID;


public class ConfSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "ConfSyncAdapter";

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

    @Inject
    protected Context mContext;

    @Inject
    DatabaseHelper databaseHelper;

    @Inject
    BootstrapServiceProvider serviceProvider;

    public ConfSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        Log.d(TAG, "ConfSyncAdapter constructor... " + autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    public ConfSyncAdapter(Context context, boolean autoInitialize,
                           boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContext = context;
        mContentResolver = context.getContentResolver();
    }

    public ConfSyncAdapter(Context context, NotificationManager notificationManager, BootstrapServiceProvider serviceProvider, DatabaseHelper databaseHelper) {
        super(context, true);
        mContext = context;
        Log.d(TAG, "ConfSyncAdapter constructor... " + true);
        this.notificationManager = notificationManager;
        mContentResolver = context.getContentResolver();
        this.databaseHelper = databaseHelper;
        this.serviceProvider = serviceProvider;
    }


    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Log.e(TAG, "onPerformSync");
        try {
            if (ConnectionUtils.isInternetAvaialable(getContext())) {
                List<String> errorMessages = new ArrayList<>(10);
                List<String> successessages = new ArrayList<>(10);
                try {
                    updateNotification("Configuration import in progress");
                    //DatabaseHelper db = new DatabaseHelper(mContext);
                    String hospitalRef = PrefUtils.getFromPrefs(getContext(), PrefUtils.PREFS_HOSP_ID, null);
                    if (null != hospitalRef && !"".equals(hospitalRef.trim())) {
                        //List<ServiceType> serviceTypes = serviceProvider.getAuthenticatedService().getServiceTypes();
                        List<Unit> units = serviceProvider.getAuthenticatedService().getUnits();
                        //List<Specialty> specialties = serviceProvider.getAuthenticatedService().getSpecialities();
                        List<IncidentType> incidentTypes = serviceProvider.getAuthenticatedService().getIncidentTypes();

                        if (null != units && units.size() > 0) {
                            databaseHelper.syncUnits(units, hospitalRef);
                            successessages.add("Units added");
                        } else {
                            updateClosableNotification("Units not configured", IMPORT_UNIT_NOTIFICATION_ID);
                            errorMessages.add("Units not configured");
                        }

                        if (null != incidentTypes && incidentTypes.size() > 0) {
                            databaseHelper.syncIncidentTypes(incidentTypes, hospitalRef);
                            successessages.add("Incident Types added");
                        } else {
                            updateClosableNotification("Incident Types  not configured", IMPORT_INCIDENTTYPE_NOTIFICATION_ID);
                            errorMessages.add("Incident Types  not configured");
                        }

                    } else {
                        //Show warning
                    }
                    updateClosableNotification("Configuration update completed", IMPORT_NOTIFICATION_ID);
                } finally {
                    //db.close();
                }
            }
            getContext().getContentResolver().notifyChange(ConfSyncContentProvider.CONTENT_URI, null);
        } catch (Exception e) {
            Log.e(TAG, "syncFailed:", e);
            updateNotification("Data sync failed!");
        }
    }

    @Override
    public void onSyncCanceled() {
        //eventBus.unregister(this);
        notificationManager.cancel(IMPORT_NOTIFICATION_ID);
        super.onSyncCanceled();
    }

    @Override
    public void onSyncCanceled(Thread thread) {
        //eventBus.unregister(this);
        notificationManager.cancel(IMPORT_NOTIFICATION_ID);
        super.onSyncCanceled(thread);
    }

    private void updateNotification(String message) {
        if (null != notificationManager) {
            notificationManager.notify(IMPORT_NOTIFICATION_ID, NotificationUtils.getNotification(getContext(), "CQMS : Configuration update", message));
        }
    }

    private void updateNotification(String message, int notificationID) {
        if (null != notificationManager) {
            notificationManager.notify(notificationID, NotificationUtils.getNotification(getContext(), "CQMS : Configuration update", message));
        }
    }

    private void updateClosableNotification(String message, int notificationID) {
        if (null != notificationManager) {
            final Intent i = new Intent(getContext(), SettingsActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, i, 0);
            NotificationCompat.Builder builder = NotificationUtils.getNotificationBuilder(getContext(), "CQMS : Configuration update", message, pendingIntent)
                    .setContentText(message);
            builder.setOngoing(false);
            notificationManager.notify(notificationID, builder.build());
        }
    }
}
