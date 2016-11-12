package com.boxfishedu.workorder.servicex.timer;

import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.dao.jpa.CourseScheduleRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mongo.ScheduleCourseInfo;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.ScheduleCourseInfoService;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

/**
 * Created by hucl on 16/11/1.
 */
@Component
public class PersistCoursesHandler {
    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;

    @Autowired
    private WorkOrderLogService workOrderLogService;

    @Autowired
    private CourseScheduleRepository courseScheduleRepository;

    @Autowired
    private ScheduleCourseInfoService scheduleCourseInfoService;

    @Autowired
    private UrlConf urlConf;

    @Transactional
    public void persistCourseInfos(WorkOrder workOrder, CourseSchedule courseSchedule, RecommandCourseView courseView) {
        // 保存鱼卡
        workOrder.initCourseInfo(courseView);
        workOrderJpaRepository.save(workOrder);

        // 记鱼卡日志
        workOrderLogService.saveWorkOrderLog(workOrder,
                String.format("课程推荐,类型:[%s],课程Id:[%s],课程名:[%s]",
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
