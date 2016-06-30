package com.boxfishedu.workorder.common.rabbitmq;

import com.boxfishedu.card.bean.ServiceTimerMessage;
import com.boxfishedu.card.bean.TimerMessageType;
import com.boxfishedu.online.order.entity.OrderForm;
import com.boxfishedu.workorder.common.bean.QueueTypeEnum;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.service.ServeService;
import com.boxfishedu.workorder.servicex.courseonline.CourseOnlineServiceX;
import com.boxfishedu.workorder.servicex.orderrelated.OrderRelatedServiceX;
import com.boxfishedu.workorder.servicex.timer.CourseScheduleUpdatorServiceX;
import com.boxfishedu.workorder.servicex.timer.FishCardStatusFinderServiceX;
import com.boxfishedu.workorder.servicex.timer.FishCardUpdatorServiceX;
import com.boxfishedu.workorder.servicex.timer.ScheduleTeachersStasticsServiceX;
import com.boxfishedu.workorder.web.view.teacher.TeacherView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
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
    private FishCardStatusFinderServiceX fishCardStatusFinderServiceX;


    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 订单中心转换请求
     */
    @RabbitListener(queues = RabbitMqConstant.ORDER_TO_SERVICE_QUEUE)
    public void orderConsumer(OrderForm orderView) throws Exception {
        logger.info("@orderConsumer");
        try {
            serveService.order2ServiceAndWorkOrder(orderView);
        }
        catch (Exception ex){
            logger.error("订单[{}]转换失败",orderView.getId());
            throw new Exception("转换失败放回队列");
        }
//        logger.info("收到来自订单中心的转换请求,订单id:[{}]",orderView.getId());
//        orderRelatedServiceX.preHandleOrder(orderView);
////        orderRelatedServiceX.order2ServiceAndWorkOrder(orderView);
//        System.out.println("##############");
//        return null;
    }


    /**
     *定时器消息监听
     */
    @RabbitListener(queues = RabbitMqConstant.UNASSIGNED_TEACHER_TIMER_QUEUE)
    public void timerMessageListener(ServiceTimerMessage serviceTimerMessage) throws Exception {
        try {
            logger.info("@TIMER->->->->->->->接收来自定时器的消息,参数:{},", JacksonUtil.toJSon(serviceTimerMessage));
            if(serviceTimerMessage.getType()== TimerMessageType.TEACHER_ASSIGN_NOTIFY.value()) {
                logger.info("批量更新教师入库开始");
                courseScheduleUpdatorServiceX.bathUpdateTeacherIntoSchedule();
                ServiceTimerMessage result=new ServiceTimerMessage();
                result.setType(TimerMessageType.TEACHER_ASSIGN_NOTIFY_REPLY.value());
                result.setStatus(0);
                result.setBody(null);
                Date date=new Date();
                ServiceTimerMessage replay=new ServiceTimerMessage();
                replay.setTime(DateUtil.Date2String(new Date()));
                replay.setType(TimerMessageType.TEACHER_ASSIGN_NOTIFY_REPLY.value());
                rabbitMqSender.send(replay, QueueTypeEnum.ASSIGN_TEACHER_TIMER_REPLY);
            }
            //定时查询教师不够的情况
            else if(serviceTimerMessage.getType()== TimerMessageType.TEACHER_OUT_NUM_NOTIFY.value()){
                logger.info("@TIMER>>>>QUERY_UNASSIGNED>>>检查教师的分配情况开始");
                scheduleTeachersStasticsServiceX.teacherNumAlter(serviceTimerMessage);
            }
            else if(serviceTimerMessage.getType()==TimerMessageType.TEACHER_ABSENT_QUERY_NOTIFY.value()){
                logger.info("@TIMER>>>>>TEACHER_ABSENT>>>>>检查教师旷课情况开始");
                fishCardStatusFinderServiceX.teacherAbsentFinder();
            }
            else if(serviceTimerMessage.getType()==TimerMessageType.STUDENT_ABSENT_QUERY_NOTIFY.value()){
                logger.info("@TIMER>>>>>STUDENT_ABSENT>>>>>>检查学生旷课情况开始");
                fishCardStatusFinderServiceX.studentAbsentFinder();
            }
            else if(serviceTimerMessage.getType()==TimerMessageType.COMPLETE_FORCE_SERVER_NOTIFY.value()){
                logger.info("@TIMER>>>>>SERVER_FORCE_COMPLETED>>>>>检查服务端强制下课情况开始");
                fishCardStatusFinderServiceX.forceCompleteFinder();
            }
            else if(serviceTimerMessage.getType()==TimerMessageType.TEACHER_PREPARE_CLASS_NOTIFY.value()){
                logger.info("@TIMER>>>>>TEACHER_PREPARE>>>>检查提醒教师准备上课的情况");
                fishCardStatusFinderServiceX.teacherPrepareClassFinder();
            }
            else if(serviceTimerMessage.getType()==TimerMessageType.STUDENT_NOT_MAKEUP_NOTIFY.value()){
                logger.info("@TIMER>>>>>STUDENT_NOT_MAKEUP_NOTIFY>>>>检查学生超期未补课的情况");
            }
        } catch (Exception ex) {
            logger.error("检查教师失败",ex);
            throw new AmqpRejectAndDontRequeueException("失败", ex);
        }
    }

    /**
     *师生运营组发送的教师分配情况
     */
    @RabbitListener(queues = RabbitMqConstant.ASSIGNED_TEACHER_REPLY_QUEUE)
    public void assignTeacher(Map data) {
        if(data == null) {
            return;
        }

        logger.info("@assignTeacher接收分配老师Message:{},", JacksonUtil.toJSon(data));
        data.forEach( (courseScheduleId, bean) -> {
            TeacherView teacherView = new TeacherView();
            Map<String, Object> beanMap = (Map<String, Object>) bean;

            if(!beanMap.get("teacherId").toString().equals("0")) {
                logger.debug("发送群组添加请求给教学组:{}学生加老师{}", beanMap.get("studentId"), beanMap.get("teacherId"));
                teacherView.setTeacherId(Long.valueOf(beanMap.get("teacherId").toString()));
                teacherView.setTeacherName((String) beanMap.get("teacherName"));
                courseScheduleUpdatorServiceX.handleWorkOrderAndCourseSchedule(Long.valueOf(courseScheduleId.toString()), teacherView);
            }
        });
    }

    /**
     * 小马更新状态
     */
    @RabbitListener(queues = RabbitMqConstant.TEACHING_ONLINE_STATUS_QUEUE)
    public void updateWorkOrderStatus(Map<String,Object> map){
        logger.debug("@updateWorkOrderStatus接收来自在线教学组的更新状态请求,参数{}", JacksonUtil.toJSon(map));
        courseOnlineServiceX.updateTeachingStatus(map);
    }
}
