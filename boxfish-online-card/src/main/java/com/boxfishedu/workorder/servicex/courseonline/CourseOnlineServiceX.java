package com.boxfishedu.workorder.servicex.courseonline;

import com.boxfishedu.workorder.common.bean.ComboTypeEnum;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.exception.BoxfishException;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.exception.ValidationException;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.common.util.ConstantUtil;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.entity.mongo.ContinousAbsenceRecord;
import com.boxfishedu.workorder.entity.mongo.WorkOrderLog;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.CourseOnlineRequester;
import com.boxfishedu.workorder.requester.RecommandCourseRequester;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.CourseOnlineService;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.ServeService;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.absencendeal.AbsenceDealService;
import com.boxfishedu.workorder.service.accountcardinfo.DataCollectorService;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.servicex.dataanalysis.FetchHeartBeatServiceX;
import com.boxfishedu.workorder.servicex.fishcardcenter.FishCardFreezeServiceX;
import com.boxfishedu.workorder.web.view.fishcard.WorkOrderView;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.List;
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

    @Autowired
    private ThreadPoolManager threadPoolManager;

    @Autowired
    private AbsenceDealService absenceDealService;

    @Autowired
    private FishCardFreezeServiceX fishCardFreezeServiceX;

    @Autowired
    private DataCollectorService dataCollectorService;

    @Autowired
    private FetchHeartBeatServiceX fetchHeartBeatServiceX;


    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public Page<WorkOrder> getWorkOrdersByTeacher(Long teacherId, String startDateStr, String endDateStr, Pageable pageable) throws BusinessException {
        Date startDate = DateUtil.String2Date(startDateStr += " 00:00:00");
        Date endDate = DateUtil.String2Date(endDateStr += " 59:59:59");
        Page<WorkOrder> workOrders = workOrderService.findByTeacherIdAndStartTimeBetweenOrderByStartTime(teacherId, startDate, endDate, pageable);
        return workOrders;
    }

    public void updateTeachingStatus(Map<String, Object> map) {
        logger.info("@updateTeachingStatus,参数{}", JacksonUtil.toJSon(map));
        Long workOrderId = Long.parseLong(map.get("id").toString());
        Integer status = Integer.parseInt(map.get("status").toString());

        String reportTime = StringUtils.EMPTY;
        reportTime = getReportTime(map, reportTime);

        WorkOrder workOrder = workOrderService.findOne(workOrderId);
        CourseSchedule courseSchedule = courseScheduleService.findByWorkOrderId(workOrderId);

        courseOnlineService.notAllowUpdateStatus(workOrder, "不能覆盖已有消息" + "###reportTime::" + reportTime + "####新消息:" + FishCardStatusEnum.getDesc(status));

        //处理参数问题
        dealNullFishCard(workOrderId, workOrder, courseSchedule);

        //未到上课时间尝试更新,不做处理
//        Date now = new Date();
//        if (now.before(workOrder.getStartTime())) {
//            tooEarlyReport(status, workOrder);
//            return;
//        }

        //处理完成的情况
        if (status == FishCardStatusEnum.COMPLETED.getCode() || status == FishCardStatusEnum.COMPLETED_FORCE.getCode()) {
            logger.debug("@updateTeachingStatus#complete#fishcard{}完成上课,开始做完成处理",workOrder.getId());
            completeCourse(workOrder, courseSchedule, status);
        }
        //处理早退的情况(定时器不到不应该减去服务)
        else if (status == FishCardStatusEnum.TEACHER_LEAVE_EARLY.getCode() || status == FishCardStatusEnum.STUDENT_LEAVE_EARLY.getCode()) {
            if (!handleLeaveEarly(workOrder, courseSchedule, status)) {
                return;
            }
        } else {
            if (status == FishCardStatusEnum.ONCLASS.getCode()) {
                workOrder.setActualStartTime(new Date());
            }
            workOrder.setStatus(status);
            workOrder.setUpdateTime(new Date());
            courseSchedule.setStatus(status);
            courseSchedule.setUpdateTime(new Date());
            workOrderService.saveWorkOrderAndSchedule(workOrder, courseSchedule);
        }
        workOrderLogService.saveWorkOrderLog(workOrder, "reporttime::" + reportTime + "#########" + FishCardStatusEnum.getDesc(workOrder.getStatus()));
    }

    private void tooEarlyReport(Integer status, WorkOrder workOrder) {
        logger.error("@updateWorkOrderStatus,没有到上课时间;尝试更新鱼卡[{}]状态失败", workOrder.getId());
        workOrderLogService.saveWorkOrderLog(workOrder, "没有到上课时间,尝试更新鱼卡状态被拒,尝试更新状态为:[" + status + "]");
    }

    private String getReportTime(Map<String, Object> map, String reportTime) {
        try {
            reportTime = map.get("report_time").toString();
        } catch (Exception ex) {
            logger.info("updateTeachingStatus#no_report_time,终端没有上报上传时间");
        }
        return reportTime;
    }

    private void dealNullFishCard(Long workOrderId, WorkOrder workOrder, CourseSchedule courseSchedule) {
        if (null == workOrder || null == courseSchedule) {
            String msg = "无对应的鱼卡或课程schedule,请确认参数传入是否正确,鱼卡id[" + workOrderId + "]";
            logger.info(msg);
            throw new BusinessException(msg);
        }
    }


    //更新WorkOrder的状态;已弃用,改为mq的方式
    public void updateWorkOrderStatus(WorkOrderView workOrderView) throws BusinessException {
        logger.info("@updateWorkOrderStatus鱼卡id为:[{}]开始状态更新", workOrderView.getId());
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

    public void completeCourse(WorkOrder workOrder, CourseSchedule courseSchedule, Integer status) throws BoxfishException {
        logger.info("@completeCourse,开始做课程完成处理;鱼卡状态将被设置为:[{}]", FishCardStatusEnum.getDesc(status));
        //连续旷课操作
        this.handleContinusAbsence(workOrder, status);
        // 服务消费扣除
        serveService.decreaseService(workOrder, courseSchedule, status);
        //异步更新首页数据
        dataCollectorService.updateBothChnAndFnItemAsync(workOrder.getStudentId());
        //通知师生运营释放教师资源
        teacherStudentRequester.releaseTeacher(workOrder);
        //通知小马解散师生关系
        courseOnlineRequester.releaseGroup(workOrder);
        //通知订单修改状态
        serveService.notifyOrderUpdateStatus(workOrder, ConstantUtil.WORKORDER_COMPLETED);

        //处理网络数据
        fetchHeartBeatServiceX.persistAnalysisAsync(workOrder);
        //通知推荐课服务,目前由App调用
//        recommandCourseRequester.notifyCompleteCourse(workOrder);
    }

    public void handleContinusAbsence(WorkOrder workOrder, Integer status) {
        threadPoolManager.execute(new Thread(() -> {
            try {
                logger.debug("@handleContinusAbsence#user[{}],workorder[{}]连续旷课处理逻辑",workOrder.getStudentId(),workOrder.getId());
                ContinousAbsenceRecord continousAbsenceRecord = absenceDealService.queryByStudentIdAndComboType(workOrder.getStudentId(), workOrder.getService().getComboType());
                if (null != continousAbsenceRecord) {
                    if (status == FishCardStatusEnum.STUDENT_ABSENT.getCode()) {
                        continousAbsenceRecord.setContinusAbsenceNum(continousAbsenceRecord.getContinusAbsenceNum() + 1);
                        continousAbsenceRecord.setUpdateTime(new Date());
                    } else {
                        continousAbsenceRecord.setContinusAbsenceNum(0);
                    }
                    continousAbsenceRecord.setUpdateTime(new Date());
                    absenceDealService.updateCourseAbsenceNum(continousAbsenceRecord);
                } else {
                    continousAbsenceRecord = new ContinousAbsenceRecord();
                    continousAbsenceRecord.setComboType(workOrder.getService().getComboType());
                    continousAbsenceRecord.setStudentId(workOrder.getStudentId());
                    continousAbsenceRecord.setCreateTime(new Date());
                    if (status == FishCardStatusEnum.STUDENT_ABSENT.getCode()) {
                        continousAbsenceRecord.setContinusAbsenceNum(1);
                    } else {
                        continousAbsenceRecord.setContinusAbsenceNum(0);
                    }
                    absenceDealService.save(continousAbsenceRecord);
                }
                //连续旷课超过两次,
                if(continousAbsenceRecord.getContinusAbsenceNum()>1){
                    //如果超过两次,直接全部冻结
                    if(workOrder.getOrderChannel().equals(ComboTypeEnum.EXCHANGE.toString())){
                        logger.debug("@handleContinusAbsence#user[{}],workorder[{}],次数[{}]连续旷课次数超过一次处理逻辑",workOrder.getStudentId(),workOrder.getId(),continousAbsenceRecord.getContinusAbsenceNum());
                        List<WorkOrder> workOrders=workOrderService.findByStudentIdAndOrderChannelAndStartTimeAfter(workOrder.getStudentId(),ComboTypeEnum.EXCHANGE.toString(),new Date());
                        workOrders.forEach(workOrder1 -> {
                            logger.info("@handleContinusAbsence#freeze#冻结鱼卡[{}]",workOrder1.getId());
                            fishCardFreezeServiceX.freeze(workOrder1.getId());
                        });
                    }
                }
            } catch (Exception ex) {
                logger.error("@handleContinusAbsence#exception#鱼卡[{}],参数[{}]", workOrder.getId(), JacksonUtil.toJSon(workOrder));
            }

        }));
    }

    /**
     * 处理早退情况,如果之前工单状态为早退,并且和当前的角色不相同,时间在7分钟以内的上报,则认为是系统异常
     * 如果为异常,返回错误,如果不为异常则为true
     */
    private boolean handleLeaveEarly(WorkOrder workOrder, CourseSchedule courseSchedule, Integer status) {
        logger.debug("@handleLeaveEarly收到早退信息,鱼卡[{}],状态[{}],状态描述[{}]", workOrder.getId(), status, FishCardStatusEnum.getDesc(status));
        int diff = workOrder.getStatus() - status;
        long timeDiff = new Date().getTime() - workOrder.getUpdateTime().getTime();
        //如果相差1,并且上报时间差小于3分钟
        if ((Math.abs(diff) == 1) && (timeDiff / 1000 < 60)) {
            workOrderLogService.saveWorkOrderLog(workOrder, "师生上报消息不足一分钟,设置为系统异常");
            courseOnlineService.handleException(workOrder, courseSchedule, status);
            logger.info("@handleLeaveEarly双方都上报异常情况,将鱼卡[{}]标记为[系统异常]", JacksonUtil.toJSon(workOrder));
            return false;
        }
        workOrder.setStatus(status);
        courseOnlineService.saveStatus4WorkOrderAndSchedule(workOrder, courseSchedule);
        return true;
    }
}
