package com.boxfishedu.workorder.common.bean;

/**
 * Created by LuoLiBing on 17/1/9.
 * 课程难度枚举
 */
public enum CourseDifficultyEnum {
    LEVEL1("PUBLIC_CLASS_LEVEL1"),
    LEVEL2("PUBLIC_CLASS_LEVEL2"),
    LEVEL3("PUBLIC_CLASS_LEVEL3"),
    LEVEL4("PUBLIC_CLASS_LEVEL4"),
    LEVEL5("PUBLIC_CLASS_LEVEL5"),
    LEVEL6("PUBLIC_CLASS_LEVEL6"),
    LEVEL7("PUBLIC_CLASS_LEVEL7"),
    LEVEl8("PUBLIC_CLASS_LEVEL8");

    public final String pushCode;

    CourseDifficultyEnum(String pushCode) {
        this.pushCode = pushCode;
    }
}
