package com.boxfishedu.workorder.common.bean;

/**
 * Created by hucl on 16/6/16.
 */
public enum ComboTypeEnum {
    // 核心素养
    OVERALL("OVERALL"),
    //外教
    FOREIGN("FOREIGN"),
    //外教点评
    CRITIQUE("CRITIQUE"),
    //金币换课
    EXCHANGE("EXCHANGE"),
    //终极梦想
    CHINESE("CHINESE"),
    //免费体验
    EXPERIENCE("EXPERIENCE"),

    SMALLCLASS("SMALLCLASS"),

    REWARD("REWARD"),
    //终极梦想+考试指导
    INTELLIGENT("INTELLIGENT"),

    FSCF("FSCF"),

    FSCC("FSCC");

    private String value;

    ComboTypeEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}