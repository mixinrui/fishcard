package com.boxfishedu.workorder.common.bean;

/**
 * Created by LuoLiBing on 17/1/11.
 */
public enum PublicClassInfoStatusEnum {

    ENTER(100), QUIT(200);

    public final int code;

    PublicClassInfoStatusEnum(int code) {
        this.code = code;
    }
}
