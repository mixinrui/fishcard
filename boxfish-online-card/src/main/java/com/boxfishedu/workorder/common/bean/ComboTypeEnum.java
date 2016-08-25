package com.boxfishedu.workorder.common.bean;

/**
 * Created by hucl on 16/6/16.
 */
public enum ComboTypeEnum {
    OVERALL("OVERALL"),
    FOREIGN("FOREIGN"),
    CRITIQUE("CRITIQUE"),
    EXCHANGE("EXCHANGE");

    private String value;

    ComboTypeEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}