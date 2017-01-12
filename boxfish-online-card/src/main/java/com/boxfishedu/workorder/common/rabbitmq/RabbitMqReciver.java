package com.boxfishedu.workorder.common.rabbitmq;

import com.boxfishedu.card.bean.CourseTypeEnum;
import com.boxfishedu.card.bean.ServiceTimerMessage;
import com.boxfishedu.card.bean.TimerMessageType;
import com.boxfishedu.mall.domain.order.OrderForm;
import com.boxfishedu.workorder.common.bean.QueueTypeEnum;
import com.boxfishedu.workorder.common.log.ServiceLog;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.JSONParser;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.entity.mysql.FromTeacherStudentForm;
import com.boxfishedu.workorder.entity.mysql.UpdatePicturesForm;
import com.boxfishedu.workorder.service.ServeService;
import com.boxfishedu.workorder.service.absenteeism.AbsenteeismService;
import com.boxfishedu.workorder.service.accountcardinfo.DataCollectorService;
import com.boxfishedu.workorder.service.accountcardinfo.OnlineAccountService;
import com.boxfishedu.workorder.service.commentcard.ForeignTeacherCommentCardService;
import com.boxfishedu.workorder.servicex.assignTeacher.AssignTeacherServiceX;
import com.boxfishedu.workorder.servicex.coursenotify.CourseNotifyOneDayServiceX;
import com.boxfishedu.workorder.servicex.courseonline.CourseOnlineServiceX;
import com.boxfishedu.workorder.servicex.fishcardcenter.AutuConfirmFishCardServiceX;
import com.boxfishedu.workorder.servicex.graborder.CourseChangeServiceX;
import com.boxfishedu.workorder.servicex.graborder.MakeWorkOrderServiceX;
import com.boxfishedu.workorder.servicex.instantclass.timer.InstantClassTimerServiceX;
import com.boxfishedu.workorder.servicex.orderrelated.OrderRelatedServiceX;
import com.boxfishedu.workorder.servicex.studentrelated.PublicClassRoom;
import com.boxfishedu.workorder.servicex.timer.*;
import com.boxfishedu.workorder.web.view.teacher.TeacherView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * Created by hucl on 16/4/15.
 */
@Component
@Configuration
@Profile({"local_hucl","product","local","development","development_new","test","demo","pretest"})
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

    @Autowired
    private CourseChangeServiceX courseChangeServiceX;

    @Autowired
    private CourseNotifyOneDayServiceX courseNotifyOneDayServiceX;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AbsenteeismService absenteeismService;

    @Autowired
    private DataCollectorService dataCollectorService;

    @Autowired
    private OnlineAccountService onlineAccountService;

    @Autowired
    private AutuConfirmFishCardServiceX autuConfirmFishCardServiceX;

    @Autowired
    private InstantClassTimerServiceX instantClassTimerServiceX;

    @Autowired
    private AssignTeacherServiceX assignTeacherServiceX;

    @Autowired
    private PublicClassRoom publicClassRoom;


    /**
     * 订单中心转换请求
     */
    @RabbitListener(queues = RabbitMqConstant.ORDER_TO_SERVICE_QUEUE)
    public void orderConsumer(OrderForm orderView) {
        logger.info("@orderConsumer");
        try {
            System.out.println(orderView);
            serveService.order2ServiceAndWorkOrder(orderView);

            //更新首页和用户信息
            dataCollectorService.updateBothChnAndFnItemAsync(orderView.getUserId());
            onlineAccountService.add(orderView.getUserId());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(
                    new ServiceLog()
                            .data(orderView)
                            .errorLevel()
                            .operation("订单转换为服务")
                            .toString());
            logger.error("订单[{}]转换失败", orderView.getId());
//            throw new Exception("转换失败放回队列");
        }
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
                logger.info("=========>初始化抢单数据(中教)");
                makeWorkOrderServiceX.makeSendWorkOrder(null, CourseTypeEnum.FUNCTION.toString());
            }else if (serviceTimerMessage.getType() == TimerMessageType.GRAB_ORDER_DATA_INIT_FOREIGN.value()) {
                logger.info("=========>getGRAB_ORDER_DATA_INIT91message");
                logger.info("=========>初始化抢单数据(外教)");
                makeWorkOrderServiceX.makeSendWorkOrder(null, CourseTypeEnum.TALK.toString());
            }
            //定时查询教师不够的情况
            else if (serviceTimerMessage.getType() == TimerMessageType.GRAB_ORDER_DATA_CLEAR_DAY.value()) {
                logger.info("=========>getGRAB_ORDER_DATA_CLEAR_DAY100message");
                logger.info("=========>清理抢单数据(中教)");
                makeWorkOrderServiceX.clearGrabData();
            }else if (serviceTimerMessage.getType() == TimerMessageType.GRAB_ORDER_DATA_CLEAR_DAY_FOREIGH.value()) {
                logger.info("=========>getGRAB_ORDER_DATA_CLEAR_DAY101message");
                logger.info("=========>清理抢单数据(外教)");
                makeWorkOrderServiceX.clearGrabData();
            }else if(serviceTimerMessage.getType() == TimerMessageType.COURSE_CHANGER_WORKORDER.value()){
                courseChangeServiceX.sendCourseChangeWorkOrders();
            }
            else if(serviceTimerMessage.getType() == TimerMessageType.COMMENT_CARD_NO_ANSWER.value()){
                logger.info("@CommentCardTimer>>>>>COMMENT_CARD_NO_ANSWER>>>>检查24小时和48小时内为点评的外教,判定重新分配或返还学生购买点评次数");
                foreignTeacherCommentCardService.foreignTeacherCommentUnAnswer();
                foreignTeacherCommentCardService.foreignTeacherCommentUnAnswer2();
                foreignTeacherCommentCardService.foreignUndistributedTeacherCommentCards();
            }else if(serviceTimerMessage.getType() == TimerMessageType.CLASSS_TOMO_STU_NOTIFY.value()){
                logger.info("=========>receiveMessageTomoStuhasClass");
                courseNotifyOneDayServiceX.notiFyStudentClass();
            }else if(serviceTimerMessage.getType() == TimerMessageType.STUDENT_ABSENT_DEDUCT_SCORE.value()){
                logger.info("@CommentCardTimer>>>>>STUDENT_ABSENT_DEDUCT_SCORE>>>>查询旷课的学生,扣积分");
                absenteeismService.queryAbsentStudent();
            }else if(serviceTimerMessage.getType() == TimerMessageType.CLASSS_TODY_TEA_NOTIFY.value()){
                logger.info("=========>notifyTomoStudentHasClass=====>>>>通知老师今天有课");
                courseNotifyOneDayServiceX.notiFyTeacherClass();
            }else if(serviceTimerMessage.getType()==TimerMessageType.FREEZE_UPDATE_HOME.value()){
                courseScheduleUpdatorServiceX.freezeUpdateHome();
            } else if(serviceTimerMessage.getType() == TimerMessageType.RECOMMEND_COURSES.value()) {
                courseScheduleUpdatorServiceX.recommendCourses();
            } else  if(serviceTimerMessage.getType() == TimerMessageType.AUTO_CONFIRM_STATUS.value()){
                logger.info("==========>AutuConfirmFishCardServiceX ===>>> 自动确认鱼卡状态");
                autuConfirmFishCardServiceX.autoConfirmFishCard();
            }else if(serviceTimerMessage.getType() == TimerMessageType.INSTANT_CLASS.value()){
                logger.info("==========>INSTANT_CLASS ===>>> 立即上课");
                instantClassTimerServiceX.putCardsToMatchTeachers();
            }
            else if(serviceTimerMessage.getType() == TimerMessageType.INSTANT_CLASS_MARK_UNMATCH.value()){
                logger.info("==========>INSTANT_CLASS_MARK_UNMATCH ===>>> 一分钟未匹配教师,标记未匹配");
                instantClassTimerServiceX.markUnmatchCard();
            }
            else if(serviceTimerMessage.getType() == TimerMessageType.INSTANT_CLASS_BACK_COURSES.value()){
                logger.info("==========>INSTANT_CLASS_BACK_COURSES ===>>> 未上的课程退回到课程推荐");
                instantClassTimerServiceX.backUnmatchCoursesAsync();
            } else if(serviceTimerMessage.getType() == TimerMessageType.EXPIRE_COMMENT_CARD.value()) {
                logger.info("==========>EXPIRE_COMMENT_CARD ===>>> 会员外教点评过期提醒");
                foreignTeacherCommentCardService.notifyExpireCommentCards();
            }
            else if(serviceTimerMessage.getType() == TimerMessageType.ASSGIN_TEACHER.value()) {
                logger.info("==========>@@@@assign-timer===>>> 指定老师定时任务接受到任务");
                assignTeacherServiceX.autoAssign();
            } else if(serviceTimerMessage.getType() == TimerMessageType.EXPIRE_PUBLIC_CLASS.value()) {
                logger.info("==========>@@@@EXPIRE_PUBLIC_CLASS===>>> 删除公开课缓存");
                publicClassRoom.expireClassRoomCache();
            } else if(serviceTimerMessage.getType() == TimerMessageType.PUBLIC_CLASS_NOTIFY.value()) {
                logger.info("==========>@@@@PUBLIC_CLASS_NOTIFY===>>> 公开课通知");
                publicClassRoom.publicClassRoomNotification();
            }
            //
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
        try{
            FromTeacherStudentForm fromTeacherStudentForm = JSONParser.fromJson(param,FromTeacherStudentForm.class);
            foreignTeacherCommentCardService.foreignTeacherCommentUpdateAnswer(fromTeacherStudentForm);
            logger.info("@assignForeignTeacher接收外教点评分配老师Message:{},", param);
        }catch (Exception e){
            e.printStackTrace();
            logger.info("@assignForeignTeacher接收外教点评分配老师失败!");
        }
    }

    /**
     * 种老师通知外教点评卡头像更新
     */
    @RabbitListener(queues = RabbitMqConstant.UPDATE_PICTURE_QUEUE)
    public void updateCommentCardsPictures(String param){
        try{
            UpdatePicturesForm updatePicturesForm = JSONParser.fromJson(param,UpdatePicturesForm.class);
            if(updatePicturesForm.getFigure_url().isEmpty()){
                throw new AmqpRejectAndDontRequeueException("param 为 null!");
            }else {
                foreignTeacherCommentCardService.updateCommentCardsPictures(updatePicturesForm);
                logger.info("@updateCommentCardsPictures接收修改外教点评卡头像Message:{},", param);
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.info("接收头像更新通知,但更新失败!");
        }
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
            logger.error("@updateWorkOrderStatus,消息[{}]处理失败",JacksonUtil.toJSon(map));
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
