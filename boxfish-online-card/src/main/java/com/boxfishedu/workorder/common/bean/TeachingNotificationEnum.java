package com.boxfishedu.workorder.common.bean;

/**
 * Created by hucl on 16/6/16.
 */
public enum TeachingNotificationEnum {
    TEACHER_ABSENT("ABSENT"),
    PREPARE_START_CLASS("START_CLASS");

    private String value;

    TeachingNotificationEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}