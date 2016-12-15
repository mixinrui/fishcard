package com.boxfishedu.workorder.servicex.studentrelated;

import com.boxfishedu.workorder.common.bean.AssignTeacherApplyStatusEnum;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.StStudentApplyRecords;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.StStudentApplyRecordsService;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.web.param.ScheduleBatchReqSt;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.google.common.collect.Lists;
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

    //2 获取指定老师带的课程列表
    public JsonResultModel getAssginTeacherCourseList(Long oldWorkOrderId ,Long studentId, Long teacherId, Pageable pageable) {
        Page<CourseSchedule> courseSchedulePage = courseScheduleService.findFinishCourseSchedulePage(studentId, pageable);
        trimPage(courseSchedulePage);
        return JsonResultModel.newJsonResultModel(courseSchedulePage);
    }



    // 2.1 组装向师生运营发送的消息内容   teacherId 指定的老师id
    public ScheduleBatchReqSt makeScheduleBatchReqSt(Long oldWorkOrderId,Long teacherId){
        ScheduleBatchReqSt scheduleBatchReqSt = new ScheduleBatchReqSt();
        List<WorkOrder> workOrders =   this.getAssignTeacherList(oldWorkOrderId);
        if(CollectionUtils.isEmpty(workOrders))
            return null;

        // 获取和该 workOrders 未冻结 开始时间相同 并且为指定老师的鱼卡信息
        List startTimelist = Lists.newArrayList();
        workOrders.forEach(w->{
            startTimelist.add(w.getStartTime());
        });
        List<WorkOrder> workOrdersB = workOrderService.getMatchWorkOrders(teacherId,startTimelist);

        return scheduleBatchReqSt;
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
        List<WorkOrder> workOrders =   this.getAssignTeacherList(workOrderId);
        if(CollectionUtils.isEmpty(workOrders)){
            return JsonResultModel.newJsonResultModel(false);
        }else {
            return JsonResultModel.newJsonResultModel(true);
        }

    }



    public List<WorkOrder> getAssignTeacherList(Long workOrderId){
        StStudentApplyRecords stStudentApplyRecords =   stStudentApplyRecordsService.getStStudentApplyRecordsBy(workOrderId, AssignTeacherApplyStatusEnum.YES.getCode());
        if(null==stStudentApplyRecords){
            WorkOrder workOrder = workOrderService.findOne(workOrderId);
            if(workOrder==null)
                throw new BusinessException("课程信息有误");
            List<WorkOrder>  listWorkOrders =  workOrderService.findByStartTimeMoreThanAndSkuIdAndIsFreeze(workOrder);
            if(CollectionUtils.isEmpty(listWorkOrders)){
                return null;
            }else {
                return listWorkOrders;
            }
        }else {
            return null;
        }
    }


}
