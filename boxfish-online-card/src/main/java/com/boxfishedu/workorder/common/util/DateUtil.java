package com.boxfishedu.workorder.common.util;

import com.boxfishedu.workorder.servicex.bean.MonthTimeSlots;
import com.boxfishedu.workorder.web.view.form.DateRangeForm;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static com.boxfishedu.workorder.servicex.instantclass.container.ThreadLocalUtil.*;

/**
 * Created by hucl on 16/3/24.
 */
public class DateUtil {

    public final static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public final static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public final static DateTimeFormatter timeFormatter1 = DateTimeFormatter.ofPattern("HH:mm");

    public static Date simpleString2Date(String simpleStr) {
        return String2Date(String.join(" ", simpleStr, "00:00:00"));
    }

    public static Date String2Date(String str) throws RuntimeException {
        try {
            return dateTimeFormat.get().parse(str);
        } catch (Exception ex) {
            throw new RuntimeException("日期格式不合法");
        }
    }

    public static LocalDateTime string2LocalDateTime(String dateStr) {
        return convertLocalDateTime(String2Date(dateStr));
    }

    public static String Date2ForForeignDate(Date date) {
        return new SimpleDateFormat(" HH:mm,MM/dd ").format(date);
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
     *
     * @param str
     * @return
     * @throws RuntimeException
     */
    public static Date String2SimpleDate(String str) throws RuntimeException {
        try {
            return dateFormat.get().parse(str);
        } catch (Exception ex) {
            throw new RuntimeException("日期格式不合法");
        }
    }

    public static Date date2SimpleDate(Date dateWithTime) {
        String dateWithTimeStr = Date2String(dateWithTime);
        return String2SimpleDate(dateWithTimeStr);
    }

    public static String dateTrimYear(Date date) {
        return StringUtils.split(DateUtil.Date2String(date), " ")[1];
    }

    public static String simpleDate2String(Date date) {
        try {
            return dateFormat.get().format(date);
        } catch (Exception ex) {
            throw new RuntimeException("日期格式不合法");
        }
    }

    public static String date2ShortString(Date date) {
        String dateStr = null;
        try {
            dateStr = new SimpleDateFormat("HH:mm").format(date);
        } catch (Exception ex) {
            throw new RuntimeException("日期格式不合法");
        }
        return dateStr;
    }

    public static String timeShortString(Date date) {
        try {
            return timeFormat.get().format(date);
        } catch (Exception ex) {
            throw new RuntimeException("日期格式不合法");
        }
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

    public static String formatLocalTime(LocalTime localTime) {
        return timeFormatter.format(localTime);
    }

    public static String formatLocalDateTime(LocalDateTime localDateTime) {
        return dateTimeFormatter.format(localDateTime);
    }

    public static Date convertToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static Date convertToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
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
     *
     * @param date
     * @return
     */
    public static String string(Date date) {
        return dateFormat.get().format(date);
    }

    //TODO:将hh:mm:ss(12进制改为HH:mm:ss:24进制)
    public static String Date2String(Date date) throws RuntimeException {
        return dateTimeFormat.get().format(date);
    }


    public static String Date2StringChinese(Date date) throws RuntimeException {
        return cnDateFormat.get().format(date);
    }


    public static String Date2String24(Date date) throws RuntimeException {
        return dateTimeFormat.get().format(date);
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
     * 获取制定时间的前 num  天
     */
    public static Date getBeforeDays(Date date, int num) {

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(calendar.DATE, -num);//把日期往后增加一天.整数往后推,负数往前移动
        return calendar.getTime();   //这个时间就是日期往后推一天的结果

    }

    public static Date localDate2Date(LocalDateTime ldt) {
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date parseTime(String time) {
        try {
            return timeFormat.get().parse(time);
        } catch (ParseException e) {
            throw new RuntimeException(time + "时间转换错误!!" + e.getMessage());
        }
    }

    /**
     * @param date
     * @param flag * @flag 0 返回yyyy-MM-dd 00:00:00日期<br>
     *             1 返回yyyy-MM-dd 23:59:59日期
     * @return
     */
    public static Date parseTime(Date date, int flag) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        //时分秒（毫秒数）
        long millisecond = hour * 60 * 60 * 1000 + minute * 60 * 1000 + second * 1000;
        //凌晨00:00:00
        cal.setTimeInMillis(cal.getTimeInMillis() - millisecond);

        if (flag == 0) {
            return cal.getTime();
        } else if (flag == 1) {
            //凌晨23:59:59
            cal.setTimeInMillis(cal.getTimeInMillis() + 23 * 60 * 60 * 1000 + 59 * 60 * 1000 + 59 * 1000);
        }
        return cal.getTime();

    }

    /**
     * 获取第二天凌晨时间
     *
     * @return
     */
    public static Date getTheTomrrowLC(Date date) {
        date = getBeforeDays(new Date(), -1);
        return parseTime(date, 0);
    }

    public static String localDate2SimpleString(LocalDateTime localDateTime) {
        Date date = localDate2Date(localDateTime);
        return simpleDate2String(date2SimpleDate(date));
    }

    public static String date2SimpleString(Date date) {
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

    public static Date addMinutes(Date date, int minutes) {
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
     * 返回当前起2个月的区间
     *
     * @return
     */
    public static DateRangeForm createHalfYearDateRangeForm(int months) {
        YearMonth now = YearMonth.now();
        Date from = convertToDate(now.atDay(1));
        Date to = convertToDate(now.plusMonths(months).atEndOfMonth());
        return new DateRangeForm(from, to);
    }

    public static int getDayOfWeek(Date dt) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        return cal.get(Calendar.DAY_OF_WEEK) - 1;
    }


    /**
     * 当前时间 加 几分钟
     *
     * @param now
     * @param second
     * @return
     */
    public static Date addSecond(Date now, int second) {
        return new Date(now.getTime() + second * 1000 * 60);
    }


    /*
    * Java代码计算时间差
    * 现在是：2004-03-26 13：31：40
    * 过去是：2004-01-02 11：30：24
    */
    public static int getBetweenDays(Date begin, Date end) {
        long l = end.getTime() - begin.getTime();
        return ((int) (l / (24 * 60 * 60 * 1000)) - 1);
    }

    public static int getBetweenMinus(Date begin, Date end) {
        long l = end.getTime() - begin.getTime();
        return ((int) (l / (60 * 1000)) - 1);
    }


    /**
     * 48小时以内
     *
     * @param dateTime
     * @return
     */
    public static boolean within48Hours(Date dateTime) {
        Duration duration = Duration.between(
                LocalDateTime.now(), DateUtil.convertLocalDateTime(dateTime));
        // 课程推荐, 否则推课程类型
        return duration.toMinutes() <= 48 * 60;
    }

    /**
     * 72小时以内
     *
     * @param dateTime
     * @return
     */
    public static boolean within72Hours(Date dateTime) {
        Duration duration = Duration.between(
                LocalDateTime.now(), DateUtil.convertLocalDateTime(dateTime));
        // 课程推荐, 否则推课程类型
        return duration.toMinutes() <= 72 * 60;
    }

    public static String formatMonthDay2String(Date date) {
        try {
            return cnYearMonthFormat.get().format(date);
        } catch (Exception ex) {
            throw new RuntimeException("日期格式不合法");
        }
    }

    public static boolean getWeekDay() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        return (w == 0 || w == 6) ? true : false;
    }

    public static boolean getWeekDay3567(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int w = cal.get(Calendar.DAY_OF_WEEK);
        return WorkOrderConstant.weekDays.contains(w) ? true : false;
    }

    // 是否包含在list的天中  2345671   周一到周日 4671
    public static boolean getWeekInByDate(Date date, List<Integer> list) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int week = cal.get(Calendar.DAY_OF_WEEK);
        if (list.contains(week))
            return true;
        return false;
    }


    //注：周日是一周的开始 获取周一的日期  week 2345671
    public static Date getMonday(Date date) throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int week = cal.get(Calendar.DAY_OF_WEEK);
        if (week >= 2) {
            cal.add(Calendar.DAY_OF_MONTH, -(week - 2));
        } else {
            cal.add(Calendar.DAY_OF_MONTH, 1 - 7);
        }
        return cal.getTime();
    }




    public static Date getAfterTomoDate(Date date) {
        return addMinutes(date, 60 * 24 * 2);
    }

    public static Date getAfter7Days(Date date, int i) {
        return (i - 1) == 0 ? date : addMinutes(date, 60 * 24 * (i - 1) * 7);
    }

    public static Date getAfterOneDay(Date date, int i) {
        return addMinutes(date, 60 * 24 * i);
    }

    private final static long DAY_OF_SECONDS = 24 * 60 * 60 * 1000;

    public static long durationOfDay(Date from, Date to) {
        return (to.getTime() - from.getTime()) / DAY_OF_SECONDS;
    }

//    public static void main(String[] args) throws ParseException {
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//        Date from = format.parse("2016-12-10");
//        System.out.println(durationOfDay(from, new Date()));
//    }

    public static Date getNextWeekSunday(Date date) {

        int mondayPlus = getMondayPlus();
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.add(GregorianCalendar.DATE, mondayPlus + 7 + 6);
        Date monday = currentDate.getTime();
        DateFormat df = DateFormat.getDateInstance();
        String preMonday = df.format(monday);
        return parseTime(monday, 1);
    }

    public static LocalDate getFirstDateOfWeek(LocalDate localDate) {
        int value = localDate.getDayOfWeek().getValue();
        return localDate.minusDays(value - 1);
    }

    public static LocalDate getLastDateOfWeek(LocalDate localDate) {
        int value = localDate.getDayOfWeek().getValue();
        return localDate.plusDays(7 - value);
    }

    private static int getMondayPlus() {
        Calendar cd = Calendar.getInstance();
        // 获得今天是一周的第几天，星期日是第一天，星期二是第二天......
        int dayOfWeek = cd.get(Calendar.DAY_OF_WEEK) - 1;         //因为按中国礼拜一作为第一天所以这里减1
        if (dayOfWeek == 1) {
            return 0;
        } else {
            return 1 - dayOfWeek;
        }
    }


    // 是否在同一天
    public static boolean isSameDate(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        boolean isSameYear = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
        boolean isSameMonth = isSameYear && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
        boolean isSameDate = isSameMonth && cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
        return isSameDate;
    }


    public static void main(String[] args) throws Exception {
        Date date1 = DateTime.now().toDate();
        Date date2 =DateTime.now().minusHours(15).toDate();
                System.out.println(isSameDate(date1,date2));
    }

}
