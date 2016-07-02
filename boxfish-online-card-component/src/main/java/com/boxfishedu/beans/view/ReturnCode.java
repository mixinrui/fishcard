package com.boxfishedu.beans.view;

/**
 * Created by liuzhihao on 16/3/14.
 */
public enum ReturnCode {

    SUCCESS("200"), FAILED("600"), ERROR("601");

    private String code;

    ReturnCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }
}
