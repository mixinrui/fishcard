package com.boxfishedu.mall.enums;

import org.apache.commons.lang.StringUtils;

/**
 * 用于标识套餐类型与教师角色的枚举,与持久化无关,是与教学中心的一种协议
 * Created by lauzhihao on 2016/08/08.
 */
@SuppressWarnings("unused")
public enum ComboTypeToRoleId {

    OVERALL(1),
    FOREIGN(2),
    CHINESE(1),
    CRITIQUE_CN(1),
    CRITIQUE(2),
    EXCHANGE(3),
    UNKNOW(-1),
    EXPERIENCE(3),
    // 只能套餐
    INTELLIGENT(3);

    private int value;

    ComboTypeToRoleId(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static ComboTypeToRoleId resolve(String comboType) {
        if(StringUtils.isBlank(comboType)) {
            return UNKNOW;
        }
        ComboTypeToRoleId result = ComboTypeToRoleId.valueOf(comboType);
        return result == null ? UNKNOW : result;
    }

}
