package com.boxfishedu.workorder.common.bean;

/**
 * 课程类型
 * Created by jiaozijun on 17/2/28.
 */

public enum ChannelTypeEnum {
    COURSE_CHINESE_1P("COURSE_CHINESE_1P","中教1对1"),
    COURSE_CHINESE_4P("COURSE_CHINESE_4P","中教小班课"),
    COURSE_CHINESE_OPEN("COURSE_CHINESE_OPEN","中教公开课"),

    COURSE_FOREIGN_1P("COURSE_FOREIGN_1P","外教1对1"),
    COURSE_FOREIGN_4P("COURSE_FOREIGN_4P","外教小班课"),
    COURSE_FOREIGN_OPEN("COURSE_FOREIGN_OPEN","外教公开课");

    private String value;

    private String desc;

    ChannelTypeEnum(String value,String desc) {
        this.value = value;
        this.desc =desc;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
