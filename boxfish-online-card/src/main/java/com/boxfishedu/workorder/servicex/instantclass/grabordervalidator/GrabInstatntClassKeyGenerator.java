package com.boxfishedu.workorder.servicex.instantclass.grabordervalidator;

import com.boxfishedu.workorder.web.param.TeacherInstantRequestParam;

/**
 * Created by hucl on 16/11/9.
 */
public class GrabInstatntClassKeyGenerator {
    public static String generateKey(TeacherInstantRequestParam teacherInstantRequestParam) {
        return new StringBuilder("grapcard.instantcard:")
                .append(teacherInstantRequestParam.getCardId())
                .toString();
    }

    public static String matchedKey(Long cardId){
        return new StringBuilder("matched.instantcard:")
                .append(cardId)
                .toString();
    }
}
