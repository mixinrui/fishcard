package com.boxfishedu.workorder.common.bean;

/**
 * Created by hucl on 16/6/16.
 */
public enum SlotRuleEnum {
    MUTEX("mutex"),
    RANGE("range");

    private String value;

    SlotRuleEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}