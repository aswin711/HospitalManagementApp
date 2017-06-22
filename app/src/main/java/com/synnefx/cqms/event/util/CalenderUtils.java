package com.synnefx.cqms.event.util;

import android.text.TextUtils;

import com.synnefx.cqms.event.core.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class CalenderUtils {

    // private static final String DEFAULT_DATE_FORMAT = "EEE, d MMM yyyy";

    public static final TimeZone DEFAULT_TIME_ZONE = TimeZone
            .getTimeZone("IST");

    private static final String HOURS_STR = "hours";
    private static final String HOUR_STR = "hour";

    private static final String MINUTES_STR = "minutes";

    public static final TimeZone TIME_ZONE = TimeZone.getTimeZone("UTC");

    public static Calendar setTimeZone(Calendar calendar) {
        Calendar cal = Calendar.getInstance(TIME_ZONE);
        cal.setTimeInMillis(calendar.getTimeInMillis());
        return cal;
    }

    public static String formatUpdatedTime(Calendar calendar, String format) {
        Calendar now = Calendar.getInstance();
        if (now.after(calendar)) {
            long startTime = calendar.getTime().getTime();
            long endTime = now.getTime().getTime();
            long diffTime = endTime - startTime;
            int diff = CalenderUtils.compareDate(now, calendar);
            if (diff == 0) {
                long diffMinutes = diffTime / (1000 * 60);
                StringBuilder bldr = new StringBuilder("In ");
                if (diffMinutes < 5) {
                    return "In 5 minutes";
                } else if (diffMinutes < 60) {
                    return bldr.append(diffMinutes).append(Constants.Common.BLANK)
                            .append(MINUTES_STR).toString();
                } else {
                    long diffHours = diffMinutes / 60;
                    String ss = (diffHours == 1) ? HOUR_STR : HOURS_STR;
                    return bldr.append(diffHours).append(Constants.Common.BLANK)
                            .append(ss).toString();
                }
            } else {
                int diffDays = CalenderUtils.getDateDiff(calendar, now);
                //long diffDays = diffTime / (1000 * 60 * 60 * 24);
                if (diffDays == 1) {
                    return Constants.Common.YESTERDAY;
                } else {
                    return formatCalendarToString(calendar, format);
                }
            }
        }
        return "";
    }

    public static String formatCalendarToString(Calendar calendar, String format) {
        if (null == calendar)
            return null;
        if (TextUtils.isEmpty(format)) {
            format = Constants.Common.DATE_DISPLAY_FORMAT;
        }
        SimpleDateFormat format1 = new SimpleDateFormat(format);
        // format1.setTimeZone(TIME_ZONE);
        String formatted = format1.format(calendar.getTime());
        return formatted;
    }

    public static String formatCalendarToString(Calendar eventDate) {
        return formatCalendarToString(eventDate, null);
    }

    public static Calendar parseStringToCalender(String formattedVal,
                                                 String format) {
        if (TextUtils.isEmpty(format)) {
            format = Constants.Common.DATE_DISPLAY_FORMAT;
        }
        SimpleDateFormat format1 = new SimpleDateFormat(format);
        // format1.setTimeZone(TIME_ZONE);
        try {
            Date date = format1.parse(formattedVal);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return cal;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Calendar setTimeToDayStart(Calendar cal) {
        if (null == cal)
            cal = Calendar.getInstance();
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.AM_PM, Calendar.AM);
        return cal;
    }

    public static Calendar setTimeToMidnight(Calendar cal) {
        if (null == cal)
            cal = Calendar.getInstance();
        cal.set(Calendar.HOUR, 12);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.AM_PM, Calendar.PM);
        return cal;
    }

    public static boolean isFutureDate(Calendar ca) {
        ca = setTimeToDayStart(ca);
        return ca.after(setTimeToDayStart(Calendar.getInstance()));
    }

    public static int getCurrentYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    public static int getDateDiff(Calendar startDate, Calendar endDate) {
        long start;
        long end;
        start = setTimeToDayStart(startDate).getTimeInMillis();
        end = setTimeToDayStart(endDate).getTimeInMillis();

        long diffTime = end - start;
        if (diffTime != 0) {
            int diff = (int) (diffTime / (1000 * 60 * 60 * 24));
            return diff;
        } else {
            return 0;
        }
    }

    public static int getHourDifference(Calendar startDate, Calendar endDate) {
        long start;
        long end;
        start = startDate.getTimeInMillis();
        end = endDate.getTimeInMillis();
        long diffTime = end - start;
        if (diffTime != 0) {
            int diff = (int) (diffTime / (1000 * 60 * 60));
            return diff;
        } else {
            return 0;
        }
    }

    public static int compareDate(Calendar ca1, Calendar ca2) {
        Calendar temp1 = Calendar.getInstance();
        Calendar temp2 = Calendar.getInstance();
        if (null != temp1) {
            temp1.setTime(ca1.getTime());
            temp1 = setTimeToDayStart(temp1);
        } else {
            return -1;
        }
        if (null != temp2) {
            temp2.setTime(ca2.getTime());
            temp2 = setTimeToDayStart(temp2);
        } else {
            return 1;
        }
        String s1 = temp1.getTime().toString();
        String s2 = temp2.getTime().toString();
        return temp1.compareTo(temp2);
    }

    public static boolean isFutureTime(Calendar ca) {
        return ca.after(Calendar.getInstance());
    }

    public static void main(String args[]) {
        Calendar c = parseStringToCalender("1/2/2105", "MM/dd/yyyy");
        c.set(Calendar.MINUTE, 10);
        Calendar c1 = parseStringToCalender("1/2/2105", "MM/dd/yyyy");
        System.out.println(getDateDiff(c, c1));
    }
}
