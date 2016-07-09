package com.boxfishedu.workorder.servicex.timer;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.QueueTypeEnum;
import com.boxfishedu.workorder.common.rabbitmq.RabbitMqSender;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.ServiceSDK;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.web.param.FetchTeacherParam;
import com.boxfishedu.workorder.web.view.teacher.TeacherView;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by hucl on 16/5/6.
 * 定时器的执行程序,定时器触发发送命令
 */
@Component
public class CourseScheduleUpdatorServiceX {
    @Autowired
    private CourseScheduleService courseScheduleService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ServiceSDK serviceSDK;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private RabbitMqSender rabbitMqSender;

    @Autowired
    private WorkOrderLogService workOrderLogService;

    //定时任务，向师生运营组获取教师
//    @Scheduled(cron="*/10 * * * * ?")
    public void bathUpdateTeacherIntoSchedule() throws ParseException {
        List<CourseSchedule> courseScheduleList = courseScheduleService.findByTeacherId(CourseSchedule.NO_ASSIGN_TEACHER_ID.longValue());
        //向教师运营组发起请求获取教师
        if(CollectionUtils.isEmpty(courseScheduleList)) {
            return;
        }

        // 2 根据学生进行分组,生成获取老师的参数
        Map<Long, List<CourseSchedule>> groupCourseScheduleMap = groupByUserId(courseScheduleList);
        List<FetchTeacherParam> fetchTeacherParams = FetchTeacherParam.fetchTeacherParamList(groupCourseScheduleMap);
        if(CollectionUtils.isEmpty(fetchTeacherParams)) {
            return;
        }

        // 3 发送分配老师Message
        fetchTeacherParams.forEach(this::sendAssignTeacherMessage);
    }

    private static Map<Long, List<CourseSchedule>> groupByUserId(List<CourseSchedule> courseScheduleList) {
        Map<Long, List<CourseSchedule>> resultMap = Maps.newLinkedHashMap();
        for(CourseSchedule courseSchedule: courseScheduleList) {
            List<CourseSchedule> studentCourseScheduleList = resultMap.get(courseSchedule.getStudentId());
            if(studentCourseScheduleList == null) {
                studentCourseScheduleList = Lists.newArrayList();
                resultMap.put(courseSchedule.getStudentId(), studentCourseScheduleList);
            }
            studentCourseScheduleList.add(courseSchedule);
        }
        return resultMap;
    }


    private void sendAssignTeacherMessage(FetchTeacherParam fetchTeacherParam) {
        rabbitMqSender.send(fetchTeacherParam.convertToScheduleBatchReq(), QueueTypeEnum.ASSIGN_TEACHER);
    }


    /**
     * 处理工单与排课表, 应该在同一个事务里面...
     * @param courseScheduleId
     * @param teacher
     */
    @Transactional
    public void handleWorkOrderAndCourseSchedule(Long courseScheduleId, TeacherView teacher) {
        // 没有分配到老师不处理
        if(teacher.getTeacherId() == CourseSchedule.NO_ASSIGN_TEACHER_ID.longValue()) {
            return;
        }

        CourseSchedule courseSchedule = courseScheduleService.findOne(courseScheduleId);
        if(courseSchedule == null) {
            return;
        }
        // 修改排课表状态,保存排课表
        courseSchedule.setTeacherId(teacher.getTeacherId());
        if(courseSchedule.getWorkorderId() == null) {
            logger.error("排课表{}没有对应的工单", courseSchedule.getId());
        }
        courseSchedule.setStatus(FishCardStatusEnum.TEACHER_ASSIGNED.getCode());
        courseScheduleService.save(courseSchedule);

        // 修改工单以及状态,保存工单
        WorkOrder workOrder = workOrderService.findOne(courseSchedule.getWorkorderId());
        workOrder.setTeacherId(teacher.getTeacherId());
        workOrder.setTeacherName(teacher.getName());
        workOrder.setAssignTeacherTime(new Date());
        if(!StringUtils.isEmpty(teacher.getTeacherName())){
            workOrder.setTeacherName(teacher.getTeacherName());
        }
        workOrder.setStatus(FishCardStatusEnum.TEACHER_ASSIGNED.getCode());
        workOrderService.save(workOrder);

        // 创建群组
        serviceSDK.createGroup(workOrder);
        workOrderLogService.saveWorkOrderLog(workOrder);
    }
}
