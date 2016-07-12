package com.boxfishedu.workorder.servicex.courseonline;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.exception.BoxfishException;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.exception.ValidationException;
import com.boxfishedu.workorder.common.util.ConstantUtil;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.entity.mongo.WorkOrderLog;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.CourseOnlineRequester;
import com.boxfishedu.workorder.requester.RecommandCourseRequester;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.*;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.web.view.fishcard.WorkOrderView;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.Map;

/**
 * Created by hucl on 16/3/22.
 */
@org.springframework.stereotype.Service
public class CourseOnlineServiceX {
    @Autowired
    private ServeService serveService;
    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private WorkOrderLogService workOrderLogService;

    @Autowired
    private CourseOnlineService courseOnlineService;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private CourseScheduleService courseScheduleService;

    @Autowired
    private CourseOnlineRequester courseOnlineRequester;

    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    @Autowired
    private RecommandCourseRequester recommandCourseRequester;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public Page<WorkOrder> getWorkOrdersByTeacher(Long teacherId, String startDateStr, String endDateStr, Pageable pageable) throws BusinessException {
        Date startDate = DateUtil.String2Date(startDateStr += " 00:00:00");
        Date endDate = DateUtil.String2Date(endDateStr += " 59:59:59");
        Page<WorkOrder> workOrders = workOrderService.findByTeacherIdAndStartTimeBetweenOrderByStartTime(teacherId, startDate, endDate, pageable);
        return workOrders;
    }

    //接收来自在线授课的,TODO:没有异常描述信息
    public void updateTeachingStatus(Map<String, Object> map) {
        logger.info("@updateTeachingStatus,参数{}",JacksonUtil.toJSon(map));
        Long workOrderId = Long.parseLong(map.get("id").toString());
        Integer status = Integer.parseInt(map.get("status").toString());
        WorkOrder workOrder = workOrderService.findOne(workOrderId);
        CourseSchedule courseSchedule= courseScheduleService.findByWorkOrderId(workOrderId);
        if((status!=FishCardStatusEnum.COMPLETED.getCode())&&(status!=FishCardStatusEnum.COMPLETED_FORCE.getCode())) {
            courseOnlineService.notAllowUpdateStatus(workOrder,status);
        }
        if (null == workOrder||null==courseSchedule) {
            String msg="无对应的鱼卡或课程schedule,请确认参数传入是否正确,鱼卡id[" + workOrderId + "]";
            logger.info(msg);
            throw new BusinessException(msg);
        }
        //处理完成的情况
        if(status==FishCardStatusEnum.COMPLETED.getCode()||status==FishCardStatusEnum.COMPLETED_FORCE.getCode()){
            completeCourse(workOrder,courseSchedule, status);
        }
        //学生旷课
        else if(status==FishCardStatusEnum.TEACHER_CANCEL_PUSH.getCode()){
            workOrderLogService.saveWorkOrderLog(workOrder,FishCardStatusEnum.getDesc(status));
            completeCourse(workOrder,courseSchedule, FishCardStatusEnum.STUDENT_ABSENT.getCode());
        }
        //处理早退的情况(定时器不到不应该减去服务)
        else if(status==FishCardStatusEnum.TEACHER_LEAVE_EARLY.getCode()||status==FishCardStatusEnum.STUDENT_LEAVE_EARLY.getCode()){
            handleLeaveEarly(workOrder,courseSchedule,status);
        }
        //TODO:异常情况分哪些;哪些异常上报给我;在哪些情况下需要扣除上课的情况.没有确定
        else {
            if(status==FishCardStatusEnum.ONCLASS.getCode()){
                workOrder.setActualStartTime(new Date());
            }
            workOrder.setStatus(status);
            workOrder.setUpdateTime(new Date());
            workOrderService.save(workOrder);
        }
        addWorkOrderLog(workOrder);
    }

    //更新WorkOrder的状态
    public void updateWorkOrderStatus(WorkOrderView workOrderView) throws BusinessException {
        logger.info("鱼卡id为:[{}]开始状态更新", workOrderView.getId());
        boolean pivot = false;
        for (FishCardStatusEnum fishCardStatusEnum : FishCardStatusEnum.values()) {
            if (workOrderView.getStatus() == fishCardStatusEnum.getCode()) {
                pivot = true;
                break;
            }
        }
        if (!pivot) {
            throw new ValidationException("传入的状态码不合法");
        }
        WorkOrder workOrder = workOrderService.findOne(workOrderView.getId());
        if (null == workOrder) {
            throw new BusinessException("找不到对应的鱼卡");
        }
        workOrder.setStatus(workOrderView.getStatus());
        workOrder.setUpdateTime(new Date());
        workOrderService.save(workOrder);

        //将记录加入鱼卡日志
        WorkOrderLog workOrderLog = new WorkOrderLog();
        workOrderLog.setWorkOrderId(workOrder.getId());
        workOrderLog.setStatus(workOrderView.getStatus());
        if (!StringUtils.isEmpty(workOrderView.getContent())) {
            workOrderLog.setContent(workOrderView.getContent());
        } else {
            workOrderLog.setContent(FishCardStatusEnum.getDesc(workOrderView.getStatus()));
        }
        workOrderLog.setCreateTime(new Date());
        workOrderLogService.save(workOrderLog);
        logger.info("鱼卡id为:[{}]开始状态更新成功,新生成的鱼卡日志id为:[{}]", workOrderView.getId(), workOrderLog.getId());
    }

    public void completeCourse(WorkOrder workOrder,CourseSchedule courseSchedule,Integer status) throws BoxfishException {
        logger.info("@completeCourse,开始做课程完成处理;鱼卡状态将被设置为:[{}]",FishCardStatusEnum.getDesc(status));
        // 服务消费扣除
        serveService.decreaseService(workOrder,courseSchedule,status);
        //通知师生运营释放教师资源
        teacherStudentRequester.releaseTeacher(workOrder);
        //通知小马解散师生关系
        courseOnlineRequester.releaseGroup(workOrder);
        //通知订单修改状态
        serveService.notifyOrderUpdateStatus(workOrder, ConstantUtil.WORKORDER_COMPLETED);
        //通知推荐课服务
        recommandCourseRequester.notifyCompleteCourse(workOrder);
    }


    public WorkOrderLog addWorkOrderLog(WorkOrder workWorder) throws BoxfishException {
        logger.info("@addWorkOrderLog,添加鱼卡日志,鱼卡信息{}",JacksonUtil.toJSon(workWorder));
        WorkOrderLog workOrderLog = new WorkOrderLog();
        workOrderLog.setWorkOrderId(workWorder.getId());
        workOrderLog.setStatus(workWorder.getStatus());
        workOrderLog.setCreateTime(new Date());
        workOrderLog.setContent(FishCardStatusEnum.getDesc(workWorder.getStatus()));
        workOrderLogService.save(workOrderLog);
        return workOrderLog;
    }

    /**
     *处理早退情况,如果之前工单状态为早退,并且和当前的角色不相同,时间在7分钟以内的上报,则认为是系统异常
     */
    private void handleLeaveEarly(WorkOrder workOrder,CourseSchedule courseSchedule,Integer status){
        logger.debug("@handleLeaveEarly收到早退信息,鱼卡[{}],状态[{}],状态描述[{}]",workOrder.getId(),status,FishCardStatusEnum.getDesc(status));
        int diff=workOrder.getStatus()-status;
        long timeDiff=new Date().getTime()-workOrder.getUpdateTime().getTime();
        //如果相差1,并且上报时间差小于3分钟
        if((Math.abs(diff)==1)&&(timeDiff/1000<180)){
            courseOnlineService.handleException(workOrder,courseSchedule,status);
            logger.info("@handleLeaveEarly双方都上报异常情况,将鱼卡[{}]标记为[系统异常]", JacksonUtil.toJSon(workOrder));
            return;
        }
        workOrder.setStatus(status);
        courseOnlineService.saveStatus4WorkOrderAndSchedule(workOrder,courseSchedule);
    }
}
