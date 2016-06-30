package com.boxfishedu.workorder.common.bean;

/**
 * Created by hucl on 16/3/19.
 */
public enum OrderConvertEnum {
    WAIT_PROCESS(-1), PROCESSING(0), PROCESSED(1);
    private int code;

    private OrderConvertEnum(int code) {
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
