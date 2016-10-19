package com.boxfishedu.workorder.servicex.timer;

import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.CourseScheduleRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mongo.ScheduleCourseInfo;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.ScheduleCourseInfoService;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.servicex.studentrelated.recommend.DefaultRecommendHandler;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by LuoLiBing on 16/10/19.
 */
@Component
public class RecommendCourseTask implements Runnable {

    private static DefaultRecommendHandler defaultRecommendHandler;

    private static WorkOrderService workOrderService;

    private static WorkOrderJpaRepository workOrderJpaRepository;

    private static WorkOrderLogService workOrderLogService;

    private static ScheduleCourseInfoService scheduleCourseInfoService;

    private static UrlConf urlConf;

    private static CourseScheduleRepository courseScheduleRepository;

    @Autowired
    public void autowired(DefaultRecommendHandler defaultRecommendHandler, WorkOrderService workOrderService,
                          WorkOrderJpaRepository workOrderJpaRepository, WorkOrderLogService workOrderLogService,
                          ScheduleCourseInfoService scheduleCourseInfoService, UrlConf urlConf,
                          CourseScheduleRepository courseScheduleRepository) {
        RecommendCourseTask.defaultRecommendHandler = defaultRecommendHandler;
        RecommendCourseTask.workOrderService = workOrderService;
        RecommendCourseTask.workOrderJpaRepository = workOrderJpaRepository;
        RecommendCourseTask.workOrderLogService = workOrderLogService;
        RecommendCourseTask.scheduleCourseInfoService = scheduleCourseInfoService;
        RecommendCourseTask.courseScheduleRepository = courseScheduleRepository;
        RecommendCourseTask.urlConf = urlConf;
    }

    private List<WorkOrder> workOrders;

    private Map<Long,CourseSchedule> courseScheduleMap;

    public RecommendCourseTask() {}

    public RecommendCourseTask(List<WorkOrder> workOrders, Map<Long,CourseSchedule> courseScheduleMap) {
        this.workOrders = workOrders;
        this.courseScheduleMap = courseScheduleMap;
    }

    @Override
    @Transactional
    public void run() {
        for(int i = 0, size = workOrders.size(); i < size; i++) {
            singleRecommend(workOrders.get(i), courseScheduleMap.get(workOrders.get(i).getId()));
        }
    }

    /**
     * 单课程推荐
     * @param workOrder
     * @param courseSchedule
     */
    public static void singleRecommend(WorkOrder workOrder, CourseSchedule courseSchedule) {
        Map<Integer, RecommandCourseView> recommendCourseMap =
                defaultRecommendHandler.recommendCourse(
                        Collections.singletonList(workOrder),
                        (w -> DateUtil.within72Hours(w.getStartTime())));
        RecommandCourseView courseView = recommendCourseMap.get(workOrder.getSeqNum());

        // 如果已经分配老师,课程类型如果与课程推荐不匹配重新更换老师
        if(!StringUtils.equals(courseView.getCourseType(), workOrder.getCourseType())
                && !Objects.isNull(workOrder.getTeacherId())) {
            workOrderService.changeTeacherForTypeChanged(workOrder);
        }

        // 保存鱼卡
        workOrder.initCourseInfo(courseView);
        workOrderJpaRepository.save(workOrder);

        // 记鱼卡日志
        workOrderLogService.saveWorkOrderLog(workOrder,
                String.format("定时课程推荐,类型:[%s],课程Id:[%s],课程名:[%s]",
                        courseView.getCourseType(), courseView.getCourseId(), courseView.getCourseName()));

        // 保存mongo课程信息
        ScheduleCourseInfo scheduleCourseInfo = scheduleCourseInfoService.queryByWorkId(workOrder.getId());
        if(scheduleCourseInfo == null) {
            scheduleCourseInfo = new ScheduleCourseInfo(workOrder.getId(), courseSchedule.getId());
        }
        scheduleCourseInfo.initRecommendCourse(urlConf.getThumbnail_server(), courseView);
        scheduleCourseInfoService.save(scheduleCourseInfo);

        // 保存课表数据
        courseSchedule.setStatus(workOrder.getStatus());
        courseSchedule.setCourseId(workOrder.getCourseId());
        courseSchedule.setCourseName(workOrder.getCourseName());
        courseSchedule.setCourseType(workOrder.getCourseType());
        courseScheduleRepository.save(courseSchedule);
    }
}