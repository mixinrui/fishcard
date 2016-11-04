package com.boxfishedu.workorder.servicex.instantclass.instantvalidator;

import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.servicex.studentrelated.selectmode.SelectMode;
import com.boxfishedu.workorder.web.param.InstantRequestParam;
import com.boxfishedu.workorder.web.param.TimeSlotParam;

import java.util.List;

/**
 * Created by hucl on 16/11/4.
 */
public interface InstantClassValidator {
    default int preValidate(InstantRequestParam instantRequestParam) {return 0;}
}
