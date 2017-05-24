package com.sioo.util;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    public static final String dtShort = "yyyyMMdd";
    public static final String dtLong = "yyyyMMddHHmmss";
    public static final String hour = "HHmmss";

    public static int getDay() {
        Date date = new Date();
        DateFormat df = new SimpleDateFormat(dtShort);
        String strNow = df.format(date);
        return Integer.valueOf(strNow);
    }

    public static long getTime() {
        Date date = new Date();
        DateFormat df = new SimpleDateFormat(dtLong);
        String strNow = df.format(date);
        return Long.valueOf(strNow);
    }

    public static String getHHmmss() {
        Date date = new Date();
        DateFormat df = new SimpleDateFormat(hour);
        return df.format(date);
    }

    public static long getLongTime(Date date) {
        DateFormat df = new SimpleDateFormat(dtLong);
        String strNow = df.format(date);
        return Long.valueOf(strNow);
    }

    public static long getDayStart() {
        Date date = new Date();
        DateFormat df = new SimpleDateFormat(dtShort);
        String strNow = df.format(date) + "000000";
        return Long.valueOf(strNow);
    }
}
