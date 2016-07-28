package com.boxfishedu.workorder.common.rabbitmq;

import com.boxfishedu.card.bean.ServiceTimerMessage;
import com.boxfishedu.card.bean.TimerMessageType;
import com.boxfishedu.online.order.entity.OrderForm;
import com.boxfishedu.workorder.common.bean.QueueTypeEnum;
import com.boxfishedu.workorder.common.exception.ValidationException;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.entity.mysql.FromTeacherStudentForm;
import com.boxfishedu.workorder.service.ServeService;
import com.boxfishedu.workorder.service.commentcard.ForeignTeacherCommentCardService;
import com.boxfishedu.workorder.servicex.courseonline.CourseOnlineServiceX;
import com.boxfishedu.workorder.servicex.graborder.MakeWorkOrderServiceX;
import com.boxfishedu.workorder.servicex.orderrelated.OrderRelatedServiceX;
import com.boxfishedu.workorder.servicex.timer.*;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.web.view.teacher.TeacherView;
import org.apache.http.io.SessionOutputBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * Created by hucl on 16/4/15.
 */
@Component
public class RabbitMqReciver {
    @Autowired
    private OrderRelatedServiceX orderRelatedServiceX;
    @Autowired
    private CourseScheduleUpdatorServiceX courseScheduleUpdatorServiceX;
    @Autowired
    private CourseOnlineServiceX courseOnlineServiceX;
    @Autowired
    private ServeService serveService;
    @Autowired                                      
    private RabbitMqSender rabbitMqSender;
    @Autowired
    private ScheduleTeachersStasticsServiceX scheduleTeachersStasticsServiceX;
    @Autowired
    private FishCardUpdatorServiceX fishCardUpdatorServiceX;

    @Autowired
    private DailyCourseAssignedServiceX dailyCourseAssignedServiceX;

    @Autowired
    private FishCardStatusFinderServiceX fishCardStatusFinderServiceX;

    @Autowired
    private ForeignTeacherCommentCardService foreignTeacherCommentCardService;
    // 抢单服务层
    @Autowired
    private MakeWorkOrderServiceX makeWorkOrderServiceX;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 订单中心转换请求
     */
    @RabbitListener(queues = RabbitMqConstant.ORDER_TO_SERVICE_QUEUE)
    public void orderConsumer(OrderForm orderView) throws Exception {
        logger.info("@orderConsumer");
        try {
            serveService.order2ServiceAndWorkOrder(orderView);
        } catch (Exception ex) {
            logger.error("订单[{}]转换失败", orderView.getId());
            throw new Exception("转换失败放回队列");
        }
//        logger.info("收到来自订单中心的转换请求,订单id:[{}]",orderView.getId());
//        orderRelatedServiceX.preHandleOrder(orderView);
////        orderRelatedServiceX.order2ServiceAndWorkOrder(orderView);
//        System.out.println("##############");
//        return null;
    }


    /**
     * 定时器消息监听
     */
    @RabbitListener(queues = RabbitMqConstant.UNASSIGNED_TEACHER_TIMER_QUEUE)
    public void timerMessageListener(ServiceTimerMessage serviceTimerMessage) throws Exception {
        try {
            logger.info("@TIMER->->->->->->->接收来自定时器的消息,参数:{},", JacksonUtil.toJSon(serviceTimerMessage));
            if (serviceTimerMessage.getType() == TimerMessageType.TEACHER_ASSIGN_NOTIFY.value()) {
                logger.info("批量更新教师入库开始");
                courseScheduleUpdatorServiceX.bathUpdateTeacherIntoSchedule();
                ServiceTimerMessage result = new ServiceTimerMessage();
                result.setType(TimerMessageType.TEACHER_ASSIGN_NOTIFY_REPLY.value());
                result.setStatus(0);
                result.setBody(null);
                Date date = new Date();
                ServiceTimerMessage replay = new ServiceTimerMessage();
                replay.setTime(DateUtil.Date2String(new Date()));
                replay.setType(TimerMessageType.TEACHER_ASSIGN_NOTIFY_REPLY.value());
                rabbitMqSender.send(replay, QueueTypeEnum.ASSIGN_TEACHER_TIMER_REPLY);
            }
            //定时查询教师不够的情况
            else if (serviceTimerMessage.getType() == TimerMessageType.TEACHER_OUT_NUM_NOTIFY.value()) {
                logger.info("@TIMER>>>>QUERY_UNASSIGNED>>>检查教师的分配情况开始");
                scheduleTeachersStasticsServiceX.teacherNumAlter(serviceTimerMessage);
            } else if (serviceTimerMessage.getType() == TimerMessageType.TEACHER_ABSENT_QUERY_NOTIFY.value()) {
                logger.info("@TIMER>>>>>TEACHER_ABSENT>>>>>检查教师旷课情况开始");
                fishCardStatusFinderServiceX.teacherAbsentFinder();
            } else if (serviceTimerMessage.getType() == TimerMessageType.STUDENT_ABSENT_QUERY_NOTIFY.value()) {
                logger.info("@TIMER>>>>>STUDENT_ABSENT>>>>>>检查学生旷课情况开始");
                fishCardStatusFinderServiceX.studentAbsentFinder();
            } else if (serviceTimerMessage.getType() == TimerMessageType.COMPLETE_FORCE_SERVER_NOTIFY.value()) {
                logger.info("@TIMER>>>>>SERVER_FORCE_COMPLETED>>>>>检查服务端强制下课情况开始");
                fishCardStatusFinderServiceX.forceCompleteFinder();
            } else if (serviceTimerMessage.getType() == TimerMessageType.TEACHER_PREPARE_CLASS_NOTIFY.value()) {
                logger.info("@TIMER>>>>>TEACHER_PREPARE>>>>检查提醒教师准备上课的情况");
                fishCardStatusFinderServiceX.teacherPrepareClassFinder();
            } else if (serviceTimerMessage.getType() == TimerMessageType.STUDENT_NOT_MAKEUP_NOTIFY.value()) {
                logger.info("@TIMER>>>>>STUDENT_NOT_MAKEUP_NOTIFY>>>>检查学生超期未补课的情况");
            } else if (serviceTimerMessage.getType() == TimerMessageType.TEACHER_COURSE_NEW_ASSIGNEDED_DAY.value()) {
                logger.info("@TIMER>>>>>TEACHER_COURSE_NEW_ASSIGNEDED_DAY>>>>检查教师当天新分课程,并发送相关通知消息");
                dailyCourseAssignedServiceX.batchNotifyTeacherAssignedCourse();
            } else if (serviceTimerMessage.getType() == TimerMessageType.GRAB_ORDER_DATA_INIT.value()) {
                logger.info("=========>getGRAB_ORDER_DATA_INIT90message");
                logger.info("=========>初始化抢单数据");
                makeWorkOrderServiceX.makeSendWorkOrder(null);
            }
            //定时查询教师不够的情况
            else if (serviceTimerMessage.getType() == TimerMessageType.GRAB_ORDER_DATA_CLEAR_DAY.value()) {
                logger.info("=========>getGRAB_ORDER_DATA_CLEAR_DAY100message");
                logger.info("=========>清理抢单数据");
                makeWorkOrderServiceX.clearGrabData();
            }
            else if(serviceTimerMessage.getType() == TimerMessageType.COMMENT_CARD_NO_ANSWER.value()){
                logger.info("@TIMER>>>>>COMMENT_CARD_NO_ANSWER>>>>检查24小时和48小时内为点评的外教,判定重新分配或返还学生购买点评次数");
                foreignTeacherCommentCardService.foreignTeacherCommentUnAnswer();
                foreignTeacherCommentCardService.foreignTeacherCommentUnAnswer2();
            }
        } catch (Exception ex) {
            logger.error("检查教师失败", ex);
//            throw new AmqpRejectAndDontRequeueException("失败", ex);
        }
    }

    /**
     * 师生运营组发送的教师分配情况
     */
    @RabbitListener(queues = RabbitMqConstant.ASSIGNED_TEACHER_REPLY_QUEUE)
    public void assignTeacher(Map data) {
        if (data == null) {
            return;
        }

        logger.info("@assignTeacher接收分配老师Message:{},", JacksonUtil.toJSon(data));
        data.forEach((courseScheduleId, bean) -> {
            TeacherView teacherView = new TeacherView();
            Map<String, Object> beanMap = (Map<String, Object>) bean;

            if (!beanMap.get("teacherId").toString().equals("0")) {
                logger.debug("发送群组添加请求给教学组:{}学生加老师{}", beanMap.get("studentId"), beanMap.get("teacherId"));
                teacherView.setTeacherId(Long.valueOf(beanMap.get("teacherId").toString()));
                teacherView.setTeacherName((String) beanMap.get("teacherName"));
                courseScheduleUpdatorServiceX.handleWorkOrderAndCourseSchedule(Long.valueOf(courseScheduleId.toString()), teacherView);
            }
        });
    }

    /**
     *师生运营组发送的外教点评教师分配情况
     */
    @RabbitListener(queues = RabbitMqConstant.ALLOT_FOREIGN_TEACHER_COMMENT_QUEUE)
    public void assignForeignTeacher(String param) {
        if(param == null){
            throw new ValidationException();
        }
        FromTeacherStudentForm fromTeacherStudentForm = JacksonUtil.readValue(param,FromTeacherStudentForm.class);
        foreignTeacherCommentCardService.foreignTeacherCommentUpdateAnswer(fromTeacherStudentForm);
        logger.info("@assignForeignTeacher接收外教点评分配老师Message:{},", param);
    }
    /**
     * 小马更新状态
     */
    @RabbitListener(queues = RabbitMqConstant.TEACHING_ONLINE_STATUS_QUEUE)
    public void updateWorkOrderStatus(Map<String, Object> map) {
        logger.debug("@updateWorkOrderStatus接收来自在线教学组的更新状态请求,参数{}", JacksonUtil.toJSon(map));
        try {
            courseOnlineServiceX.updateTeachingStatus(map);
        } catch (Exception ex) {
            logger.error("@updateWorkOrderStatus,消息处理失败");
        }
    }

//    /**
//     * 抢单监听
//     * 1 初始化数据 生成能够抢单的工单
//     * 2 清理数据   每天清理一次
//     *
//     * @param serviceTimerMessage
//     * @throws Exception
//     */
//    //@RabbitListener(queues = RabbitMqConstant.GRAB_WORKER_ORDER_TIME_QUEUE)
//    public void timerGrabOrder(ServiceTimerMessage serviceTimerMessage) throws Exception {
//        try {
//            logger.info("@TIMER->->->->->->->接收来自定时器的消息(抢单),参数:{},", JacksonUtil.toJSon(serviceTimerMessage));
//            if (serviceTimerMessage.getType() == TimerMessageType.GRAB_ORDER_DATA_INIT.value()) {
//                logger.info("=========>初始化抢单数据");
//                makeWorkOrderServiceX.makeSendWorkOrder();
//
//            }
//            //定时查询教师不够的情况
//            else if (serviceTimerMessage.getType() == TimerMessageType.GRAB_ORDER_DATA_CLEAR_DAY.value()) {
//                logger.info("=========>清理抢单数据");
//                makeWorkOrderServiceX.clearGrabData();
//
//            }
//
//        } catch (Exception ex) {
//            logger.error("检查抢单数据失败", ex);
////            throw new AmqpRejectAndDontRequeueException("失败", ex);
//        }
//    }

}
