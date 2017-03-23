package com.boxfishedu.workorder.common.bean.instanclass;

/**
 * 课程用户类型
 * jiaozijun
 */


public enum ClassUserTypeEnum {
    SUPER("SUPER"),
    DEFAULT(null);

    private String value;

    ClassUserTypeEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public static ClassUserTypeEnum getByName(String name) {
        try {
            return Enum.valueOf(ClassUserTypeEnum.class, name);
        } catch (Exception ex) {
            return DEFAULT;
        }

    }
}


