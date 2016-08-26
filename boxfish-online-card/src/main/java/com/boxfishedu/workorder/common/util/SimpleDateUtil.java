package com.boxfishedu.workorder.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ansel on 16/8/26.
 */
public class SimpleDateUtil {
    public final static SimpleDateFormat yearFormat = new SimpleDateFormat("YYYY");
    public final static SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
    public final static SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
    public final static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    public static String getYearFromDate(Date date){
        String dateStr = null;
        try {
            dateStr = yearFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateStr;
    }

    public static String getMonthFromDate(Date date){
        String dateStr = null;
        try {
            dateStr = monthFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateStr;
    }

    public static String getDayFromDate(Date date){
        String dateStr = null;
        try {
            dateStr = dayFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateStr;
    }

    public static String getTimeFromDate(Date date){
        String dateStr = null;
        try {
            dateStr = timeFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateStr;
    }

    public static String getEnglishMonth1FromDate(Date date){
        String dateStr = null;
        try {
            String monthStr = getMonthFromDate(date);
            switch (monthStr){
                case "1":
                case "01":
                    dateStr = "January";
                    break;
                case "2":
                case "02":
                    dateStr = "February";
                    break;
                case "3":
                case "03":
                    dateStr = "March";
                    break;
                case "4":
                case "04":
                    dateStr = "April";
                    break;
                case "5":
                case "05":
                    dateStr = "May";
                    break;
                case "6":
                case "06":
                    dateStr = "June";
                    break;
                case "7":
                case "07":
                    dateStr = "July";
                    break;
                case "8":
                case "08":
                    dateStr = "August";
                    break;
                case "9":
                case "09":
                    dateStr = "September";
                    break;
                case "10":
                    dateStr = "October";
                    break;
                case "11":
                    dateStr = "November";
                    break;
                case "12":
                    dateStr = "December";
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateStr;
    }

    public static String getEnglishMonth2FromDate(Date date){
        String dateStr = null;
        try {
            String monthStr = getMonthFromDate(date);
            switch (monthStr){
                case "1":
                case "01":
                    dateStr = "Jan.";
                    break;
                case "2":
                case "02":
                    dateStr = "Feb.";
                    break;
                case "3":
                case "03":
                    dateStr = "Mar.";
                    break;
                case "4":
                case "04":
                    dateStr = "Apr.";
                    break;
                case "5":
                case "05":
                    dateStr = "May.";
                    break;
                case "6":
                case "06":
                    dateStr = "June.";
                    break;
                case "7":
                case "07":
                    dateStr = "July.";
                    break;
                case "8":
                case "08":
                    dateStr = "Aug.";
                    break;
                case "9":
                case "09":
                    dateStr = "Sept.";
                    break;
                case "10":
                    dateStr = "Oct.";
                    break;
                case "11":
                    dateStr = "Nov.";
                    break;
                case "12":
                    dateStr = "Dec.";
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateStr;
    }

    public static String getEnglishDate1(Date date){
        String dateStr = null;
        try {
            dateStr = getEnglishMonth1FromDate(date) + " " + getDayFromDate(date) +","+getYearFromDate(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateStr;
    }

    public static String getEnglishDate2(Date date){
        String dateStr = null;
        try {
            dateStr = getEnglishMonth2FromDate(date) + " " + getDayFromDate(date) +","+getYearFromDate(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateStr;
    }
}
