package com.boxfishedu.workorder.servicex.timer;

import com.boxfishedu.workorder.common.bean.AppPointRecordEventEnum;
import com.boxfishedu.workorder.common.bean.FishCardDelayMessage;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.TeachingNotificationEnum;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.redis.CacheKeyConstant;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.dao.mongo.WorkOrderLogMorphiaRepository;
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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

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

    @Autowired
    private WorkOrderLogMorphiaRepository workOrderLogMorphiaRepository;


    /**
     * 教师旷课逻辑处理
     */
    public void teacherAbsentUpdator(FishCardDelayMessage fishCardDelayMessage) {
        logger.debug("@teacherAbsentUpdator,参数{}", JacksonUtil.toJSon(fishCardDelayMessage));
        WorkOrder workOrder = workOrderService.findOne(fishCardDelayMessage.getId());
        if (null == workOrder) {
            throw new BusinessException("无对应的鱼卡:" + fishCardDelayMessage.getId());
        }
        if (!isTeacherAbsent(workOrder)) {
            logger.debug("@absentUpdator鱼卡:[{}]的已经处理过,没有旷课现象;当前状态:[{}]",
                    workOrder.getId(), FishCardStatusEnum.getDesc(workOrder.getStatus()));
            return;
        } else {
            CourseSchedule courseSchedule = courseScheduleService.findByWorkOrderId(workOrder.getId());
            courseOnlineService.teacherAbsentNotAllowUpdateStatus(workOrder, "教师旷课");

            logger.warn("@[absentUpdator.teacher]鱼卡:[{}]的状态为教师旷课,开始处理", workOrder.getId());
            courseOnlineRequester.notifyTeachingOnlinePushMessage(fishCardDelayMessage.getId(), TeachingNotificationEnum.TEACHER_ABSENT);
            workOrder.setStatus(FishCardStatusEnum.TEACHER_ABSENT.getCode());
            courseSchedule.setStatus(FishCardStatusEnum.TEACHER_ABSENT.getCode());

            workOrder.setIsCourseOver((short) 1);
            workOrder.setUpdateTime(new Date());
            courseSchedule.setUpdateTime(new Date());

            courseOnlineServiceX.completeCourse(workOrder, courseSchedule, FishCardStatusEnum.TEACHER_ABSENT.getCode());

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
                && (workOrder.getStatus() != FishCardStatusEnum.CONNECTED.getCode()) && (workOrder.getStatus() != FishCardStatusEnum.STUDENT_INVITED_SCREEN.getCode())
                && (workOrder.getStatus() != FishCardStatusEnum.TEACHER_LEAVE_EARLY.getCode())) {
            logger.info("@studentAbsentUpdator#status_changed#学生当前的鱼卡[{}]状态[{}]已经不是旷课需要处理的状态,暂不做处理",
                    workOrder.getId(), FishCardStatusEnum.getDesc(workOrder.getStatus()));
            return;
        }
        CourseSchedule courseSchedule = courseScheduleService.findByWorkOrderId(workOrder.getId());
        List<WorkOrderLog> workOrderLogs = workOrderLogService.queryByWorkId(workOrder.getId());

        for (WorkOrderLog workOrderLog : workOrderLogs) {
            if (workOrderLog.getStatus() == FishCardStatusEnum.STUDENT_ENTER_ROOM.getCode()) {
                logger.info("@studentAbsentUpdator#newversion#status_changed鱼卡[{}]的学生已经点击过进入房间", workOrderLog.getWorkOrderId());
                return;
            }
            if (workOrderLog.getStatus() == FishCardStatusEnum.STUDENT_ACCEPTED.getCode()) {
                logger.info("@studentAbsentUpdator#oldversion#status_changed#学生鱼卡[{}]状态[{}]已经属于[异常]的状态,不做处理",
                        workOrder.getId(), FishCardStatusEnum.getDesc(workOrder.getStatus()));
                return;
            }
        }

        logger.info("@studentAbsentUpdator#set_student_absent#学生鱼卡[{}]状态[{}]作旷课处理",
                workOrder.getId(), FishCardStatusEnum.getDesc(workOrder.getStatus()));
        workOrder.setStatus(FishCardStatusEnum.STUDENT_ABSENT.getCode());
        courseSchedule.setStatus(workOrder.getStatus());
        workOrderService.saveWorkOrderAndSchedule(workOrder, courseSchedule);
        workOrderLogService.saveWorkOrderLog(workOrder);
    }


    /**
     * 最终的学生旷课逻辑处理
     */
    public boolean studentForceAbsentUpdator(WorkOrder workOrder, CourseSchedule courseSchedule) {
        logger.info("@studentForceAbsentUpdator#强制鱼卡[{}]旷课开始", workOrder.getId());
        //默认不为异常
        boolean isExceptionFlag = false;

        List<WorkOrderLog> workOrderLogs = workOrderLogService.queryByWorkId(workOrder.getId());

        boolean containConnectedFlag = false;
        LocalDateTime startLocalDate = LocalDateTime.ofInstant(
                workOrder.getStartTime().toInstant(), ZoneId.systemDefault()).minusMinutes(3);

        LocalDateTime endLocalDate = startLocalDate.plusMinutes(studentAbsentTimeLimit);
        Date startDate = DateUtil.localDate2Date(startLocalDate);
        Date endDate = DateUtil.localDate2Date(endLocalDate);

        //老版本的没有学生点击进入的情况,十分钟之内的时候,如果有[师生互联]的状态则将其记录
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
            workOrderLogService.saveWorkOrderLog(workOrder, "学生课前在线,但是没有师生连通,将改为系统异常");
            logger.info("studentForceAbsentUpdator判断鱼卡[{}]是否由于终端异常导致,判断结果[{}]", workOrder.getId(), isOnline);
            return true;
        }

        logger.warn("[studentForceAbsentUpdator]鱼卡:[{}]的状态为学生旷课,开始处理", workOrder.getId());

        courseOnlineServiceX.completeCourse(workOrder, courseSchedule, FishCardStatusEnum.STUDENT_ABSENT.getCode());
        workOrderLogService.saveWorkOrderLog(workOrder);
        //返回false
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
            logger.error("无对应的courseschedule,鱼卡[{" + fishCardDelayMessage.getId() + "}]");
            throw new BusinessException("无对应的courseschedule,鱼卡[{" + fishCardDelayMessage.getId() + "}]");
        }
        if (!workOrder.getStatus().equals(fishCardDelayMessage.getStatus())) {
            logger.info("鱼卡:[{}]的已经处理过,无需做强制下课处理,当前状态:[{}]",
                    workOrder.getId(), FishCardStatusEnum.getDesc(workOrder.getStatus()));
            return;
        }
        if (fishCardDelayMessage.getStatus() != FishCardStatusEnum.STUDENT_ABSENT.getCode()) {
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
            courseOnlineServiceX.completeCourse(workOrder, courseSchedule, FishCardStatusEnum.TEACHER_LEAVE_EARLY.getCode());
        }
        //处于学生接受请求或者ready状态,标记为系统异常
        else {
            if (!this.pickLeaveEarlyFromException(fishCardDelayMessage, workOrder, courseSchedule)) {
                logger.debug("@forceCompleteUpdator->将鱼卡[{}]标记为[{}]", fishCardDelayMessage.getId(),
                        FishCardStatusEnum.getDesc(FishCardStatusEnum.EXCEPTION.getCode()));
                courseOnlineServiceX.completeCourse(workOrder, courseSchedule, FishCardStatusEnum.EXCEPTION.getCode());
            }
        }
        workOrderLogService.saveWorkOrderLog(workOrder);
    }


    /**
     * 将学生早退的情况从之前被判断为系统异常的情况中挑选出来
     *
     * @param fishCardDelayMessage
     * @param workOrder
     * @return true:鱼卡为学生早退 , false:留下用来标记为系统异常
     */
    public boolean pickLeaveEarlyFromException(FishCardDelayMessage fishCardDelayMessage, WorkOrder workOrder, CourseSchedule courseSchedule) {
        logger.debug("@pickLeaveEarlyFromException 将鱼卡[{}]标记为[{}]", fishCardDelayMessage.getId(),
                FishCardStatusEnum.getDesc(FishCardStatusEnum.STUDENT_LEAVE_EARLY.getCode()));

        List<WorkOrderLog> workOrderLogs = workOrderLogMorphiaRepository.queryByWorkId(workOrder.getId(), false);
        if (CollectionUtils.isEmpty(workOrderLogs)) {
            return false;
        }

        if (!this.containStudentLeaveEarly(workOrderLogs)) {
            return false;
        }

        if (!this.isTeacherAction(workOrderLogs.get(0).getStatus())) {
            return false;
        }

        return this.pickLeaveEarlyFromException(workOrderLogs, workOrder, courseSchedule);
    }

    private boolean pickLeaveEarlyFromException(List<WorkOrderLog> workOrderLogs, WorkOrder workOrder, CourseSchedule courseSchedule) {
        for (WorkOrderLog workOrderLog : workOrderLogs) {
            if (this.isTeacherAction(workOrderLog)) {
                continue;
            }

            if (this.isStudentLeaveEarly(workOrderLog)) {
                logger.debug("@pickLeaveEarlyFromException,鱼卡[{}],标记为学生早退", workOrder.getId());
                this.dealStudentLeaveEarly(workOrder, courseSchedule);
                return true;
            }

            return false;
        }

        return false;
    }

    private void dealStudentLeaveEarly(WorkOrder workOrder, CourseSchedule courseSchedule) {
        courseOnlineServiceX.completeCourse(workOrder, courseSchedule
                , FishCardStatusEnum.STUDENT_LEAVE_EARLY.getCode());
    }

    /**
     * 是否包含学生早退的动作
     *
     * @param workOrderLogs
     * @return
     */
    private boolean containStudentLeaveEarly(List<WorkOrderLog> workOrderLogs) {
        for (WorkOrderLog workOrderLog : workOrderLogs) {
            if (workOrderLog.getStatus() == FishCardStatusEnum.STUDENT_LEAVE_EARLY.getCode()) {
                return true;
            }
        }
        return false;
    }


    private boolean isTeacherAction(WorkOrderLog workOrderLog) {
        return this.isTeacherAction(workOrderLog.getStatus());
    }

    /**
     * 判断当前最后一次动作是否为教师所为
     *
     * @param status 鱼卡流水日志的状态码
     * @return 如果有老师的操作动作, 返回true, 否则false
     */
    private boolean isTeacherAction(Integer status) {
        switch (FishCardStatusEnum.get(status)) {
            case WAITFORSTUDENT:
            case TEACHER_CANCEL_PUSH:
            case CONNECTED:
            case STUDENT_INVITED_SCREEN:
                return true;
            default:
                return false;
        }
    }

    private boolean isStudentLeaveEarly(WorkOrderLog workOrderLog) {
        return this.isStudentLeaveEarly(workOrderLog.getStatus());
    }

    private boolean isStudentLeaveEarly(int status) {
        return status == FishCardStatusEnum.STUDENT_LEAVE_EARLY.getCode();
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


    //如果确定教师旷课返回true,否则返回false
    private boolean isTeacherAbsent(WorkOrder workOrder) {
        List<WorkOrderLog> workOrderLogs = workOrderLogService.queryByWorkId(workOrder.getId());
        if (CollectionUtils.isEmpty(workOrderLogs)) {
            logger.error("@excluedeTeacherAbsent获取到的鱼卡日志为空,可确定其为旷课,鱼卡id[{}]", workOrder.getId());
            return true;
        }
        for (WorkOrderLog workOrderLog : workOrderLogs) {
            if (workOrderLog.getStatus() == FishCardStatusEnum.WAITFORSTUDENT.getCode()
                    || workOrderLog.getStatus() == FishCardStatusEnum.CONNECTED.getCode()
                    || workOrder.getStatus() == FishCardStatusEnum.ONCLASS.getCode()) {
                return false;
            }
        }
        return true;
    }
}
