package com.boxfishedu.workorder.servicex.instantclass.tutorrange;

import com.boxfishedu.workorder.common.bean.TutorTypeEnum;

/**
 * Created by hucl on 16/12/21.
 */
public class InstantRangeEnum {
    public static final String SUFFIX="_RANGE";
    public static final String CN_RANGE="CN_RANGE";
    public static final String FRN_RANGE="FRN_RANGE";

    public static String tutorType2Range(TutorTypeEnum tutorTypeEnum){
        return tutorTypeEnum.toString()+SUFFIX;
    }
}
