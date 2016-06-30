package com.boxfishedu.workorder.service.studentrelated;

import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.bean.QueueTypeEnum;
import com.boxfishedu.workorder.common.rabbitmq.RabbitMqSender;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.web.param.FetchTeacherParam;
import com.boxfishedu.workorder.web.param.ScheduleModel;
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
    private Logger logger= LoggerFactory.getLogger(this.getClass());
    @Autowired
    private RabbitMqSender rabbitMqSender;
    //根据course_schedule获取教师
    public void getRecommandTeachers(Service service, List<CourseSchedule> courseSchedules) {
        FetchTeacherParam fetchTeacherParam = new FetchTeacherParam();
        List<ScheduleModel> scheduleModelList = new ArrayList<>();
        for (CourseSchedule courseSchedule : courseSchedules) {
            ScheduleModel scheduleModel = new ScheduleModel();
            scheduleModel.setId(courseSchedule.getId());
            scheduleModel.setRoleId(courseSchedule.getRoleId());
            scheduleModel.setCourseType(courseSchedule.getCourseType());
            try {
                long time = courseSchedule.getClassDate().getTime();
                logger.debug("-=-====----schedule的id:{},time为:{}",courseSchedule.getId(),time);
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
}
