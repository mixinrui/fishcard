package com.boxfishedu.workorder.servicex.instantclass.grabordervalidator;

import com.boxfishedu.workorder.common.bean.instanclass.TeacherInstantClassStatus;

/**
 * Created by hucl on 16/11/4.
 */
public interface IGrabInstantClassValidator {
    default TeacherInstantClassStatus preValidate() {return TeacherInstantClassStatus.UNKNOWN;}
}
