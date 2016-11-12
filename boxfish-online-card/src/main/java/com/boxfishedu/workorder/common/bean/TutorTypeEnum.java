package com.boxfishedu.workorder.common.bean;

/**
 * Created by hucl on 16/6/16.
 */
public enum TutorTypeEnum {
    CN("CN"),
    FRN("FRN"),
    MIXED("MIXED");

    private String value;

    TutorTypeEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}