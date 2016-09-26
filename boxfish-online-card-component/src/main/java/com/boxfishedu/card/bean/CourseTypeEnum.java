package com.boxfishedu.card.bean;

/**
 * Created by hucl on 16/6/16.
 */
public enum CourseTypeEnum {
    READING("READING"),
    CONVERSATION("CONVERSATION"),

    FUNCTION("FUNCTION"),
    PHONICS("PHONICS"),

    TALK("TALK"),

    EXAMINATION("EXAMINATION");

    private String value;

    CourseTypeEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}