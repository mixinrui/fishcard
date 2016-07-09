package com.boxfishedu.fishcard.timer.common.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by hucl on 16/3/24.
 */
public class DateUtil {

    public final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public final static DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public final static DateTimeFormatter dateTimeFormatterWithTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Date String2Date(String str) throws RuntimeException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = sdf.parse(str);
        } catch (Exception ex) {
            throw new RuntimeException("日期格式不合法");
        }
        return date;
    }

    /**
     * yyyy-MM-dd
     * @param str
     * @return
     * @throws RuntimeException
     */
    public static Date String2SimpleDate(String str) throws RuntimeException {
        Date date = null;
        try {
            date = dateFormat.parse(str);
        } catch (Exception ex) {
            throw new RuntimeException("日期格式不合法");
        }
        return date;
    }

    public static Date date2SimpleDate(Date dateWithTime){
        String dateWithTimeStr=Date2String(dateWithTime);
        return String2SimpleDate(dateWithTimeStr);
    }

    public static String simpleDate2String(Date date) {
        String dateStr = null;
        try {
            dateStr = dateFormat.format(date);
        } catch (Exception ex) {
            throw new RuntimeException("日期格式不合法");
        }
        return dateStr;
    }

    public static String simpleDateLong2String(Long time) {
        return simpleDate2String(new Date(time));
    }

    public static Date date(long dateStr) {
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(dateStr);
        return instance.getTime();
    }

    /**
     * yyyy-MM-dd
     * @param date
     * @return
     */
    public static String string(Date date) {
        return dateFormat.format(date);
    }

    public static String Date2String(Date date) throws RuntimeException {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }


    public static String string(LocalDateTime localDateTime) {
        return dateTimeFormatter.format(localDateTime);
    }

    public static Long getStartTime(String date){
        Calendar todayStart = Calendar.getInstance();
        todayStart.setTime(String2SimpleDate(date));
        todayStart.set(Calendar.HOUR, 0);
        todayStart.set(Calendar.MINUTE, 0);
        todayStart.set(Calendar.SECOND, 0);
        todayStart.set(Calendar.MILLISECOND, 0);
        return todayStart.getTime().getTime();
    }

    public static Long getEndTime(String date){
        Calendar todayEnd = Calendar.getInstance();
        todayEnd.setTime(String2SimpleDate(date));
        todayEnd.set(Calendar.HOUR, 23);
        todayEnd.set(Calendar.MINUTE, 59);
        todayEnd.set(Calendar.SECOND, 59);
        todayEnd.set(Calendar.MILLISECOND, 999);
        return todayEnd.getTime().getTime();
    }

    public static Date localDate2Date(LocalDateTime ldt){
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDateTime localDateTime2Date(Date date){
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    public static int getDayFromDate(Date dt){
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        return cal.get(Calendar.DAY_OF_WEEK) - 1;
    }

    public final static DateFormat HHmmss = new SimpleDateFormat("HH:mm:ss");

    public static String mockMinutesAfter(int minutes) {
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.MINUTE, minutes);
        return HHmmss.format(instance.getTime());
    }

    public static Date parseTime(String time) {
        try {
            return HHmmss.parse(time);
        } catch (ParseException e) {
            throw new RuntimeException(time + "时间转换错误!!" + e.getMessage());
        }
    }

    public static String LocalDate2String(LocalDateTime localDateTime){
        return Date2String(localDate2Date(localDateTime));
    }

    public static void main(String[] args) {
        System.out.println(mockMinutesAfter(5));
    }

    public static Date merge(Date date, Date time) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        instance.set(Calendar.HOUR_OF_DAY, time.getHours());
        instance.set(Calendar.MINUTE, time.getMinutes());
        instance.set(Calendar.SECOND, time.getSeconds());
        return instance.getTime();
    }
}
