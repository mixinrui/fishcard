package com.boxfishedu.fishcard.timer.task;

import com.boxfishedu.card.bean.ServiceTimerMessage;
import com.boxfishedu.card.bean.TimerMessageType;
import com.boxfishedu.fishcard.timer.common.util.DateUtil;
import com.boxfishedu.fishcard.timer.mq.RabbitMqSender;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Created by hucl on 16/5/7.
 */
@Component
public class NotifyTimer {
    @Autowired
    private RabbitMqSender rabbitMqSender;

    private org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    //定时任务，向师生运营组获取教师
    @Scheduled(cron = "0 0 1 * * ?")
//    @Scheduled(cron="0 0/1 * * * ?")//每30分钟执行一次ls
    public void notifyService() {
        logger.info("<<<<<<开始通知<<<开始通知向师生运营获取没有分配的教师>>>的消息,时间[{}]", DateUtil.Date2String(new Date()));
        ServiceTimerMessage serviceTimerMessage = new ServiceTimerMessage();
        serviceTimerMessage.setStatus(0);
        serviceTimerMessage.setTime(DateUtil.Date2String(new Date()));
        serviceTimerMessage.setType(TimerMessageType.TEACHER_ASSIGN_NOTIFY.value());
        serviceTimerMessage.setBody(null);
        rabbitMqSender.send(serviceTimerMessage);
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void teacherOutNumberNotifyService() {
        logger.info("<<<<<<开始通知<<<检查教师不足预警>>>的消息,时间[{}]", DateUtil.Date2String(new Date()));
        ServiceTimerMessage serviceTimerMessage = new ServiceTimerMessage();
        serviceTimerMessage.setStatus(0);
        serviceTimerMessage.setTime(DateUtil.Date2String(new Date()));
        serviceTimerMessage.setType(TimerMessageType.TEACHER_OUT_NUM_NOTIFY.value());
        serviceTimerMessage.setBody(null);
        Date date = new Date();
        LocalDateTime startDate = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        LocalDateTime endDate = startDate.plusDays(2);
        serviceTimerMessage.setStartTime(DateUtil.LocalDate2String(startDate));
        serviceTimerMessage.setEndTime(DateUtil.LocalDate2String(endDate));
        rabbitMqSender.send(serviceTimerMessage);
    }

    @Scheduled(cron = "0 0 18 * * ?")
    public void teacherNewClassNotifyService() {
        logger.info("<<<<<<开始通知<<<通知教师的新课程数量>>>的消息,时间[{}]", DateUtil.Date2String(new Date()));
        ServiceTimerMessage serviceTimerMessage = new ServiceTimerMessage();
        serviceTimerMessage.setStatus(0);
        serviceTimerMessage.setTime(DateUtil.Date2String(new Date()));
        serviceTimerMessage.setType(TimerMessageType.TEACHER_COURSE_NEW_ASSIGNEDED_DAY.value());
        serviceTimerMessage.setBody(null);
        Date date = new Date();
        rabbitMqSender.send(serviceTimerMessage);
    }

    /**
     * 教师上课前五分钟提醒定时器
     */
    @Scheduled(cron = "0 1/5 * * * ?")
    public void notifyTeacherPrepareClass() {
        logger.info("<<<<<<开始通知<<<通知教师准备上课>>>的消息,时间[{}]", DateUtil.Date2String(new Date()));
        ServiceTimerMessage serviceTimerMessage = new ServiceTimerMessage(TimerMessageType.TEACHER_PREPARE_CLASS_NOTIFY.value());
        serviceTimerMessage.setTime(DateUtil.Date2String(new Date()));
        rabbitMqSender.send(serviceTimerMessage);
    }

    /**
     * 教师旷课通知定时器
     */
    @Scheduled(cron="0 0/10 * * * ?")
    public void notifyTeacherAbsentService() {
        logger.info("<<<<<<开始通知<<<教师旷课>>>的消息,时间[{}]", DateUtil.Date2String(new Date()));
        ServiceTimerMessage serviceTimerMessage = new ServiceTimerMessage(TimerMessageType.TEACHER_ABSENT_QUERY_NOTIFY.value());
        serviceTimerMessage.setTime(DateUtil.Date2String(new Date()));
        rabbitMqSender.send(serviceTimerMessage);
    }

    /**
     * 服务器强制下课定时器;从第三分钟开始,每五分钟;与上面区分开;目前允许将拖堂时间上升为10分钟
     */
    @Scheduled(cron="0 2/10 * * * ?")
    public void completeForceService() {
        logger.info("<<<<<<开始通知<<<服务端强制下课>>>的消息,时间[{}]", DateUtil.Date2String(new Date()));
        ServiceTimerMessage serviceTimerMessage = new ServiceTimerMessage(TimerMessageType.COMPLETE_FORCE_SERVER_NOTIFY.value());
        serviceTimerMessage.setTime(DateUtil.Date2String(new Date()));
        rabbitMqSender.send(serviceTimerMessage);
    }

    /**
     * 学生旷课通知定时器;该功能并入强制完成
     */
    @Scheduled(cron="0 4/10 * * * ?")
    public void notifyStudentAbsentService() {
        logger.info("<<<<<<开始通知<<<检查学生旷课>>>的消息,时间[{}]", DateUtil.Date2String(new Date()));
        ServiceTimerMessage serviceTimerMessage = new ServiceTimerMessage(TimerMessageType.STUDENT_ABSENT_QUERY_NOTIFY.value());
        serviceTimerMessage.setTime(DateUtil.Date2String(new Date()));
        rabbitMqSender.send(serviceTimerMessage);
    }

    /**
     * 服务器检测没有补课的超时课程信息..每天4点检查一次;此功能暂时停用
     */
//    @Scheduled(cron = "0 0 4 * * ?")
    public void studentNotMakeUpService() {
        logger.info("<<<<<<开始通知<<<超时没补课>>>的消息,时间[{}]", DateUtil.Date2String(new Date()));
        ServiceTimerMessage serviceTimerMessage = new ServiceTimerMessage(TimerMessageType.STUDENT_NOT_MAKEUP_NOTIFY.value());
        serviceTimerMessage.setTime(DateUtil.Date2String(new Date()));
        rabbitMqSender.send(serviceTimerMessage);
    }

    /**
     * 外教点评定时检查
     */
//    @Scheduled(cron = "0 0/5 * * * ?")
    @Scheduled(cron = "0 0/2 * * * ?")
    public void judgeCommentCard(){
        logger.info("<<<<<<开始通知<<<获取在24/48小时内未评论的外教或没分配到老师的点评卡>>>的消息,时间[{}]", DateUtil.Date2String(new Date()));
        ServiceTimerMessage serviceTimerMessage = new ServiceTimerMessage(TimerMessageType.COMMENT_CARD_NO_ANSWER.value());
        serviceTimerMessage.setTime(DateUtil.Date2String(new Date()));
        rabbitMqSender.send(serviceTimerMessage);
    }

    /**
     * 抢单: 每天18点到 24点  每10分钟 轮训查询(中教)
     */
   @Scheduled(cron = "0 0/10 18,19,20,21,22,23 * * ?")
    public void initGrabOrderDataChinese() {
        logger.info("<<<<<<graborder-initGrabOrderDataChinese<<<<<<<<<<<<<<<<");
        logger.info("<<<<<<开始通知<<<轮训抢单初始化数据>>>的消息,时间[{}]", DateUtil.Date2String(new Date()));
        ServiceTimerMessage serviceTimerMessage = new ServiceTimerMessage(TimerMessageType.GRAB_ORDER_DATA_INIT.value());
        serviceTimerMessage.setTime(DateUtil.Date2String(new Date()));
        rabbitMqSender.send(serviceTimerMessage);
    }

    /**
     * 抢单: 每天18点到 24点  每10分钟 轮训查询(外教)
     */
    @Scheduled(cron = "0 0/10 18,19,20,21,22,23 * * ?")
    public void initGrabOrderDataForeigh() {
        logger.info("<<<<<<graborder-initGrabOrderDataForeigh<<<<<<<<<<<<<<<<");
        logger.info("<<<<<<开始通知<<<轮训抢单初始化数据外教>>>的消息,时间[{}]", DateUtil.Date2String(new Date()));
        ServiceTimerMessage serviceTimerMessage = new ServiceTimerMessage(TimerMessageType.GRAB_ORDER_DATA_INIT_FOREIGN.value());
        serviceTimerMessage.setTime(DateUtil.Date2String(new Date()));
        rabbitMqSender.send(serviceTimerMessage);
    }

    /**
     * 抢单:每天17:40清理数据
     */
    @Scheduled(cron = "0 40 17 * * ?")
    public void clearGrabOrderDataChinese() {
        logger.info("<<<<<<graborder-clearGrabOrderData<<<<<<<<<<<<<<<<");
        logger.info("<<<<<<开始通知<<<清理昨天抢单历史数据>>>的消息,时间[{}]", DateUtil.Date2String(new Date()));
        ServiceTimerMessage serviceTimerMessage = new ServiceTimerMessage(TimerMessageType.GRAB_ORDER_DATA_CLEAR_DAY.value());
        serviceTimerMessage.setTime(DateUtil.Date2String(new Date()));
        rabbitMqSender.send(serviceTimerMessage);
    }

    /**
     * 抢单:每天17:40清理数据
     */
    //@Scheduled(cron = "0 40 17 * * ?")
    public void clearGrabOrderDataForeigh() {
        logger.info("<<<<<<graborder-clearGrabOrderDataForeigh<<<<<<<<<<<<<<<<");
        logger.info("<<<<<<开始通知<<<清理昨天抢单历史数据外教>>>的消息,时间[{}]", DateUtil.Date2String(new Date()));
        ServiceTimerMessage serviceTimerMessage = new ServiceTimerMessage(TimerMessageType.GRAB_ORDER_DATA_CLEAR_DAY_FOREIGH.value());
        serviceTimerMessage.setTime(DateUtil.Date2String(new Date()));
        rabbitMqSender.send(serviceTimerMessage);
    }


    /**
     * 每天18:00 向教师发送 从现在开始  未来48+6小时内 变更课程的数量  的消息
     */
     @Scheduled(cron = "0 0 18 * * ?")
    public void courseChangeSendMessage() {
        logger.info("<<<<<<courseChangeSendMessage<<<<<<<<<<<<<<<<");
        logger.info("<<<<<<开始通知<<< 变更课程的数量  >>>的消息,时间[{}]", DateUtil.Date2String(new Date()));
        ServiceTimerMessage serviceTimerMessage = new ServiceTimerMessage(TimerMessageType.COURSE_CHANGER_WORKORDER.value());
        serviceTimerMessage.setTime(DateUtil.Date2String(new Date()));
        rabbitMqSender.send(serviceTimerMessage);
    }



    /**
     * 通知学生明天 有几节课
     */
    @Scheduled(cron = "0 30 18 * * ?")
    public void notifyTomoStudentHasClass() {
        logger.info("<<<<<<notifyTomoStudentHasClass<<<<<<<<<<<<<<<<");
        logger.info("<<<<<<开始通知<<<学生明天有课>>>的消息,时间[{}]", DateUtil.Date2String(new Date()));
        ServiceTimerMessage serviceTimerMessage = new ServiceTimerMessage(TimerMessageType.CLASSS_TOMO_STU_NOTIFY.value());
        serviceTimerMessage.setTime(DateUtil.Date2String(new Date()));
        rabbitMqSender.send(serviceTimerMessage);
    }

    /**
     * 7点通知老师今天有几节课 第一节什么时间上
     */
    @Scheduled(cron = "0 30 07 * * ?")
    public void notifyTodyTeacherHasClass() {
        logger.info("<<<<<<notifyTodyTeacherHasClass<<<<<<<<<<<<<<<<");
        logger.info("<<<<<<开始通知<<<老师今天有课>>>的消息,时间[{}]", DateUtil.Date2String(new Date()));
        ServiceTimerMessage serviceTimerMessage = new ServiceTimerMessage(TimerMessageType.CLASSS_TODY_TEA_NOTIFY.value());
        serviceTimerMessage.setTime(DateUtil.Date2String(new Date()));
        rabbitMqSender.send(serviceTimerMessage);
    }

    /**
     * 查询旷课的学生,对其扣积分
     */

    @Scheduled(cron = "0 0 1 * * ?")
//    @Scheduled(cron = "0 0/5 * * * ?")    //测试时5分钟检查一次
    public void deductScore(){
        logger.info("<<<<<<deductScore<<<<<<<<<<<<<<<<");
        logger.info("<<<<<<查询旷课的学生<<<<<<扣积分,时间[{}]", DateUtil.Date2String(new Date()));
        ServiceTimerMessage serviceTimerMessage = new ServiceTimerMessage(TimerMessageType.STUDENT_ABSENT_DEDUCT_SCORE.value());
        serviceTimerMessage.setTime(DateUtil.Date2String(new Date()));
        rabbitMqSender.send(serviceTimerMessage);
    }


    //定时任务，处理冻结的课程
    @Scheduled(cron = "0 0 2 * * ?")
    public void dealFreeze() {
        logger.info("<<<<<<开始通知<<<开始通知处理冻结的鱼卡,并更新首页>>>的消息,时间[{}]", DateUtil.Date2String(new Date()));
        ServiceTimerMessage serviceTimerMessage = new ServiceTimerMessage();
        serviceTimerMessage.setStatus(0);
        serviceTimerMessage.setTime(DateUtil.Date2String(new Date()));
        serviceTimerMessage.setType(TimerMessageType.FREEZE_UPDATE_HOME.value());
        serviceTimerMessage.setBody(null);
        rabbitMqSender.send(serviceTimerMessage);
    }

    // 课程推荐
    @Scheduled(cron = "0 0 3 * * ?")
//    @Scheduled(cron = "0 0/5 * * * ?")
    public void recommendCourses(){
        logger.info("<<<<<<recommendCourses<<<<<<<<<<<<<<<<");
        logger.info("<<<<<<课程推荐<<<<<<,时间[{}]", DateUtil.Date2String(new Date()));
        ServiceTimerMessage serviceTimerMessage = new ServiceTimerMessage(TimerMessageType.RECOMMEND_COURSES.value());
        serviceTimerMessage.setTime(DateUtil.Date2String(new Date()));
        rabbitMqSender.send(serviceTimerMessage);
    }

    /**
     * 鱼卡自动确认状态
     */
    //@Scheduled(cron = "0 0 4 * * ?")
    //@Scheduled(cron = "0 10 15 * * ?")
    public void autoConfirmStatus(){
//        logger.info("<<<<<<autoConfirmStatus<<<<<<<<<<<<<<<<");
//        logger.info("<<<<<<自动确认状态<<<<<<,时间[{}]", DateUtil.Date2String(new Date()));
//        ServiceTimerMessage serviceTimerMessage = new ServiceTimerMessage(TimerMessageType.RECOMMEND_COURSES.value());
//        serviceTimerMessage.setTime(DateUtil.Date2String(new Date()));
//        rabbitMqSender.send(serviceTimerMessage);
    }

    // 即时上课,每20S一次
    @Scheduled(cron = "0/20 * * * * ?")
    public void instantClasses(){
        logger.info("<<<<<<instantClasses<<<<<<<<<<<<<<<<");
        ServiceTimerMessage serviceTimerMessage = new ServiceTimerMessage(TimerMessageType.INSTANT_CLASS.value());
        serviceTimerMessage.setTime(DateUtil.Date2String(new Date()));
        rabbitMqSender.send(serviceTimerMessage);
    }

    @Scheduled(cron = "0 0/1 * * * ?")
    public void markUnmatchInstantClass(){
        logger.info("<<<<<<markUnmatchInstantClass<<<<<<<<<<<<<<<<");
        logger.info("<<<<<<自动将超过一分钟没匹配上教师的instantCard标记为未匹配教师<<<<<<,时间[{}]", DateUtil.Date2String(new Date()));
        ServiceTimerMessage serviceTimerMessage = new ServiceTimerMessage(TimerMessageType.INSTANT_CLASS_MARK_UNMATCH.value());
        serviceTimerMessage.setTime(DateUtil.Date2String(new Date()));
        rabbitMqSender.send(serviceTimerMessage);
    }

    /**
     * 定时清理没有匹配上的课程,将相应的课退回到推荐课系统
     */
    @Scheduled(cron = "0 1/30 * * * ?")
    public void pushBackCourses() {
        logger.info("<<<<<<开始通知<<<退回没有成功的实时抢课的课程>>>的消息,时间[{}]", DateUtil.Date2String(new Date()));
        ServiceTimerMessage serviceTimerMessage = new ServiceTimerMessage(TimerMessageType.INSTANT_CLASS_BACK_COURSES.value());
        serviceTimerMessage.setTime(DateUtil.Date2String(new Date()));
        rabbitMqSender.send(serviceTimerMessage);
    }


    /**
     * 外教点评会员过期提前三天提示
     */
//    @Scheduled(cron = "0 0 17 * * ?")
    public void pushExpireCommentCard() {
        logger.info("<<<<<<开始通知<<<外教点评会员过期提醒>>>的消息,时间[{}]", DateUtil.Date2String(new Date()));
        ServiceTimerMessage serviceTimerMessage = new ServiceTimerMessage(TimerMessageType.EXPIRE_COMMENT_CARD.value());
        serviceTimerMessage.setTime(DateUtil.Date2String(new Date()));
        rabbitMqSender.send(serviceTimerMessage);
    }

    /**
     * 指定老师====每周周6的凌晨4点开始跑定时任务
     * @Scheduled(cron = "0 0 4 0 0 6 ")
     */
    @Scheduled(cron = "0 0 4 * * 6 ")
    public void assginTeacher(){
        logger.info("<<<<<<@@@@assign-timer 开始通知<<<指定老师>>>的消息,时间[{}]", DateUtil.Date2String(new Date()));
        ServiceTimerMessage serviceTimerMessage = new ServiceTimerMessage(TimerMessageType.ASSGIN_TEACHER.value());
        serviceTimerMessage.setTime(DateUtil.Date2String(new Date()));
        rabbitMqSender.send(serviceTimerMessage);
    }
}
