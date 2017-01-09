package com.boxfishedu.workorder.common.bean.instanclass;


import com.boxfishedu.mall.enums.ComboTypeToRoleId;

public enum ClassTypeEnum {
    INSTNAT("INSTNAT"),
    GRAB("GRAB"),
    // 正常课
    NORMAL("NORMAL"),
    // 小班课
    SMALL("SMALL"),
    // 公开课
    PUBLIC("PUBLIC"),
    DEFAULT(null);

    private String value;

    ClassTypeEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public static ClassTypeEnum resolveByComboType(String comboType) {
        ComboTypeToRoleId comboTypeToRoleId;
        try {
            comboTypeToRoleId = Enum.valueOf(ComboTypeToRoleId.class, comboType);
        } catch (Exception e) {
            return DEFAULT;
        }
        switch (comboTypeToRoleId) {
            case SMALLCLASS: return SMALL;
            case PUBLIC: return PUBLIC;
            default: return NORMAL;
        }
    }
}


