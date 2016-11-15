package com.boxfishedu.workorder.common.bean;

/**
 * 消息推送类型----- 汇总
 * Created by hucl on 16/6/16.
 */
public enum MessagePushTypeEnum {
    // 抢单发送消息类型
    SEND_GRAB_ORDER_TYPE("SEND_GRAB_ORDER_TYPE"),

    //  师生互评 消息类型 换课
    SEND_TEASTU_ASSESS_TYPE("SEND_TEASTU_ASSESS_TYPE"),


    //当天新匹配课程消息推送
    NEW_COURSES_ASSIGNED_DAILY("NEW_COURSES_ASSIGNED_DAILY"),

    // 向学生发送 明天有几节课准备上课
    SEND_STUDENT_CLASS_TOMO_TYPE("SEND_STUDENT_CLASS_TOMO_TYPE"),


    // 退款成功 消息推送
    SEND_STUDENT_CLASS_REFUND_TYPE("SEND_STUDENT_CLASS_REFUND_TYPE"),

    // 鱼卡后台换时间
    SEND_TEACHER_CHANGE_CLASSTIME_TYPE("SEND_TEACHER_CHANGE_CLASSTIME_TYPE"),

    //立即上课
    SEND_INSTANT_CLASS_TYPE("INSTANTCLASS");

    private String value;

    MessagePushTypeEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}