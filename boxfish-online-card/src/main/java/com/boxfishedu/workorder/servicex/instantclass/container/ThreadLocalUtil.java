package com.boxfishedu.workorder.servicex.instantclass.container;

import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.web.param.InstantRequestParam;
import com.boxfishedu.workorder.web.param.TeacherInstantRequestParam;

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
}
