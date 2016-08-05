package com.boxfishedu.mall.utils.string;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtil {

    public static String getDate(long millisecond) {
        DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millisecond);

        return formatter.format(calendar.getTime());
    }

    public static String getDatetime(long millisecond) {
        DateFormat formatter = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millisecond);

        return formatter.format(calendar.getTime());
    }
}
