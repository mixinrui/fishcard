package com.boxfishedu.workorder.service.fishcardcenter;

import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mongo.ScheduleCourseInfo;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.RecommandCourseRequester;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.FishCardStatusService;
import com.boxfishedu.workorder.service.ScheduleCourseInfoService;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.base.BaseService;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Created by hucl on 16/7/8.
 */
@Component
public class FishCardModifyService extends BaseService<WorkOrder, WorkOrderJpaRepository, Long> {

    @Autowired
    private RecommandCourseRequester recommandCourseRequester;

    @Autowired
    private CourseScheduleService courseScheduleService;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private ScheduleCourseInfoService scheduleCourseInfoService;

    @Autowired
    private UrlConf urlConf;

    @Autowired
    private WorkOrderLogService workOrderLogService;

    public void changeCourse(WorkOrder workOrder) {
        String oldCourseName=workOrder.getCourseName();

        //取消老课程
        recommandCourseRequester.cancelOldRecommandCourse(workOrder);

        RecommandCourseView recommandCourseView = recommandCourseRequester.getRecommandCourse(workOrder);
        workOrder.setCourseName(recommandCourseView.getCourseName());
        workOrder.setCourseId(recommandCourseView.getCourseId());
        workOrder.setCourseType(recommandCourseView.getCourseType());

        CourseSchedule courseSchedule = courseScheduleService.findByWorkOrderId(workOrder.getId());
        courseSchedule.setCourseId(workOrder.getCourseId());
        courseSchedule.setCourseName(workOrder.getCourseType());
        courseSchedule.setCourseType(workOrder.getCourseType());

        //修改课程信息
        ScheduleCourseInfo scheduleCourseInfo = scheduleCourseInfoService.queryByWorkId(workOrder.getId());
        scheduleCourseInfo.setCourseType(workOrder.getCourseType());
        scheduleCourseInfo.setCourseId(workOrder.getCourseId());
        scheduleCourseInfo.setName(workOrder.getCourseName());
        scheduleCourseInfo.setDifficulty(recommandCourseView.getDifficulty());
        scheduleCourseInfo.setPublicDate(recommandCourseView.getPublicDate());
        scheduleCourseInfo.setThumbnail(recommandCourseRequester.getThumbNailPath(recommandCourseView));

        scheduleCourseInfoService.updateCourseIntoScheduleInfo(scheduleCourseInfo);
        workOrderService.saveWorkOrderAndSchedule(workOrder, courseSchedule);
        workOrderLogService.saveWorkOrderLog(workOrder, "!更换课程信息,老课程[" + oldCourseName + "]");
    }

    public List<WorkOrder> findByStudentIdAndOrderIdAndStatusLessThan(Long studentId,Long orderId,Integer status){
        return jpa.findByStudentIdAndOrderIdAndStatusLessThan(studentId,orderId,status);
    }

    public List<WorkOrder> findByStudentIdAndStatusLessThan(Long studentId,Integer status){
        return jpa.findByStudentIdAndStatusLessThan(studentId,status);
    }

    public void changeCourse(WorkOrder workOrder,Integer index) {
        String oldCourseName=workOrder.getCourseName();

        RecommandCourseView recommandCourseView = recommandCourseRequester.getRecommandCourse(workOrder,index);
        workOrder.setCourseName(recommandCourseView.getCourseName());
        workOrder.setCourseId(recommandCourseView.getCourseId());
        workOrder.setCourseType(recommandCourseView.getCourseType());

        CourseSchedule courseSchedule = courseScheduleService.findByWorkOrderId(workOrder.getId());
        courseSchedule.setCourseId(workOrder.getCourseId());
        courseSchedule.setCourseName(workOrder.getCourseType());
        courseSchedule.setCourseType(workOrder.getCourseType());

        //修改课程信息
        ScheduleCourseInfo scheduleCourseInfo = scheduleCourseInfoService.queryByWorkId(workOrder.getId());
        scheduleCourseInfo.setCourseType(workOrder.getCourseType());
        scheduleCourseInfo.setCourseId(workOrder.getCourseId());
        scheduleCourseInfo.setName(workOrder.getCourseName());
        scheduleCourseInfo.setDifficulty(recommandCourseView.getDifficulty());
        scheduleCourseInfo.setPublicDate(recommandCourseView.getPublicDate());
        scheduleCourseInfo.setThumbnail(recommandCourseRequester.getThumbNailPath(recommandCourseView));

        scheduleCourseInfoService.updateCourseIntoScheduleInfo(scheduleCourseInfo);
        workOrderService.saveWorkOrderAndSchedule(workOrder, courseSchedule);
        workOrderLogService.saveWorkOrderLog(workOrder, "!更换课程信息,老课程[" + oldCourseName + "]");
    }

    public List<WorkOrder> findByStudentIdAndStatusLessThanAndStartTimeAfter(Long studentId,Integer status,Date beginDate){
        return jpa.findByStudentIdAndStatusLessThanAndStartTimeAfter(studentId,status,beginDate);
    }

    public List<CourseSchedule> findCourseSchedulesByWorkOrders(List<Long> workOrderIds){
        return courseScheduleService.findByWorkorderIdIn((Long[])workOrderIds.toArray());
    }

    @Transactional
    public void deleteCardsAndSchedules(List<WorkOrder> workOrders,List<CourseSchedule> courseSchedules){
        this.delete(workOrders);
        courseScheduleService.delete(courseSchedules);
    }
}
