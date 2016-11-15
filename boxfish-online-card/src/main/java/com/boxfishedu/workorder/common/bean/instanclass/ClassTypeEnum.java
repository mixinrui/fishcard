package com.boxfishedu.workorder.common.bean.instanclass;


public enum ClassTypeEnum {
    INSTNAT("INSTNAT"),
    GRAB("GRAB"),
    NORMAL("NORMAL");


    private String value;

    ClassTypeEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

}


