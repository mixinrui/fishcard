package com.boxfishedu.workorder.common.bean;

/**
 * Created by hucl on 16/8/4.
 */
public enum AppPointRecordEventEnum {
    ONLINE_COURSE_HEARTBEAT("online_course_heartbeat");

    private String code;

    private AppPointRecordEventEnum(String code) {
        this.code = code;
    }

    public String value() {
        return this.code;
    }

    @Override
    public String toString() {
        return this.code;
    }
}
