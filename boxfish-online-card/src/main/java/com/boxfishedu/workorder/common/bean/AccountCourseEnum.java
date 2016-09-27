package com.boxfishedu.workorder.common.bean;

/**
 * Created by hucl on 16/9/26.
 */
public enum AccountCourseEnum {
    FOREIGN("FOREIGN"),
    CHINESE("CHINESE"),
    CRITIQUE("CRITIQUE");

    private String value;

    AccountCourseEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
