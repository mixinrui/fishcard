package com.boxfishedu.workorder.common.bean;

/**
 * Created by fubenluo on 4/9/16.
 */
public enum CourseScheduleStatusEnum {

    /**
     * 10:创建,20:分配老师,30:分配学生,40:就绪,50:更换学生,60:更换老师,70:更换时间,80:更换课程,90:正在上课,100:上课完成,110:异常
     */

    CREATED(10), ASSIGNEDTEACHER(20), ASSIGNEDSTUDENT(30), READY(40), CHANGESTUDENT(50),
    CHANGETEACHER(60), CHANGETIME(70), CHANGECOURSE(80), ONCLASS(90), COMPLETED(100), EXCEPTION(110);

    private int code;

    private CourseScheduleStatusEnum(int code) {
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
