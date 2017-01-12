package com.boxfishedu.workorder.common.bean;

/**
 * Created by LuoLiBing on 17/1/9.
 * 课程难度枚举
 */
public enum CourseDifficultyEnum {
    LEVEL1, LEVEL2, LEVEL3, LEVEL4, LEVEL5, LEVEL6, LEVEL7, LEVEl8;

    public String getPushTag() {
        return name();
    }
}
