package com.synnefx.cqms.event.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.synnefx.cqms.event.BootstrapApplication;
import com.synnefx.cqms.event.core.modal.User;

import timber.log.Timber;

public class PrefUtils {

    private static final String TAG = PrefUtils.class.getSimpleName();

    public static final String PREFS_LOGIN_USERNAME_KEY = "__USERNAME__";
    public static final String PREFS_LOGIN_PASSWORD_KEY = "__PASSWORD__";
    public static final String PREF_USER_FULLNAME = "__FULL_NAME__";

    public static final String PREFS_AUTH_KEY = "__AUTHKEY__";

    public static final String PREFS_DEVICE_TOKEN = "__DEVICE_TOKEN__";

    public static final String PREFS_ACTIVE_SURVEY_ID_KEY = "__SURVEY_ID__";
    public static final String PREFS_PRIMARY_LANG = "__LANG__";
    public static final String PREFS_QUESTION_ID = "__QUESTION_ID__";

    public static final String PREFS_HOSP_ID = "__HOSP_ID__";
    public static final String PREFS_USER_ID = "__USER_ID__";
    public static final String PREFS_USER_DISPLAY_NAME = "__USER_FULLNAME__";
    public static final String PREFS_HOSP_DISPLAY_NAME = "__HOSP_FULLNAME__";

    public static final String PREF_USER_LOGGED_IN = "_USER_LOGGED_IN";


    public static final String PREF_ACTIVE_USER = "pref_active_user";
    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";

    public static final String LATEST_VERSION_NUMBER = "LATEST_APP_VERSION";



    /**
     * Called to save supplied value in shared preferences against given key.
     *
     * @param key     Key of value to save against
     * @param value   Value to save
     */
    public static void saveToPrefs(Context context,String key, String value) {
        SharedPreferences prefs =  getSettings();
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void saveToPrefs(Context context,String key, Boolean value) {
        SharedPreferences prefs =  getSettings();
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }


    /**
     * Called to retrieve required value from shared preferences, identified by
     * given key. Default value will be returned of no value found or error
     * occurred.
     *
     * @param context      Context of caller activity
     * @param key          Key to find value against
     * @param defaultValue Value to return if no data found against given key
     * @return Return the value found against given key, default if not found or
     * any error occurs
     */
    public static String getFromPrefs(Context context, String key,
                                      String defaultValue) {
        SharedPreferences sharedPrefs = getSettings();
        try {
            return sharedPrefs.getString(key, defaultValue);
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }


    public static Long getLongFromPrefs(Context context, String key,
                                        Long defaultValue) {
        try {
            String pref = getFromPrefs(context, key, null);
            if (TextUtils.isEmpty(pref)) {
                return defaultValue;
            } else {
                return Long.valueOf(pref);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }



    /**
     * Get indicator, that GCM token was sent to third party server.
     *
     * @return true if successfully received by third party server. False otherwise.
     */
    public static Boolean getTokenSentToServer() {
        SharedPreferences prefs = getSettings();
        boolean tokenSent = prefs.getBoolean(SENT_TOKEN_TO_SERVER, false);
        Timber.d("%s - Obtained token sent to server: %s", TAG, tokenSent);
        return tokenSent;
    }

    /**
     * Set GCM token sent to third party server indicator.
     *
     * @param tokenSent true if successfully received by server.
     */
    public static void setTokenSentToServer(boolean tokenSent) {
        putParam(SENT_TOKEN_TO_SERVER, tokenSent);
    }

    /**
     * Obtain preferences instance.
     *
     * @return base instance of app SharedPreferences.
     */
    public static SharedPreferences getSettings() {
        return BootstrapApplication.getInstance().getSharedPreferences(BootstrapApplication.PACKAGE_NAME, Context.MODE_PRIVATE);
    }

    private static boolean putParam(String key, String value) {
        SharedPreferences.Editor editor = getSettings().edit();
        editor.putString(key, value);
        return editor.commit();
    }

    private static boolean putParam(String key, boolean value) {
        SharedPreferences.Editor editor = getSettings().edit();
        editor.putBoolean(key, value);
        editor.apply();
        return editor.commit();
    }


    public static String getLatestAppVersion(Context context) {
        SharedPreferences prefs = getSettings();
        String version = prefs.getString(LATEST_VERSION_NUMBER, "");
        Timber.d("%s - App version latest: %s", TAG, version);
        return version;
    }

    public static void deleteFromPrefs(){
        SharedPreferences preferences = getSettings();
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }


    public static String getHospitalID(){
        SharedPreferences prefs = getSettings();
        return prefs.getString(PREFS_HOSP_ID,"");
    }


    public static String getDeviceToken(){
        SharedPreferences prefs = getSettings();
        return prefs.getString(PREFS_DEVICE_TOKEN,"");
    }
}
