package com.boxfishedu.workorder.common.bean;

import java.util.Arrays;
import java.util.List;

import static com.boxfishedu.workorder.common.bean.CourseDifficultyEnum.*;

/**
 * Created by LuoLiBing on 17/1/9.
 * 上课时间设置枚举类
 *
 */
public enum PublicClassTimeEnum {

    LEVEL_2(LEVEL1, LEVEL2),

    LEVEL_3(LEVEL3),

    LEVEL_4(LEVEL4),

    LEVEL_5(LEVEL5, LEVEL6, LEVEL7, LEVEl8);

    public final List<CourseDifficultyEnum> difficulties;

    PublicClassTimeEnum(CourseDifficultyEnum...difficulties) {
        this.difficulties = Arrays.asList(difficulties);
    }
}
