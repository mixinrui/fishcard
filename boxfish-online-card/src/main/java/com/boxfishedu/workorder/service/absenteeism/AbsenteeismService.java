package com.boxfishedu.workorder.service.absenteeism;

import com.boxfishedu.beans.view.JsonResultModel;

/**
 * Created by ansel on 16/9/14.
 */
public interface AbsenteeismService {
    public JsonResultModel absenteeismDeductScore(Long studentId);

    public JsonResultModel queryAbsentStudent();
}
