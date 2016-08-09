package com.boxfishedu.card.bean;

/**
 * Created by hucl on 16/6/10.
 */
public enum TimerMessageType {

    TEACHER_ASSIGN_NOTIFY(10),
    TEACHER_ASSIGN_NOTIFY_REPLY(11),
    TEACHER_OUT_NUM_NOTIFY(20),
    TEACHER_OUT_NUM_NOTIFY_REPLY(21),
    TEACHER_PREPARE_CLASS_NOTIFY(30),
    TEACHER_ABSENT_QUERY_NOTIFY(40),
    STUDENT_ABSENT_QUERY_NOTIFY(50),
    COMPLETE_FORCE_SERVER_NOTIFY(60),
    //学生没有补课的清空操作定时器
    STUDENT_NOT_MAKEUP_NOTIFY(70),
    //教师当天新匹配到的课程
    TEACHER_COURSE_NEW_ASSIGNEDED_DAY(80),
    //抢单初始化数据队列
    GRAB_ORDER_DATA_INIT(90),
    GRAB_ORDER_DATA_INIT_FOREIGH(91),
    //抢单格式化数据队列(清理历史数据中教)
    GRAB_ORDER_DATA_CLEAR_DAY(100),

    //抢单格式化数据队列(清理历史数据外教)
    GRAB_ORDER_DATA_CLEAR_DAY_FOREIGH(101);




    private int code;

    private TimerMessageType(int code) {
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
