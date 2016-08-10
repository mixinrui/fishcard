package com.boxfishedu.workorder.servicex.timer;

import com.boxfishedu.workorder.common.bean.*;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.redis.CacheKeyConstant;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.entity.mongo.WorkOrderLog;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.CourseOnlineRequester;
import com.boxfishedu.workorder.service.CourseOnlineService;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.servicex.courseonline.CourseOnlineServiceX;
import com.boxfishedu.workorder.servicex.dataanalysis.FetchHeartBeatServiceX;
import com.boxfishedu.workorder.web.param.requester.DataAnalysisLogParam;
import org.apache.catalina.LifecycleState;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Created by hucl on 16/6/8.
 * 主要用于教师旷课;学生旷课;
 * #超过上课时间的时长(minute):
 * pass_card_start_peroid: 3
 * #超过等待学生应答的时长(minute):
 * pass_card_wait_peroid: 3
 * #超过应该结束时间时长(minute):
 * pass_card_end_time: 3
 */
@Component
public class FishCardUpdatorServiceX {
    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private CourseScheduleService courseScheduleService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private CourseOnlineRequester courseOnlineRequester;

    @Autowired
    private CourseOnlineServiceX courseOnlineServiceX;

    @Autowired
    private CourseOnlineService courseOnlineService;

    @Autowired
    private WorkOrderLogService workOrderLogService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${parameter.student_absent_time_limit}")
    private Integer studentAbsentTimeLimit;

    @Autowired
    private FetchHeartBeatServiceX fetchHeartBeatServiceX;


    /**
     * 教师旷课逻辑处理
     */
    public void teacherAbsentUpdator(FishCardDelayMessage fishCardDelayMessage) {
        logger.debug("@teacherAbsentUpdator,参数{}", JacksonUtil.toJSon(fishCardDelayMessage));
        WorkOrder workOrder = workOrderService.findOne(fishCardDelayMessage.getId());
        if (null == workOrder) {
            throw new BusinessException("无对应的鱼卡:" + fishCardDelayMessage.getId());
        }
        if (!workOrder.getStatus().equals(fishCardDelayMessage.getStatus())) {
            logger.info("absentUpdator鱼卡:[{}]的已经处理过,没有旷课现象;当前状态:[{}]",
                    workOrder.getId(), FishCardStatusEnum.getDesc(FishCardStatusEnum.TEACHER_ABSENT.getCode()));
            return;
        } else {
            CourseSchedule courseSchedule = courseScheduleService.findByWorkOrderId(workOrder.getId());
            courseOnlineService.notAllowUpdateStatus(workOrder);
            workOrder.setUpdateTime(new Date());

            logger.warn("@[absentUpdator.teacher]鱼卡:[{}]的状态为教师旷课,开始处理", workOrder.getId());
            courseOnlineRequester.notifyTeachingOnlinePushMessage(fishCardDelayMessage.getId(), TeachingNotificationEnum.TEACHER_ABSENT);
            workOrder.setStatus(FishCardStatusEnum.TEACHER_ABSENT.getCode());
            courseSchedule.setStatus(FishCardStatusEnum.TEACHER_ABSENT.getCode());

            workOrder.setIsCourseOver((short)1);
            workOrder.setUpdateTime(new Date());
            courseSchedule.setUpdateTime(new Date());

            workOrderService.saveWorkOrderAndSchedule(workOrder, courseSchedule);

            courseOnlineRequester.releaseGroup(workOrder);
            workOrderLogService.saveWorkOrderLog(workOrder);
        }
    }

    /**
     * 学生中途旷课逻辑处理
     */
    public void studentAbsentUpdator(FishCardDelayMessage fishCardDelayMessage) {
        logger.debug("@studentAbsentUpdator#FishCardDelayMessage,参数{}", JacksonUtil.toJSon(fishCardDelayMessage));
        WorkOrder workOrder = workOrderService.findOne(fishCardDelayMessage.getId());
        if (null == workOrder) {
            logger.debug("@studentAbsentUpdator#无对应的鱼卡");
            throw new BusinessException("无对应的鱼卡:" + fishCardDelayMessage.getId());
        }
        if ((workOrder.getStatus() != FishCardStatusEnum.WAITFORSTUDENT.getCode()) && (workOrder.getStatus() != FishCardStatusEnum.TEACHER_CANCEL_PUSH.getCode())
                && (workOrder.getStatus() != FishCardStatusEnum.CONNECTED.getCode())) {
            logger.info("@studentAbsentUpdator#status_changed#学生当前的鱼卡[{}]状态[{}]已经不是旷课需要处理的状态,暂不做处理",
                    workOrder.getId(), FishCardStatusEnum.getDesc(workOrder.getStatus()));
            return;
        }
        CourseSchedule courseSchedule = courseScheduleService.findByWorkOrderId(workOrder.getId());
        List<WorkOrderLog> workOrderLogs = workOrderLogService.queryByWorkId(workOrder.getId());

        boolean isExceptionFlag = false;
        for (WorkOrderLog workOrderLog : workOrderLogs) {
            if (workOrderLog.getStatus() == FishCardStatusEnum.STUDENT_ACCEPTED.getCode()) {
                isExceptionFlag = true;
                logger.info("@studentAbsentUpdator#status_changed#学生鱼卡[{}]状态[{}]已经属于[异常]的状态,不做处理",
                        workOrder.getId(), FishCardStatusEnum.getDesc(workOrder.getStatus()));
                break;
            }
        }
        if (!isExceptionFlag) {
            logger.info("@studentAbsentUpdator#set_student_absent#学生鱼卡[{}]状态[{}]作旷课处理",
                    workOrder.getId(), FishCardStatusEnum.getDesc(workOrder.getStatus()));
            workOrder.setStatus(FishCardStatusEnum.STUDENT_ABSENT.getCode());
            courseSchedule.setStatus(workOrder.getStatus());
            workOrderService.saveWorkOrderAndSchedule(workOrder, courseSchedule);
            workOrderLogService.saveWorkOrderLog(workOrder);
        }
    }


    /**
     * 最终的学生旷课逻辑处理
     */
    public boolean studentForceAbsentUpdator(WorkOrder workOrder, CourseSchedule courseSchedule) {
        logger.info("@studentForceAbsentUpdator#强制鱼卡[{}]旷课开始",workOrder.getId());
        //默认不为异常
        boolean isExceptionFlag = false;

        List<WorkOrderLog> workOrderLogs = workOrderLogService.queryByWorkId(workOrder.getId());
        if (CollectionUtils.isEmpty(workOrderLogs)) {
            return false;
        }

        boolean containConnectedFlag = false;
        LocalDateTime startLocalDate = LocalDateTime.ofInstant(workOrder.getStartTime().toInstant(), ZoneId.systemDefault()).minusMinutes(2);
        LocalDateTime endLocalDate = startLocalDate.plusMinutes(studentAbsentTimeLimit);
        Date startDate = DateUtil.localDate2Date(startLocalDate);
        Date endDate = DateUtil.localDate2Date(endLocalDate);

        for (WorkOrderLog workOrderLog : workOrderLogs) {
            if (workOrderLog.getStatus() == FishCardStatusEnum.CONNECTED.getCode()
                    && workOrderLog.getCreateTime().after(startDate)
                    && workOrderLog.getCreateTime().before(endDate)) {
                containConnectedFlag = true;
            }
        }

        boolean isOnline = fetchHeartBeatServiceX.isOnline(new DataAnalysisLogParam(workOrder.getStudentId(),
                startDate.getTime(), endDate.getTime(), AppPointRecordEventEnum.STUDENT_ONLINE_STATUS.value()));
        //没有联通但是在线,则表示为系统异常
        if (!containConnectedFlag && isOnline) {
            isExceptionFlag = true;
            logger.info("studentForceAbsentUpdator判断鱼卡[{}]是否由于终端异常导致,判断结果[{}]", workOrder.getId(), isOnline);
        }
        if (!isExceptionFlag) {
            logger.warn("[studentForceAbsentUpdator]鱼卡:[{}]的状态为学生旷课,开始处理", workOrder.getId());
            courseOnlineServiceX.completeCourse(workOrder, courseSchedule, FishCardStatusEnum.STUDENT_ABSENT.getCode());
            workOrderLogService.saveWorkOrderLog(workOrder);
        }
        return isExceptionFlag;
    }

    /**
     * 强制完成,服务数量减少1
     */
    public void forceCompleteUpdator(FishCardDelayMessage fishCardDelayMessage) {
        logger.debug("@forceCompleteUpdator,参数{}", JacksonUtil.toJSon(fishCardDelayMessage));
        WorkOrder workOrder = workOrderService.findOne(fishCardDelayMessage.getId());
        if (null == workOrder) {
            throw new BusinessException("无对应的鱼卡:" + fishCardDelayMessage.getId());
        }
        CourseSchedule courseSchedule = courseScheduleService.findByWorkOrderId(workOrder.getId());
        if (null == courseSchedule) {
            throw new BusinessException("无对应的courseschedule,鱼卡[{" + fishCardDelayMessage.getId() + "}]");
        }
        if (!workOrder.getStatus().equals(fishCardDelayMessage.getStatus())) {
            logger.info("鱼卡:[{}]的已经处理过,无需做强制下课处理,当前状态:[{}]",
                    workOrder.getId(), FishCardStatusEnum.getDesc(workOrder.getStatus()));
            return;
        }
        if(fishCardDelayMessage.getStatus() != FishCardStatusEnum.STUDENT_ABSENT.getCode()) {
            courseOnlineService.notAllowUpdateStatus(workOrder);
        }
        workOrder.setIsCourseOver((short) 1);

        //  对于不包含学生接受请求的消息,视为旷课处理;其他视为系统异常
        if (fishCardDelayMessage.getStatus() == FishCardStatusEnum.STUDENT_ABSENT.getCode()) {
            if (!studentForceAbsentUpdator(workOrder, courseSchedule)) {
                return;
            }
        }
        //处于正在上课,标记为服务器强制完成
        if (fishCardDelayMessage.getStatus() == FishCardStatusEnum.ONCLASS.getCode()) {
            logger.info("@forceCompleteUpdator->将鱼卡[{}]标记为[{}]", fishCardDelayMessage.getId(),
                    FishCardStatusEnum.getDesc(FishCardStatusEnum.COMPLETED_FORCE_SERVER.getCode()));
            courseOnlineServiceX.completeCourse(workOrder, courseSchedule, FishCardStatusEnum.COMPLETED_FORCE_SERVER.getCode());
        } else if (fishCardDelayMessage.getStatus() == FishCardStatusEnum.STUDENT_LEAVE_EARLY.getCode()) {
            logger.info("@forceCompleteUpdator->将鱼卡[{}]状态[{}]的群解散", workOrder.getId(), workOrder.getStatus());
            courseOnlineServiceX.completeCourse(workOrder, courseSchedule, FishCardStatusEnum.STUDENT_LEAVE_EARLY.getCode());
        } else if (fishCardDelayMessage.getStatus() == FishCardStatusEnum.TEACHER_LEAVE_EARLY.getCode()) {
            logger.info("@forceCompleteUpdator->将鱼卡[{}]状态[{}]的群解散", workOrder.getId(), workOrder.getStatus());
            workOrderService.save(workOrder);
            courseOnlineRequester.releaseGroup(workOrder);
        }
        //处于学生接受请求或者ready状态,标记为系统异常
        else {
            logger.info("@forceCompleteUpdator->将鱼卡[{}]标记为[{}]", fishCardDelayMessage.getId(),
                    FishCardStatusEnum.getDesc(FishCardStatusEnum.EXCEPTION.getCode()));
            courseOnlineService.handleException(workOrder, courseSchedule, FishCardStatusEnum.EXCEPTION.getCode());
        }
        workOrderLogService.saveWorkOrderLog(workOrder);
    }

    /**
     * 更新器：通知教师做好上课准备
     */
    public void teacherPrepareClassUpdator(FishCardDelayMessage fishCardDelayMessage) {
        logger.debug("@teacherPrepareClassUpdator,参数{}", JacksonUtil.toJSon(fishCardDelayMessage));
        //将已经推送过的数据放入cache,防止二次推送
        String flag = cacheManager.getCache(CacheKeyConstant.NOTIFY_TEACHER_PREPARE_CLASS_KEY).get(fishCardDelayMessage.getId(), String.class);
        if (StringUtils.isEmpty(flag)) {
            logger.info("@teacherPrepareClassUpdator->在redis中无对应的通知教师上课记录,开始通知师生运营组提醒教师上课");
            cacheManager.getCache(CacheKeyConstant.NOTIFY_TEACHER_PREPARE_CLASS_KEY).put(fishCardDelayMessage.getId(), "sent");
            courseOnlineRequester.notifyTeachingOnlinePushMessage(fishCardDelayMessage.getId(), TeachingNotificationEnum.PREPARE_START_CLASS);
            return;
        }
        logger.info("@teacherPrepareClassUpdator->在redis中存在通知教师上课记录,不需要再次通知");
    }

    private void handleStudentAbsent(FishCardDelayMessage fishCardDelayMessage, WorkOrder workOrder, CourseSchedule courseSchedule) {
        logger.info("@handleStudentAbsent->鱼卡[{}]处理TEACHER_CANCEL_PUSH", fishCardDelayMessage.getId());

    }
}
