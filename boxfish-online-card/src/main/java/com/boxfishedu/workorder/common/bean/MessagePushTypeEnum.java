package com.boxfishedu.workorder.common.bean;

/**
 * 消息推送类型----- 汇总
 * Created by hucl on 16/6/16.
 */
public enum MessagePushTypeEnum {
    // 抢单发送消息类型
<<<<<<< HEAD
    SEND_GRAB_ORDER_TYPE("GRABORDER");
=======
    SEND_GRAB_ORDER_TYPE("GRABORDER"),

    //当天新匹配课程消息推送
    NEW_COURSES_ASSIGNED_DAILY("NEW_COURSES_ASSIGNED_DAILY");
>>>>>>> develop

    private String value;

    MessagePushTypeEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}