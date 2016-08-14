package com.boxfishedu.workorder.servicex.studentrelated;

import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * Created by hucl on 16/8/14.
 */
@Component
public class CommentCourseInfoServiceX {
    @Autowired
    private WorkOrderService workOrderService;

    public JsonResultModel getFishCardCourseInfo(Long workOrderId) {
        WorkOrder workOrder = workOrderService.findOne(workOrderId);
        if(null==workOrder){
            throw new BusinessException("不存在对应的鱼卡");
        }
        HashMap map= Maps.newHashMap();
        map.put("studentId",workOrder.getStudentId());
        map.put("teacherId",workOrder.getTeacherId());
        map.put("courseId",workOrder.getCourseId());
        map.put("workOrderId",workOrder.getId());
        map.put("courseName",workOrder.getCourseName());
        map.put("courseType",workOrder.getCourseType());
        return JsonResultModel.newJsonResultModel(map);
    }

}
