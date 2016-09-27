package com.boxfishedu.workorder.service.fishcardcenter;

import com.boxfishedu.workorder.common.bean.TeachingType;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.util.JacksonUtil;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Created by hucl on 16/7/8.
 */
@Component
@SuppressWarnings("ALL")
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

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    public void changeCourse(WorkOrder workOrder) {
        logger.debug("FishCardModifyService#changeCourse:开始换课,旧的鱼卡信息[{}]", JacksonUtil.toJSon(workOrder));
        String oldCourseName = workOrder.getCourseName();

        RecommandCourseView recommandCourseView = recommandCourseRequester.changeCourse(workOrder);
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

        //外教不参与师生互评 jiaozijun
        if(TeachingType.WAIJIAO.getCode() != workOrder.getSkuId() ) {
            /** 换课更新  换课时间  jiaozijun **/
            workOrder.setUpdatetimeChangecourse(new Date());
            /** 换课 1 换课消息未发送 jiaozijun **/
            workOrder.setSendflagcc("1");
        }

        scheduleCourseInfoService.updateCourseIntoScheduleInfo(scheduleCourseInfo);
        workOrderService.saveWorkOrderAndSchedule(workOrder, courseSchedule);
        workOrderLogService.saveWorkOrderLog(workOrder, "!更换课程信息,老课程[" + oldCourseName + "]");
    }

    public List<WorkOrder> findByStudentIdAndOrderIdAndStatusLessThan(Long studentId, Long orderId, Integer status) {
        return jpa.findByStudentIdAndOrderIdAndStatusLessThan(studentId, orderId, status);
    }

    public List<WorkOrder> findByStudentIdAndStatusLessThan(Long studentId, Integer status) {
        return jpa.findByStudentIdAndStatusLessThan(studentId, status);
    }

    public List<WorkOrder> findByStudentIdAndStatusLessThanAndStartTimeAfter(Long studentId, Integer status, Date beginDate) {
        return jpa.findByStudentIdAndStatusLessThanAndStartTimeAfter(studentId, status, beginDate);
    }

    public List<CourseSchedule> findCourseSchedulesByWorkOrders(List<Long> workOrderIds) {
        return courseScheduleService.findByWorkorderIdIn((Long[]) workOrderIds.toArray());
    }

    @Transactional
    public void deleteCardsAndSchedules(List<WorkOrder> workOrders, List<CourseSchedule> courseSchedules) {
        this.delete(workOrders);
        courseScheduleService.delete(courseSchedules);
    }
}
