package com.synnefx.cqms.event.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.synnefx.cqms.event.core.Constants;

/**
 * Created by Josekutty on 8/21/2016.
 */
public class ServiceUtils {

    private static final String TAG = "ServiceUtils";

    public static void initiateSync(Context context, String serviceProvider, Account account) {
        Log.d(TAG, "Running " + serviceProvider);
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        if (null != account) {
            ContentResolver.requestSync(account, serviceProvider, settingsBundle);
        }
    }

    public static void initiateSync(Context context, String serviceProvider) {
        Log.d(TAG, "Running " + serviceProvider);
        // Pass the settings flags by inserting them in a bundle
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
            /*
	         * Request the sync for the default account, authority, and
	         * manual sync settings
	         */
        AccountManager accountManager = AccountManager.get(context);
        final Account[] accounts = accountManager
                .getAccountsByType(Constants.Auth.BOOTSTRAP_ACCOUNT_TYPE);
        if (accounts.length > 0) {
            ContentResolver.requestSync(accounts[0], serviceProvider, settingsBundle);
        }
    }

    public static void scheduleSync(Context context, String serviceProvider, long seconds) {
        // Pass the settings flags by inserting them in a bundle
        Log.d(TAG, "Scheduling " + serviceProvider + " for " + seconds + " seconds");
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
	        /*
	         * Request the sync for the default account, authority, and
	         * manual sync settings
	         */
        AccountManager accountManager = AccountManager.get(context);
        final Account[] accounts = accountManager
                .getAccountsByType(Constants.Auth.BOOTSTRAP_ACCOUNT_TYPE);
        if (accounts.length > 0) {
            ContentResolver.addPeriodicSync(accounts[0], serviceProvider, Bundle.EMPTY, seconds);
        }
    }

    public static void scheduleSync(Context context, String serviceProvider, Account account, long seconds) {
        Log.d(TAG, "Scheduling " + serviceProvider + " for " + seconds + " seconds");
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        if (null != account) {
            ContentResolver.addPeriodicSync(account, serviceProvider, Bundle.EMPTY, seconds);
        }
    }
}
