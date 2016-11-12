package com.boxfishedu.workorder.common.bean;

/**
 * Created by hucl on 16/4/15.
 */
public enum QueueTypeEnum {


    ORDER2SERVICE(1),TEACHING_ONLINE(2),TEACHING_SERVICE(3),NOTIFY_ORDER(4),NOTIFY_TIMER(5), ASSIGN_TEACHER(6), ASSIGN_TEACHER_REPLY(7),
    CREATE_GROUP(8),ASSIGN_TEACHER_TIMER_REPLY(9),TEACHER_OUT_NUMBER_TIMER(10),TEACHER_OUT_NUMBER_TIMER_REPLY(11),ASSIGN_FOREIGN_TEACHER_COMMENT(12),
    RECHARGE_ORDER(13),
    SHORT_MESSAGE(14),  //发送短信
    ASYNC_COMMENT_CARD_CUSTOMER_SERVICE(15),  //同步外教点评到客服系统
    ASYNC_NOTIFY_CUSTOMER_SERVICE(20)
    ;

    private int code;

    private QueueTypeEnum(int code) {
        this.code = code;
    }

    public int value() {
        return this.code;
    }

    @Override
    public String toString() {
        return String.valueOf(this.code);
    }
}
