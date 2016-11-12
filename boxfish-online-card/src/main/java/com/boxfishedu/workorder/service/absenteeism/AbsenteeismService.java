package com.boxfishedu.workorder.service.absenteeism;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;

import java.util.Map;

/**
 * Created by ansel on 16/9/14.
 */
public interface AbsenteeismService {
    public Map absenteeismDeductScore(WorkOrder workOrder);

    public void queryAbsentStudent();

    public int testQueryAbsentStudent();

    public int productQueryAbsentStudent();
}
