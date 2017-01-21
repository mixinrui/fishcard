package com.boxfishedu.workorder.service.studentrelated;

import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.bean.QueueTypeEnum;
import com.boxfishedu.workorder.common.rabbitmq.RabbitMqSender;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.web.param.FetchTeacherParam;
import com.boxfishedu.workorder.web.param.ScheduleModel;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hucl on 16/6/16.
 */
@Component
public class TimePickerService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private RabbitMqSender rabbitMqSender;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private CourseScheduleService courseScheduleService;

    //根据course_schedule获取教师
    public void getRecommandTeachers(Service service, List<CourseSchedule> courseSchedules) {
        FetchTeacherParam fetchTeacherParam = new FetchTeacherParam();
        List<ScheduleModel> scheduleModelList = new ArrayList<>();
        for (CourseSchedule courseSchedule : courseSchedules) {
            WorkOrder workOrder = workOrderService.findOne(courseSchedule.getWorkorderId());
            // 如果是冻结的, 或者是小班课, 不分配老师
            if (workOrder.getIsFreeze() == 1 ||
                    StringUtils.equals(workOrder.getClassType(), ClassTypeEnum.SMALL.name())) {
                continue;
            }
            ScheduleModel scheduleModel = new ScheduleModel();
            scheduleModel.setId(courseSchedule.getId());
            scheduleModel.setRoleId(courseSchedule.getRoleId());
            scheduleModel.setCourseType(courseSchedule.getCourseType());
            try {
                long time = courseSchedule.getClassDate().getTime();
                logger.debug("-=-====----schedule的id:{},time为:{}", courseSchedule.getId(), time);
                scheduleModel.setDay(time);
            } catch (Exception ex) {
                throw new BusinessException("日期格式不合法");
            }
            scheduleModel.setSlotId(courseSchedule.getTimeSlotId());
            scheduleModelList.add(scheduleModel);
        }
        fetchTeacherParam.setScheduleModelList(scheduleModelList);
        fetchTeacherParam.setUserId(service.getStudentId());
        rabbitMqSender.send(fetchTeacherParam.convertToScheduleBatchReq(), QueueTypeEnum.ASSIGN_TEACHER);
    }

    public void getRecommandTeachers(WorkOrder workOrder) {
        List<CourseSchedule> list = Lists.newArrayList();
        CourseSchedule courseSchedule = courseScheduleService.findByWorkOrderId(workOrder.getId());
        list.add(courseSchedule);
        this.getRecommandTeachers(workOrder.getService(), list);
    }
}
