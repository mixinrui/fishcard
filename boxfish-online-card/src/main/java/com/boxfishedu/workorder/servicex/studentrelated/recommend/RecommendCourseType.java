package com.boxfishedu.workorder.servicex.studentrelated.recommend;

import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;

import static com.boxfishedu.workorder.servicex.studentrelated.recommend.CourseType.*;

/**
 * Created by LuoLiBing on 16/10/12.
 */
public class RecommendCourseType {

    // 核心素养
    private final static CourseType[] CN_RECOMMEND = {FUNCTION, FUNCTION,CONVERSATION,PHONICS,EXAMINATION,READING,FUNCTION,FUNCTION};

    private final static int CN_RECOMMEND_LEN = CN_RECOMMEND.length;

    // 终极梦想
    private final static CourseType[] FRN_RECOMMEND = { FUNCTION, FUNCTION,CONVERSATION,PHONICS,TALK,READING,FUNCTION,FUNCTION};

    private final static int FRN_RECOMMEND_LEN = FRN_RECOMMEND.length;

    private final static int CN = 0;

    private final static int FRN = 1;

    public static RecommandCourseView recommendCN(int index) {
        // workOrder起始号为1
        return recommend(CN, index - 1);
    }

    public static RecommandCourseView recommendFRN(int index) {
        // workOrder起始号为1
        return recommend(FRN, index - 1);
    }

    private static RecommandCourseView recommend(int type, int index) {
        int len;
        CourseType[] courseTypes;
        if(type == CN) {
            len = CN_RECOMMEND_LEN;
            courseTypes = CN_RECOMMEND;
        } else {
            len = FRN_RECOMMEND_LEN;
            courseTypes = FRN_RECOMMEND;
        }
        if(index < 0 || index >= len) {
            throw new BusinessException("超出可选课程推荐范围");
        }

        RecommandCourseView result = new RecommandCourseView();
        result.setCourseType(courseTypes[index].name());
        return result;
    }
}
