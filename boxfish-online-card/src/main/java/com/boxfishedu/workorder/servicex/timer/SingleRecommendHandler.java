package com.boxfishedu.workorder.servicex.timer;

import com.boxfishedu.workorder.common.config.UrlConf;
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
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Created by LuoLiBing on 16/10/24.
 */
@Service
public class SingleRecommendHandler {

    @Autowired
    private DefaultRecommendHandler defaultRecommendHandler;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;

    @Autowired
    private WorkOrderLogService workOrderLogService;

    @Autowired
    private ScheduleCourseInfoService scheduleCourseInfoService;

    @Autowired
    private UrlConf urlConf;

    @Autowired
    private CourseScheduleRepository courseScheduleRepository;

    @Transactional
    public void singleRecommend(WorkOrder workOrder, CourseSchedule courseSchedule) {
        singleRecommend(workOrder, courseSchedule, (w) -> true);
    }


    /**
     * 单课程推荐
     * @param workOrder
     * @param courseSchedule
     */
    @Transactional
    public void singleRecommend(WorkOrder workOrder, CourseSchedule courseSchedule, Predicate<WorkOrder> predicate) {
        Map<Integer, RecommandCourseView> recommendCourseMap =
                defaultRecommendHandler.recommendCourse(
                        Collections.singletonList(workOrder),
                        predicate);
        RecommandCourseView courseView = recommendCourseMap.get(0);

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

        // 保存课表数据
        courseSchedule.setStatus(workOrder.getStatus());
        courseSchedule.setCourseId(workOrder.getCourseId());
        courseSchedule.setCourseName(workOrder.getCourseName());
        courseSchedule.setCourseType(workOrder.getCourseType());
        courseScheduleRepository.save(courseSchedule);

        // 保存mongo课程信息
        ScheduleCourseInfo scheduleCourseInfo = scheduleCourseInfoService.queryByWorkId(workOrder.getId());
        if(scheduleCourseInfo == null) {
            scheduleCourseInfo = new ScheduleCourseInfo(workOrder.getId(), courseSchedule.getId());
        }
        scheduleCourseInfo.initRecommendCourse(urlConf.getThumbnail_server(), courseView);
        scheduleCourseInfoService.save(scheduleCourseInfo);
    }
}
