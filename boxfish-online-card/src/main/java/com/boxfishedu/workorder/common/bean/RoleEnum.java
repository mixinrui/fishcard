package com.boxfishedu.workorder.common.bean;

/**
 * Created by hucl on 17/1/14.
 */
public enum RoleEnum {
    STUDENT("STUDENT"),
    TEACHER("TEACHER");

    private String code;

    private RoleEnum(String code) {
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
