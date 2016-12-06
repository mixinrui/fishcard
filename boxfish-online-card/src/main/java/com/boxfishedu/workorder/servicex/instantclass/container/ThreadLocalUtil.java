package com.boxfishedu.workorder.servicex.instantclass.container;

import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.web.param.InstantRequestParam;
import com.boxfishedu.workorder.web.param.TeacherInstantRequestParam;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by hucl on 2016/11/6.
 */
public class ThreadLocalUtil {
    public static final ThreadLocal<WorkOrder> latestWorkOrderThreadLocal = new ThreadLocal<WorkOrder>();
    public static final ThreadLocal<InstantRequestParam> instantRequestParamThreadLocal = new ThreadLocal<>();
    public static final ThreadLocal<TeacherInstantRequestParam> TeacherInstantParamThreadLocal = new ThreadLocal<>();
    public static final ThreadLocal<Date> classDateIn30Minutes = new ThreadLocal<>();
    public static final ThreadLocal<InstantClassCard> instantCardMatched30Minutes = new ThreadLocal<>();
    public static final ThreadLocal<String> unFinishedCourses30MinutesTips = new ThreadLocal<>();

    public static TeacherInstantRequestParam getTeacherInstantParam(){
        return TeacherInstantParamThreadLocal.get();
    }

    public final static ThreadLocal<DateFormat> dateFormat = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };

    public final static ThreadLocal<DateFormat> dateTimeFormat = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };

    public final static ThreadLocal<DateFormat> timeFormat = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("HH:mm:ss");
        }
    };


    public final static ThreadLocal<DateFormat> cnYearMonthFormat = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("MM月dd日");
        }
    };

    public final static ThreadLocal<DateFormat> cnDateFormat = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy年MM月dd日");
        }
    };
}
