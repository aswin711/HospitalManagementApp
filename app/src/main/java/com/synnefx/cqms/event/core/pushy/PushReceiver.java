package com.synnefx.cqms.event.core.pushy;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.squareup.otto.Bus;
import com.synnefx.cqms.event.BootstrapApplication;
import com.synnefx.cqms.event.core.Constants;
import com.synnefx.cqms.event.events.UnAuthorizedErrorEvent;
import com.synnefx.cqms.event.services.gcm.QuickstartPreferences;
import com.synnefx.cqms.event.sqlite.AppDao;
import com.synnefx.cqms.event.sync.conf.ConfSyncContentProvider;
import com.synnefx.cqms.event.ui.MainActivity;
import com.synnefx.cqms.event.util.NotificationUtils;
import com.synnefx.cqms.event.util.PrefUtils;

import static com.synnefx.cqms.event.core.Constants.Notification.DEFAULT_NOTIFICATION_ID;

/**
 * Created by Josekutty on 1/30/2017.
 */
public class PushReceiver extends BroadcastReceiver {

    protected AppDao appDao;

    private Bus bus;


    @Override
    public void onReceive(Context context, Intent intent) {
        String notificationTitle = "CQMS";
        String notificationText = "";
        String mType = "0";
        BootstrapApplication.component().inject(this);

        // Attempt to extract the "message" property from the payload: {"message":"Hello World!"}
        if (intent.getStringExtra("result") != null) {
            notificationText = intent.getStringExtra("result");
        }
        if (intent.getStringExtra("type") != null) {
            mType = intent.getStringExtra("type");
        }
        Integer notificationType = 0;
        if (!TextUtils.isEmpty(mType) && TextUtils.isDigitsOnly(mType)) {
            notificationType = Integer.valueOf(mType);
        }

        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */
        if (null != notificationType) {
            appDao = new AppDao(context.getApplicationContext());
            switch (notificationType) {
                case 5:
                    //Clear Pushy registration
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
                    sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false).apply();
                    PrefUtils.setTokenSentToServer(false);
                    break;
                case 10:
                case 15:
                    syncConfig(context);
                    break;
                case 120:
                    updateIncidentReportStatus(intent.getStringExtra("clientid"), intent.getStringExtra("id"), intent.getStringExtra("status_code"), context);
                    break;
                case 125:
                    updateMedicationErrortStatus(intent.getStringExtra("clientid"), intent.getStringExtra("id"), intent.getStringExtra("status_code"), context);
                    break;
                case 403:
                    bus.post(new UnAuthorizedErrorEvent("Token expired"));
                case 1:
                default:
                    sendNotification(DEFAULT_NOTIFICATION_ID, notificationText, context);
                    break;
            }
        } else {
            sendNotification(DEFAULT_NOTIFICATION_ID, notificationText, context);
        }
    }


    private void updateIncidentReportStatus(String id, String serverRef, String status, Context context) {
        Long clientId = 0l;
        Long serverId = 0l;
        Integer statusCode = 0;
        if (!TextUtils.isEmpty(id) && TextUtils.isDigitsOnly(id)) {
            clientId = Long.valueOf(id);
        }
        if (!TextUtils.isEmpty(serverRef) && TextUtils.isDigitsOnly(serverRef)) {
            serverId = Long.valueOf(serverRef);
        }
        if (!TextUtils.isEmpty(status) && TextUtils.isDigitsOnly(status)) {
            statusCode = Integer.valueOf(status);
        }
        if (0 < clientId && statusCode == 3) {
            appDao = new AppDao(context.getApplicationContext());
            appDao.updateIncidentReportStatus(clientId, serverId, statusCode);
        }
    }

    private void updateMedicationErrortStatus(String id, String serverRef, String status, Context context) {
        Long clientId = 0l;
        Long serverId = 0l;
        Integer statusCode = 0;
        if (!TextUtils.isEmpty(id) && TextUtils.isDigitsOnly(id)) {
            clientId = Long.valueOf(id);
        }
        if (!TextUtils.isEmpty(serverRef) && TextUtils.isDigitsOnly(serverRef)) {
            serverId = Long.valueOf(serverRef);
        }
        if (!TextUtils.isEmpty(status) && TextUtils.isDigitsOnly(status)) {
            statusCode = Integer.valueOf(status);
        }
        if (0 < clientId && statusCode == 3) {
            appDao = new AppDao(context.getApplicationContext());
            appDao.updateMedicationErrortStatus(clientId, serverId, statusCode);
        }
    }

    private void syncConfig(Context context) {
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        /*
         * Request the sync for the default account, authority, and
	     * manual sync settings
	     */
        AccountManager accountManager = AccountManager.get(context.getApplicationContext());
        final Account[] accounts = accountManager
                .getAccountsByType(Constants.Auth.BOOTSTRAP_ACCOUNT_TYPE);
        if (accounts.length > 0) {
            Log.d("TAG", "Request sync...Scheduled");
            /*
             * Signal the framework to run your sync adapter. Assume that
             * app initialization has already created the account.
             */
            ContentResolver.requestSync(
                    accounts[0],
                    ConfSyncContentProvider.AUTHORITY,
                    settingsBundle);
        }
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(int notificationId, String message, Context context) {
        if (null == message || 0 == message.trim().length()) {
            return;
        }
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = NotificationUtils.getSoundNotification(context.getApplicationContext(), "CQMS Notification", message, pendingIntent);
        notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(notificationId, notification);
    }
}