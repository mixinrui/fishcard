package com.boxfishedu.workorder.servicex.timer;

import com.boxfishedu.card.bean.ServiceTimerMessage;
import com.boxfishedu.card.bean.TimerMessageType;
import com.boxfishedu.workorder.common.bean.QueueTypeEnum;
import com.boxfishedu.workorder.common.rabbitmq.RabbitMqSender;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.web.view.fishcard.TeacherAlterView;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by hucl on 16/5/28.
 */
@Component
public class ScheduleTeachersStasticsServiceX {
    @Autowired
    private CourseScheduleService courseScheduleService;
    @Autowired
    private WorkOrderService workOrderService;
    @Autowired
    private RabbitMqSender rabbitMqSender;

    public void teacherNumAlter(ServiceTimerMessage message){
        ServiceTimerMessage result=new ServiceTimerMessage();
        result.setType(TimerMessageType.TEACHER_OUT_NUM_NOTIFY_REPLY.value());
        result.setStatus(0);
        result.setBody(null);
        Date date=new Date();
        List<TeacherAlterView> teacherAlterViews=courseScheduleService.getOutNumOfTeacher(
                DateUtil.String2Date(message.getStartTime()),DateUtil.String2Date(message.getEndTime()));
        if(!CollectionUtils.isEmpty(teacherAlterViews)){
            result.setStatus(1);
            Map<Integer,Long> map= Maps.newHashMap();
            teacherAlterViews.forEach(teacherAlterView -> {
                map.put(teacherAlterView.getRoleId(),teacherAlterView.getCount());
            });
            result.setBody(map);
        }
        result.setTime(DateUtil.Date2String(new Date()));
        result.setStartTime(message.getStartTime());
        result.setEndTime(message.getEndTime());
        rabbitMqSender.send(result, QueueTypeEnum.ASSIGN_TEACHER_TIMER_REPLY);
    }
}
