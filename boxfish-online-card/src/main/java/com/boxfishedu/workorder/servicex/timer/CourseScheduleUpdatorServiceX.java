package com.boxfishedu.workorder.servicex.timer;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.QueueTypeEnum;
import com.boxfishedu.workorder.common.rabbitmq.RabbitMqSender;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.CourseScheduleRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.ServiceSDK;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.accountcardinfo.DataCollectorService;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.web.param.FetchTeacherParam;
import com.boxfishedu.workorder.web.view.teacher.TeacherView;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @Autowired
    private DataCollectorService dataCollectorService;

    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;

    @Autowired
    private CourseScheduleRepository courseScheduleRepository;

    //定时任务，向师生运营组获取教师
//    @Scheduled(cron="*/10 * * * * ?")
    public void bathUpdateTeacherIntoSchedule() throws ParseException {
        List<CourseSchedule> courseScheduleList = courseScheduleService.findByTeacherId(CourseSchedule.NO_ASSIGN_TEACHER_ID.longValue());
        //向教师运营组发起请求获取教师
        if (CollectionUtils.isEmpty(courseScheduleList)) {
            return;
        }

        // 2 根据学生进行分组,生成获取老师的参数
        Map<Long, List<CourseSchedule>> groupCourseScheduleMap = groupByUserId(courseScheduleList);
        List<FetchTeacherParam> fetchTeacherParams = FetchTeacherParam.fetchTeacherParamList(groupCourseScheduleMap);
        if (CollectionUtils.isEmpty(fetchTeacherParams)) {
            return;
        }

        // 3 发送分配老师Message
        fetchTeacherParams.forEach(this::sendAssignTeacherMessage);
    }

    private static Map<Long, List<CourseSchedule>> groupByUserId(List<CourseSchedule> courseScheduleList) {
        Map<Long, List<CourseSchedule>> resultMap = Maps.newLinkedHashMap();
        for (CourseSchedule courseSchedule : courseScheduleList) {
            if(courseSchedule.getIsFreeze()==1){
                continue;
            }
            List<CourseSchedule> studentCourseScheduleList = resultMap.get(courseSchedule.getStudentId());
            if (studentCourseScheduleList == null) {
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
     *
     * @param courseScheduleId
     * @param teacher
     */
    @Transactional
    public void handleWorkOrderAndCourseSchedule(Long courseScheduleId, TeacherView teacher) {
        // 没有分配到老师不处理
        if (teacher.getTeacherId() == CourseSchedule.NO_ASSIGN_TEACHER_ID.longValue()) {
            return;
        }

        CourseSchedule courseSchedule = courseScheduleService.findOne(courseScheduleId);
        if (courseSchedule == null) {
            return;
        }
        // 修改排课表状态,保存排课表
        courseSchedule.setTeacherId(teacher.getTeacherId());
        if (courseSchedule.getWorkorderId() == null) {
            logger.error("排课表{}没有对应的工单", courseSchedule.getId());
        }
        courseSchedule.setStatus(FishCardStatusEnum.TEACHER_ASSIGNED.getCode());
        courseScheduleService.save(courseSchedule);

        // 修改工单以及状态,保存工单
        WorkOrder workOrder = workOrderService.findOne(courseSchedule.getWorkorderId());
        workOrder.setTeacherId(teacher.getTeacherId());
        workOrder.setTeacherName(teacher.getName());
        workOrder.setAssignTeacherTime(new Date());
        if (!StringUtils.isEmpty(teacher.getTeacherName())) {
            workOrder.setTeacherName(teacher.getTeacherName());
        }
        workOrder.setStatus(FishCardStatusEnum.TEACHER_ASSIGNED.getCode());
        workOrderService.save(workOrder);

        dataCollectorService.updateBothChnAndFnItemAsync(workOrder.getStudentId());

        // 创建群组
        serviceSDK.createGroup(workOrder);
        workOrderLogService.saveWorkOrderLog(workOrder);
    }

    public void freezeUpdateHome(){
        logger.info("@freezeUpdateHome###########");
        List<WorkOrder> workOrders=workOrderService.findFreezeCardsToUpdate();
        if(CollectionUtils.isEmpty(workOrders)){
            return;
        }
        Set<Long> userIdSet= Sets.newHashSet();
        workOrders.forEach(workOrder -> {
            if(!userIdSet.contains(workOrder.getStudentId())) {
                userIdSet.add(workOrder.getStudentId());
            }
            workOrder.setIsCourseOver((short)1);
        });
        workOrderService.save(workOrders);
        userIdSet.forEach(userId->{
            logger.info("@freezeUpdateHome###########updateBothChnAndFnItemAsync");
            dataCollectorService.updateBothChnAndFnItemAsync(userId);
        });
    }


    /**
     * 课程推荐
     */
    public void recommendCourses() {
        // 72小时以内
        Date endDate = DateUtil.convertToDate(LocalDate.now().plusDays(3));
        // 按照学生id进行分组
        List<WorkOrder> workOrderList = workOrderJpaRepository.findWithinHoursCreatedWorkOrderList(endDate);
        List<CourseSchedule> courseScheduleList =
                courseScheduleRepository.findWithinHoursCreatedCourseScheduleList(endDate);

        Map<Long, List<WorkOrder>> workOrderMap = workOrderList.stream().collect(
                Collectors.groupingBy(WorkOrder::getStudentId, Collectors.toList()));
        Map<Long, CourseSchedule> courseScheduleMap = courseScheduleList.stream()
                .collect(Collectors.toMap(CourseSchedule::getWorkorderId, (courseSchedule -> courseSchedule)));

        ThreadPoolExecutor exec = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        workOrderMap.forEach((studentId, list) -> exec.execute(new RecommendCourseTask(list, courseScheduleMap)));

        try {
            exec.shutdown();
            String message = null;
            while (!exec.isTerminated()) {
                exec.awaitTermination(100, TimeUnit.MILLISECONDS);
                int progress = Math.round((exec.getCompletedTaskCount() * 100) / exec.getTaskCount());
                String msg = progress + "% has done," + exec.getCompletedTaskCount() + " has completed!!";
                if(!StringUtils.equals(message, msg)) {
                    System.out.println(message = msg);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("recommend courses finished!!");

        workOrderMap.forEach((studentId,list)->dataCollectorService.updateBothChnAndFnItemAsync(studentId));
    }
}
