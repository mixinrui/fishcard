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
    @Scheduled(cron="0 1/5 * * * ?")
    public void notifyTeacherPrepareClass() {
        logger.info("<<<<<<开始通知<<<通知教师准备上课>>>的消息,时间[{}]", DateUtil.Date2String(new Date()));
        ServiceTimerMessage serviceTimerMessage = new ServiceTimerMessage(TimerMessageType.TEACHER_PREPARE_CLASS_NOTIFY.value());
        serviceTimerMessage.setTime(DateUtil.Date2String(new Date()));
        rabbitMqSender.send(serviceTimerMessage);
    }

    /**
     * 教师旷课通知定时器
     */
    @Scheduled(cron="0 0/5 * * * ?")
    public void notifyTeacherAbsentService() {
        logger.info("<<<<<<开始通知<<<教师旷课>>>的消息,时间[{}]", DateUtil.Date2String(new Date()));
        ServiceTimerMessage serviceTimerMessage = new ServiceTimerMessage(TimerMessageType.TEACHER_ABSENT_QUERY_NOTIFY.value());
        serviceTimerMessage.setTime(DateUtil.Date2String(new Date()));
        rabbitMqSender.send(serviceTimerMessage);
    }

    /**
     * 服务器强制下课定时器;从第三分钟开始,每五分钟;与上面区分开
     */
    @Scheduled(cron="0 2/5 * * * ?")
    public void completeForceService() {
        logger.info("<<<<<<开始通知<<<服务端强制下课>>>的消息,时间[{}]", DateUtil.Date2String(new Date()));
        ServiceTimerMessage serviceTimerMessage = new ServiceTimerMessage(TimerMessageType.COMPLETE_FORCE_SERVER_NOTIFY.value());
        serviceTimerMessage.setTime(DateUtil.Date2String(new Date()));
        rabbitMqSender.send(serviceTimerMessage);
    }

    /**
     * 学生旷课通知定时器;该功能并入强制完成
     */
//    @Scheduled(cron="0 4/5 * * * ?")
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
}
