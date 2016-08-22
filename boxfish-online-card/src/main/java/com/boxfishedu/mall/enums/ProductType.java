package com.boxfishedu.mall.enums;

/**
 * Created by LuoLiBing on 16/8/20.
 */
public enum ProductType {

    TEACHING(1001), COMMENT(1002);

    int value;

    ProductType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
