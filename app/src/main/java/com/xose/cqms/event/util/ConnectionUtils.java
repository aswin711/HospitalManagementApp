package com.xose.cqms.event.util;

import android.content.Context;

import com.xose.cqms.event.ui.base.ConnectionDetector;

/**
 * Created by Josekutty on 8/13/2016.
 */
public class ConnectionUtils {

    public static boolean isInternetAvaialable(Context context) {
        ConnectionDetector cd = new ConnectionDetector(context);
        return cd.isConnectingToInternet();
    }
}
