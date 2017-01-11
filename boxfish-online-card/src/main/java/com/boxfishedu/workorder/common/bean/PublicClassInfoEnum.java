package com.boxfishedu.workorder.common.bean;

/**
 * Created by LuoLiBing on 17/1/11.
 */
public enum PublicClassInfoEnum {

    ENTER(100), QUIT(200);

    public final int code;

    PublicClassInfoEnum(int code) {
        this.code = code;
    }
}
