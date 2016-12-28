package com.boxfishedu.workorder.common.bean;

import com.boxfishedu.workorder.common.exception.BusinessException;

/**
 * Created by hucl on 16/12/28.
 */
public enum SmallClassType {
    SMALL("SMALL"),
    PUBLIC("PUBLIC");

    private String value;

    SmallClassType(String value) {
        this.value = value;
    }

    public static SmallClassType getByValue(String value) {
        for (SmallClassType smallClassType : SmallClassType.values()) {
            if (smallClassType.value.equals(value)) {
                return smallClassType;
            }
        }
        throw new BusinessException("不支持的小班课类型");
    }

    @Override
    public String toString() {
        return this.value;
    }
}
