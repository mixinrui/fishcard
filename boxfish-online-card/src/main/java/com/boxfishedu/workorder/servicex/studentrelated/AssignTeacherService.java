package com.boxfishedu.workorder.servicex.studentrelated;

import com.boxfishedu.workorder.common.bean.AssignTeacherApplyStatusEnum;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.StStudentApplyRecords;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.StStudentApplyRecordsService;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * Created by jiaozijun on 16/12/14.
 */
@Component
public class AssignTeacherService {


    @Autowired
    private CourseScheduleService courseScheduleService;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private StStudentApplyRecordsService stStudentApplyRecordsService;

    public JsonResultModel getAssginTeacherCourseList(Long studentId, Long teacherId, Pageable pageable) {
        Page<CourseSchedule> courseSchedulePage = courseScheduleService.findFinishCourseSchedulePage(studentId, pageable);
        trimPage(courseSchedulePage);
        return JsonResultModel.newJsonResultModel(courseSchedulePage);
    }

    private void trimPage(Page<CourseSchedule> page) {
        ((List<CourseSchedule>) page.getContent()).forEach(courseSchedule -> {
            if (courseSchedule.getId() % 2 == 0) {
                courseSchedule.setMatchStatus(1);
            } else {
                courseSchedule.setMatchStatus(2);
            }
        });
    }


    public JsonResultModel checkAssignTeacherFlag(Long workOrderId){

        StStudentApplyRecords stStudentApplyRecords =   stStudentApplyRecordsService.getStStudentApplyRecordsBy(workOrderId, AssignTeacherApplyStatusEnum.YES.getCode());
        if(null==stStudentApplyRecords){
            WorkOrder workOrder = workOrderService.findOne(workOrderId);
            if(workOrder==null)
                throw new BusinessException("课程信息有误");
            List<WorkOrder>  listWorkOrders =  workOrderService.findByStartTimeMoreThanAndSkuIdAndIsFreeze(workOrder.getStartTime(),workOrder.getSkuId());
            if(CollectionUtils.isEmpty(listWorkOrders)){
                return JsonResultModel.newJsonResultModel(false);
            }else {
                 return JsonResultModel.newJsonResultModel(true);
            }
        }else {
            return JsonResultModel.newJsonResultModel(false);
        }

    }



}
