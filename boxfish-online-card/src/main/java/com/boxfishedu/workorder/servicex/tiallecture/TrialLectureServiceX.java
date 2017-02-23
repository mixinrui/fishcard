package com.boxfishedu.workorder.servicex.tiallecture;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.ServeService;
import com.boxfishedu.workorder.service.ServiceSDK;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.accountcardinfo.OnlineAccountService;
import com.boxfishedu.workorder.web.param.TrialLectureModifyParam;
import com.boxfishedu.workorder.web.param.TrialLectureParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Created by hucl on 16/5/23.
 */
@org.springframework.stereotype.Service
public class TrialLectureServiceX {
    @Autowired
    private WorkOrderService workOrderService;
    @Autowired
    private CourseScheduleService courseScheduleService;
    @Autowired
    private ServiceSDK serviceSDK;
    @Autowired
    private ServeService serveService;

    @Autowired
    OnlineAccountService onlineAccountService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public void buildFishCard(TrialLectureParam trialLectureParam) {
        logger.info("开始生成试讲课工单:参数[{}]", JacksonUtil.toJSon(trialLectureParam));
        CourseSchedule courseSchedule = getOldCourseSchedule(trialLectureParam);
        if (null != courseSchedule) {
            throw new BusinessException("学生在当前时间片内已存在课程,请勿重复安排课程");
        }
        //虚拟订单
        Service service = getService();
        WorkOrder workOrder = new WorkOrder();
        workOrder.setService(service);
        workOrder.setIsFreeze(0);
        workOrder.setComboType(service.getComboType());
        workOrder.setOrderId(service.getOrderId());
        saveParamIntoWorkOrder(workOrder, trialLectureParam);

        onlineAccountService.add(trialLectureParam.getStudentId());

        serveService.saveWorkorderAndCourse(workOrder);
        serviceSDK.createGroup(workOrder);
        logger.info("试讲课鱼卡生成结束,鱼卡id[{}]:", workOrder.getId());
    }

    private void saveParamIntoWorkOrder(WorkOrder workOrder, TrialLectureParam trialLectureParam) {
        workOrder.setCourseType(trialLectureParam.getCourseType());
        workOrder.setCourseId(trialLectureParam.getCourseId());
        workOrder.setCourseName(trialLectureParam.getCourseName());
        workOrder.setCreateTime(new Date());
        workOrder.setTeacherId(trialLectureParam.getTeacherId());
        workOrder.setStudentId(trialLectureParam.getStudentId());
        workOrder.setSlotId(trialLectureParam.getTimeSlotId());
        workOrder.setStartTime(DateUtil.String2Date(trialLectureParam.getStartTime()));
        workOrder.setEndTime(DateUtil.String2Date(trialLectureParam.getEndTime()));
        workOrder.setStatus(FishCardStatusEnum.TEACHER_ASSIGNED.getCode());
    }

    private Service getService() {
        Service service = serveService.findTop1ByOrderId(Long.MAX_VALUE);
        if (null != service) {
            return service;
        }
        synchronized (this) {
            if (null != service) {
                return service;
            }
            service = newService();
        }
        return service;
    }

    private Service newService() {
        Service service = new Service();
        service.setOrderId(Long.MAX_VALUE);
        serveService.save(service);
        return service;
    }

    public CourseSchedule getOldCourseSchedule(TrialLectureParam trialLectureParam) {
        Date classDate = DateUtil.String2SimpleDate(trialLectureParam.getStartTime());
        Long studentId = trialLectureParam.getStudentId();
        Integer timeSlotId = trialLectureParam.getTimeSlotId();
        CourseSchedule courseSchedule = courseScheduleService.findTop1ByStudentIdAndTimeSlotIdAndClassDate(
                studentId, timeSlotId, classDate);
        return courseSchedule;
    }

    @Transactional
    public void modifyFishCard(TrialLectureModifyParam trialLectureModifyParam) {
        TrialLectureParam before = trialLectureModifyParam.getBefore();
        TrialLectureParam after = trialLectureModifyParam.getAfter();
        CourseSchedule courseSchedule = getOldCourseSchedule(before);
        onlineAccountService.add(trialLectureModifyParam.getAfter().getStudentId());
        if (null == courseSchedule) {
            throw new BusinessException("不存在对应的鱼卡,请检查参数");
        }

        deleteFishCard(before);
        buildFishCard(after);
    }

    public void deleteFishCard(TrialLectureParam trialLectureParam) {
        CourseSchedule courseSchedule = getOldCourseSchedule(trialLectureParam);
        if (null == courseSchedule) {
            throw new BusinessException("删除失败,无对应的课程安排");
        }
        WorkOrder workOrder = workOrderService.getOne(courseSchedule.getWorkorderId());
        serveService.deleteWorkOrderAndSchedule(workOrder, courseSchedule);
    }
}
