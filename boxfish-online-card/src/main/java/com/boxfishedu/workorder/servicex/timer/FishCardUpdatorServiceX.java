package com.boxfishedu.workorder.servicex.timer;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.bean.FishCardDelayMessage;
import com.boxfishedu.workorder.common.bean.FishCardDelayMsgType;
import com.boxfishedu.workorder.common.bean.TeachingNotificationEnum;
import com.boxfishedu.workorder.common.redis.CacheKeyConstant;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.CourseOnlineRequester;
import com.boxfishedu.workorder.service.CourseOnlineService;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.servicex.courseonline.CourseOnlineServiceX;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

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

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 更新器:检测教师旷课消费者
     */
    @Transactional
    public void absentUpdator(FishCardDelayMessage fishCardDelayMessage) {
        logger.debug("@absentUpdator,参数{}", JacksonUtil.toJSon(fishCardDelayMessage));
        WorkOrder workOrder = workOrderService.findByIdForUpdate(fishCardDelayMessage.getId());
        if (null == workOrder) {
            throw new BusinessException("无对应的鱼卡:" + fishCardDelayMessage.getId());
        }
        if (!workOrder.getStatus().equals(fishCardDelayMessage.getStatus())) {
            logger.info("absentUpdator鱼卡:[{}]的已经处理过,没有旷课现象;当前状态:[{}]",
                    workOrder.getId(), FishCardStatusEnum.getDesc(FishCardStatusEnum.TEACHER_ABSENT.getCode()));
        } else {
            CourseSchedule courseSchedule = courseScheduleService.findByWorkOrderId(workOrder.getId());
            workOrder.setUpdateTime(new Date());
            if(fishCardDelayMessage.getType()==FishCardDelayMsgType.TEACHER_ABSENT.value()) {
                logger.warn("@[absentUpdator.teacher]鱼卡:[{}]的状态为教师旷课,开始处理", workOrder.getId());
                courseOnlineRequester.notifyTeachingOnlinePushMessage(fishCardDelayMessage.getId(), TeachingNotificationEnum.TEACHER_ABSENT);
                workOrder.setStatus(FishCardStatusEnum.TEACHER_ABSENT.getCode());
                courseSchedule.setStatus(FishCardStatusEnum.TEACHER_ABSENT.getCode());
            }
            else{
                logger.warn("[absentUpdator.student]鱼卡:[{}]的状态为学生旷课,开始处理", workOrder.getId());
                workOrder.setStatus(FishCardStatusEnum.STUDENT_ABSENT.getCode());
                courseSchedule.setStatus(FishCardStatusEnum.STUDENT_ABSENT.getCode());
            }
            courseSchedule.setUpdateTime(new Date());
            courseScheduleService.save(courseSchedule);
            workOrderService.save(workOrder);
        }
    }

    /**
     *强制完成,服务数量减少1
     */
    public void forceCompleteUpdator(FishCardDelayMessage fishCardDelayMessage) {
        logger.debug("@forceCompleteUpdator,参数{}", JacksonUtil.toJSon(fishCardDelayMessage));
        WorkOrder workOrder = workOrderService.findOne(fishCardDelayMessage.getId());
        if (null == workOrder) {
            throw new BusinessException("无对应的鱼卡:" + fishCardDelayMessage.getId());
        }
        CourseSchedule courseSchedule=courseScheduleService.findByWorkOrderId(workOrder.getId());
        if(null==courseSchedule){
            throw new BusinessException("无对应的courseschedule,鱼卡[{"+fishCardDelayMessage.getId()+"}]");
        }
        if (!workOrder.getStatus().equals(fishCardDelayMessage.getStatus())) {
            logger.info("鱼卡:[{}]的已经处理过,无需做强制下课处理,当前状态:[{}]",
                    workOrder.getId(), FishCardStatusEnum.getDesc(workOrder.getStatus()));
        }
        //处于正在上课,标记为服务器强制完成
        if(fishCardDelayMessage.getStatus()==FishCardStatusEnum.ONCLASS.getCode()) {
            logger.info("@forceCompleteUpdator->将鱼卡[{}]标记为[{}]",fishCardDelayMessage.getId(),
                    FishCardStatusEnum.getDesc(FishCardStatusEnum.COMPLETED_FORCE_SERVER.getCode()));
            courseOnlineServiceX.completeCourse(workOrder, courseSchedule, FishCardStatusEnum.COMPLETED_FORCE_SERVER.getCode());
        }
        //处于学生接受请求或者ready状态,标记为系统异常
        if(fishCardDelayMessage.getStatus()==FishCardStatusEnum.READY.getCode()
                ||fishCardDelayMessage.getStatus()==FishCardStatusEnum.STUDENT_ACCEPTED.getCode()){
            logger.info("@forceCompleteUpdator->将鱼卡[{}]标记为[{}]",fishCardDelayMessage.getId(),
                    FishCardStatusEnum.getDesc(FishCardStatusEnum.EXCEPTION.getCode()));
            courseOnlineService.handleException(workOrder,courseSchedule,FishCardStatusEnum.EXCEPTION.getCode());
        }
    }

    /**
     *更新器：通知教师做好上课准备
     */
    public void teacherPrepareClassUpdator(FishCardDelayMessage fishCardDelayMessage){
        logger.debug("@teacherPrepareClassUpdator,参数{}", JacksonUtil.toJSon(fishCardDelayMessage));
        //将已经推送过的数据放入cache,防止二次推送
        String flag = cacheManager.getCache(CacheKeyConstant.NOTIFY_TEACHER_PREPARE_CLASS_KEY).get(fishCardDelayMessage.getId(), String.class);
        if(StringUtils.isEmpty(flag)) {
            logger.info("@teacherPrepareClassUpdator->在redis中无对应的通知教师上课记录,开始通知师生运营组提醒教师上课");
            cacheManager.getCache(CacheKeyConstant.NOTIFY_TEACHER_PREPARE_CLASS_KEY).put(fishCardDelayMessage.getId(), "sent");
            courseOnlineRequester.notifyTeachingOnlinePushMessage(fishCardDelayMessage.getId(), TeachingNotificationEnum.PREPARE_START_CLASS);
            return;
        }
        logger.info("@teacherPrepareClassUpdator->在redis中存在通知教师上课记录,不需要再次通知");
    }
}
