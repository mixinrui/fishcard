package com.boxfishedu.workorder.common.util;

import com.boxfishedu.workorder.common.bean.multiteaching.SmallClassType;

/**
 * Created by hucl on 16/5/10.
 */
public class ConstantUtil {
    public static final String BEGIN_TIME_POSTFIX_WITH_BLANK=" 00:00:00";
    public static final String END_TIME_POSTFIX_WITH_BLANK=" 23:59:59";
    public static final Integer DAY_OF_WEEK=7;


    public static final String EARLIEST_TIME = "2016-01-01 00:00:00";
    //这么久应该够了吧
    public static final String LATEST_TIME = "2199-00-00 00:00:00";

    //用1表示外教
    public static final Integer SKU_EXTRA_VALUE=1;

    public static final String RABBITMQ_PROPERTIES_BEAN="RABBITMQ_PROPERTIES_BEAN";

    public static final Integer TEACHER_TYPE_EXTRA_FOREIGNER=1;
    public static final Integer TEACHER_TYPE_EXTRA_CHINESE=0;

    public static final Integer WORKORDER_SELECTED=30;
    public static final Integer WORKORDER_COMPLETED=40;

    public static final String REDIS_KEY_FLAG_ORDER2SERVICE="ORDER2SERVICE";

    public static final String THREADPOOL_ORDER2SERVICE="THREADPOOL_ORDER2SERVICE";
    public static final String THREADPOOL_LOG="THREADPOOL_LOG";
    public static final String THREADPOOL_ASYNC_NOTIFY="THREADPOOL_ASYNC_NOTIFY";
    public static final String STUDENT_CHANNLE = "ss_manual";
    public static final String TEACHER_CHANNLE = "st_manual";
    public static final String TIMER_CHANNLE = "auto";
    public static final String MANUAL_OPERATOR ="manual";//OperateType
    public static final String MANUAL_TECH_OPERATOR ="agree";//OperateType
    public static final String TIMER_OPERATOR ="auto";//OperateType

    public static final String SMALL_CLASS_INIT="INIT_SMALL";
    public static final String PUBLIC_CLASS_INIT="INIT_PUBLIC";
}
