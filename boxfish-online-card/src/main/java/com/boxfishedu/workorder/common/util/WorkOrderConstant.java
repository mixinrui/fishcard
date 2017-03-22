package com.boxfishedu.workorder.common.util;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * 鱼卡常量类
 * Created by jiaozijun on 16/7/12.
 */
public class WorkOrderConstant {

    // 抢单发送消息内容
    public final static String SEND_GRAB_ORDER_MESSAGE = "有学生等待上课,打开app抢先上课";

    // 抢单发送消息内容外教
    public final static String SEND_GRAB_ORDER_MESSAGE_FOREIGH = "A student is waiting for the class; Open the APP, and enjoy your class now！";

    // 鱼卡信息列表
    public final static String FISHCARD_LIST = "鱼卡信息列表";
    // 抢单成功
    public final static String GRABORDER_SUCCESS = "抢单成功";
    // 抢单失败
    public final static String GRABORDER_FAIL = "抢单失败";


    /**
     * begin 课程变更消息发送
     **/
    public final static String SEND_CHANGE_COURSE_MESSAGE_BEGIN = "您的课表有";

    public final static String SEND_CHANGE_COURSE_MESSAGE_END = "节课程更新了,请提前下载.";

    public final static String SEND_CHANGE_COURSE_MESSAGE_FOREIGN_BEGIN = "";

    public final static String SEND_CHANGE_COURSE_MESSAGE_FOREIGN_END = "classes on your schedule have been updated. Please download in advance.";
    /** end 课程变更消息发送 **/


    /**
     * begin 向学生发送提醒 明天有课
     **/
    public final static String SEND_STU_CLASS_TOMO_MESSAGE_BEGIN = "【课程通知】同学好，你明天有";

    public final static String SEND_STU_CLASS_TOMO_MESSAGE_END = "节课程，请提前做好上课准备~";
    /** end 向学生发送提醒 明天有课 **/


    /**
     * begin  退款成功消息  您好，您于2016年8月10号所上课程《超能战队》，因(不可抗力因素/老师旷课/老师早退)未完成课程，经审核已将课程退款退至您的付款账户，请查收。
     **/
    public final static String SEND_STU_CLASS_REFUND_ONE = "您好，您于";
    public final static String SEND_STU_CLASS_REFUND_TWO = "所上课程《";
    public final static String SEND_STU_CLASS_REFUND_THREE = "》，因";
    public final static String SEND_STU_CLASS_REFUND_FOUR = "未完成课程，经审核已将课程退款退至您的付款账户，请查收。";

    /** end  退款成功消息  **/


    /**
     * begin 鱼卡换时间
     **/
    public final static String SEND_TEACHER_CHANGETIME_BEGIN = "The lesson at ";
    public final static String SEND_TEACHER_CHANGETIME_END = ", is canceled, for the student has changed the lesson time.";
    /**
     * end 鱼卡换时间
     **/

    //指定老师消息推送
    //public final static String SEND_ASSIGN_TEACHER ="A student would like to have class with you. Please check your class schedule and accept their class request.";

    public final static String SEND_ASSIGN_TEACHER = "A student wants to fix classes with you! (Please ensure your APP is updated)";


    public final static List<Integer> weekDays = Lists.newArrayList(4, 6, 7, 1);
    /**
     * 用于判断小班课 周几   周3 周五  六日
     **/
    public final static List<Integer> slots = Lists.newArrayList(27,28);  // 每天的时间片

    public static boolean checkContains(Integer slot) {
        for (Integer s :  slots) {
            if (s.longValue() == slot.longValue()) {
                return true;
            }
        }
        return false;
    }

    // 上课前五分钟 显示开始上课按钮
    public final static Integer FIVE_MINUTE_BEFORE_CLASSS = 5;
}
