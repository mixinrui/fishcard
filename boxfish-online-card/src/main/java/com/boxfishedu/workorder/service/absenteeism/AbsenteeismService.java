package com.boxfishedu.workorder.service.absenteeism;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;

/**
 * Created by ansel on 16/9/14.
 */
public interface AbsenteeismService {
    public JsonResultModel absenteeismDeductScore(WorkOrder workOrder);

    public void queryAbsentStudent();
}
