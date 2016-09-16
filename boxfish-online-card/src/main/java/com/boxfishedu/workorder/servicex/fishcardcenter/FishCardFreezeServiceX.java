package com.boxfishedu.workorder.servicex.fishcardcenter;

import com.boxfishedu.workorder.common.bean.ComboTypeEnum;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.studentrelated.TimePickerService;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by hucl on 16/9/16.
 */
@Component
public class FishCardFreezeServiceX {
    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    @Autowired
    private CourseScheduleService courseScheduleService;

    @Autowired
    private WorkOrderLogService workOrderLogService;

    @Autowired
    private TimePickerService timePickerService;

    public JsonResultModel freeze(Long fishcardId){
        WorkOrder workOrder=workOrderService.findOne(fishcardId);
        if(null==workOrder){
            throw new BusinessException("不存在对应的鱼卡");
        }
        if (workOrder.getIsFreeze()==1) {
            throw new BusinessException("该鱼卡已处于冻结状态");
        }
        validateOperationCond(workOrder);

        CourseSchedule courseSchedule= courseScheduleService.findByWorkOrderId(fishcardId);
        teacherStudentRequester.notifyCancelTeacher(workOrder);

        Long teacherId=workOrder.getTeacherId();
        String teacherName=workOrder.getTeacherName();

        workOrder.setTeacherId(0l);
        workOrder.setTeacherName(null);
        courseSchedule.setTeacherId(0l);
        workOrder.setStatus(FishCardStatusEnum.COURSE_ASSIGNED.getCode());
        courseSchedule.setStatus(FishCardStatusEnum.COURSE_ASSIGNED.getCode());
        workOrder.setIsFreeze(1);
        workOrderService.saveWorkOrderAndSchedule(workOrder,courseSchedule);

        workOrderLogService.saveWorkOrderLog(workOrder,"冻结课程,冻结前教师id["+teacherId+"],教师姓名["+teacherName+"]");

        return JsonResultModel.newJsonResultModel("success");
    }

    public JsonResultModel unfreeze(Long fishcardId){
        WorkOrder workOrder=workOrderService.findOne(fishcardId);
        if(null==workOrder){
            throw new BusinessException("不存在对应的鱼卡");
        }
        if (workOrder.getIsFreeze()==0) {
            throw new BusinessException("该鱼卡未处于冻结状态,不能操作");
        }
        validateOperationCond(workOrder);

        CourseSchedule courseSchedule= courseScheduleService.findByWorkOrderId(fishcardId);


        workOrder.setIsFreeze(0);
        workOrderService.save(workOrder);

        List<CourseSchedule> list= Lists.newArrayList();
        list.add(courseSchedule);

        timePickerService.getRecommandTeachers(workOrder.getService(),list);
        workOrderLogService.saveWorkOrderLog(workOrder,"解冻结课程");

        return JsonResultModel.newJsonResultModel("success");
    }

    private void validateOperationCond(WorkOrder workOrder){
        validateComboTepe(workOrder);
        validateFreezeDate(workOrder);
    }

    private void validateComboTepe(WorkOrder workOrder) {
        if (!workOrder.getService().getComboType().equals(ComboTypeEnum.EXCHANGE.toString())) {
            throw new BusinessException("改鱼卡不是金币换课鱼卡");
        }
    }

    private void validateFreezeDate(WorkOrder workOrder){
        LocalDateTime beginLocalDate = LocalDateTime.ofInstant(DateUtil.date2SimpleDate(new Date()).toInstant(), ZoneId.systemDefault()).minusHours(24);
        if(workOrder.getStartTime().before(DateUtil.localDate2Date(beginLocalDate))){
            throw new BusinessException("只允操作上课时间为明天的鱼卡");
        }
    }
}
