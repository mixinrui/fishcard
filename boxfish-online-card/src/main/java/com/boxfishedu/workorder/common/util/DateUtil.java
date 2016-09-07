package com.boxfishedu.workorder.common.util;

import com.boxfishedu.workorder.servicex.bean.MonthTimeSlots;
import com.boxfishedu.workorder.web.view.form.DateRangeForm;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by hucl on 16/3/24.
 */
public class DateUtil {

    public final static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public final static DateTimeFormatter dateFormatterChinese = DateTimeFormatter.ofPattern("yyyy年MM月dd日");

    public final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public final static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

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

    public  static String Date2ForForeignDate(Date date){
        return new SimpleDateFormat("HH:mm,MM-dd").format(date);
    }


    public static Date String2DateBack(String str) throws RuntimeException {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
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
            date = new SimpleDateFormat("yyyy-MM-dd").parse(str);
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
            dateStr = new SimpleDateFormat("yyyy-MM-dd").format(date);
        } catch (Exception ex) {
            throw new RuntimeException("日期格式不合法");
        }
        return dateStr;
    }

    public static LocalDate convertLocalDate(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDateTime convertLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    public static LocalDate parseLocalDate(String date) {
        return LocalDate.parse(date, dateFormatter);
    }

    public static LocalTime parseLocalTime(String time) {
        return LocalTime.parse(time, timeFormatter);
    }

    public static String formatLocalDateTime(LocalDateTime localDateTime) {
        return dateTimeFormatter.format(localDateTime);
    }

    public static Date convertToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
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
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    //TODO:将hh:mm:ss(12进制改为HH:mm:ss:24进制)
    public static String Date2String(Date date) throws RuntimeException {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }


    public static String Date2StringChinese(Date date) throws RuntimeException {
        return new SimpleDateFormat("yyyy年MM月dd日").format(date);
    }



    public static String Date2String24(Date date) throws RuntimeException {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    public static String formatLocalDate(LocalDateTime localDateTime) {
        return dateFormatter.format(localDateTime);
    }

    public static Date getStartTime(Date date) {
        // 差值 + 时间戳与北京时间的8小时时间差
        long difference = (date.getTime() + 8 * 3600 * 1000) % MonthTimeSlots.DAY_OF_MILLIONS;
        return new Date(date.getTime() - difference);
    }

    /**
     *  获取制定时间的前 num  天
     */
    public static Date getBeforeDays(Date date ,int num){

        Calendar   calendar   =   new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(calendar.DATE,-num);//把日期往后增加一天.整数往后推,负数往前移动
        return  calendar.getTime();   //这个时间就是日期往后推一天的结果

    }

    public static Date localDate2Date(LocalDateTime ldt){
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date parseTime(String time) {
        try {
            return new SimpleDateFormat("HH:mm:ss").parse(time);
        } catch (ParseException e) {
            throw new RuntimeException(time + "时间转换错误!!" + e.getMessage());
        }
    }

    /**
     *
     * @param date
     * @param flag * @flag 0 返回yyyy-MM-dd 00:00:00日期<br>
     *                     1 返回yyyy-MM-dd 23:59:59日期
     * @return
     */
    public static Date parseTime(Date date , int flag){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        //时分秒（毫秒数）
        long millisecond = hour*60*60*1000 + minute*60*1000 + second*1000;
        //凌晨00:00:00
        cal.setTimeInMillis(cal.getTimeInMillis()-millisecond);

        if (flag == 0) {
            return cal.getTime();
        } else if (flag == 1) {
            //凌晨23:59:59
            cal.setTimeInMillis(cal.getTimeInMillis()+23*60*60*1000 + 59*60*1000 + 59*1000);
        }
        return cal.getTime();

    }

    /**
     * 获取第二天凌晨时间
     * @return
     */
    public static Date getTheTomrrowLC(Date date){
        date  = getBeforeDays(new Date(),-1)   ;
        return  parseTime(date,0);
    }

    public static String localDate2SimpleString(LocalDateTime localDateTime){
        Date date=localDate2Date(localDateTime);
        return simpleDate2String(date2SimpleDate(date));
    }

    public static String date2SimpleString(Date date){
        return simpleDate2String(date2SimpleDate(date));
    }


//    public static Date merge(Date date, Date time) {
//        Calendar instance = Calendar.getInstance();
//        instance.setTime(date);
//        instance.set(Calendar.HOUR_OF_DAY, time.getHours());
//        instance.set(Calendar.MINUTE, time.getMinutes());
//        instance.set(Calendar.SECOND, time.getSeconds());
//        return instance.getTime();
//    }


    public static LocalDateTime merge(LocalDate date, LocalTime time) {
        return LocalDateTime.of(date, time);
    }


    public static Date getTomorrowByDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR, 24);
        return calendar.getTime();
    }

    public static Date addMinutes(Date date,int minutes){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, minutes);
        return calendar.getTime();
    }

    @Deprecated
    public static DateRangeForm createDateRangeForm() {
        Date startTime = getStartTime(new Date());
        Calendar instance = Calendar.getInstance();
        instance.setTime(startTime);
        // 一年后,不严谨
        instance.add(Calendar.YEAR, 1);
        Date endTime = instance.getTime();
        return new DateRangeForm(startTime, endTime);
    }

    /**
     * 返回当前起半年时间区间
     * @return
     */
    public static DateRangeForm createHalfYearDateRangeForm() {
        YearMonth now = YearMonth.now();
        Date from = convertToDate(now.atDay(1));
        Date to = convertToDate(now.plusMonths(6).atEndOfMonth());
        return new DateRangeForm(from, to);
    }

    public static int getDayOfWeek(Date dt){
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        return cal.get(Calendar.DAY_OF_WEEK) - 1;
    }


    /**
     * 当前时间 加 几分钟
     * @param now
     * @param second
     * @return
     */
    public static Date addSecond(Date now ,int second) {
        return new Date(now .getTime() + second*1000*60);
    }

}
