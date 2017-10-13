package com.synnefx.cqms.event.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public final class TimeUtil {
    private TimeUtil() {
    }


    /**
     * Formats the time to look like "HH:MM:SS"
     *
     * @param millis The number of elapsed milliseconds
     * @return A formatted time value
     */
    public static String formatTime(final long millis) {
        //TODO does not support hour>=100 (4.1 days)
        return String.format("%02d:%02d:%02d",
                millis / (1000 * 60 * 60),
                (millis / (1000 * 60)) % 60,
                (millis / 1000) % 60
        );
    }

    public static String getDate(Calendar calendar){
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
// Output "Wed Sep 26 14:23:28 EST 2012"

        String formatted = format1.format(calendar.getTime());
        return formatted;
    }
}
