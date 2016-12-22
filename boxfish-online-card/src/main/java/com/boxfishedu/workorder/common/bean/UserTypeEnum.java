package com.boxfishedu.workorder.common.bean;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by LuoLiBing on 16/12/20.
 * 用户类型: 0 默认为普通用户,  1 普通会员
 *
 */
public enum UserTypeEnum {

    GENERAL_USER(0), GENERAL_MEMBER(1);

    private int type;

    UserTypeEnum(int userType) {
        this.type = userType;
    }

    public int type() {
        return type;
    }

    public static UserTypeEnum getUserTypeByComboCode(String comboCode) {
        // 1002_MEMBER_  会员
        if(StringUtils.isBlank(comboCode)) {
            return GENERAL_USER;
        }

        if(comboCode.contains("_MEMBER_")) {
            return GENERAL_MEMBER;
        }
        return GENERAL_USER;
    }
}
