package com.boxfishedu.workorder.service;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.FishCardDelayMsgType;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.base.BaseService;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by hucl on 16/6/8.
 */
@Component
public class FishCardStatusService extends BaseService<WorkOrder, WorkOrderJpaRepository, Long> {
    @Value("${parameter.pass_card_start_peroid}")
    private Integer passCardStartPeroid;
    @Value("${parameter.pass_card_wait_peroid}")
    private Integer passCardWaitPeroid;
    @Value("${parameter.pass_card_end_time}")
    private Integer passCardEndTime;
    @Value("${parameter.pass_beyond_normal}")
    private Integer passBeyondNormal;

    @Value("${parameter.teacher_absent_time_limit}")
    private Integer teacherAbsentTimeLimit;
    @Value("${parameter.student_absent_time_limit}")
    private Integer studentAbsentTimeLimit;
    @Value("${parameter.force_complete_time_limit}")
    private Integer forceCompleteTimeLimit;
    @Value("${parameter.notify_teacher_prepare_class_limit}")
    private Integer notifyTeacherPrepareLimit;

    private final long minutesOfDay=24*60;
    private final long minutesOfHour=60;

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    //找出所有上课时间大于当前时间,并且已经延迟3分钟的;这样的单教师旷课可能性更大
    public List<WorkOrder> getCardsToStart(){
        Date date=new Date();
        LocalDateTime endLocalDate = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).minusSeconds(60*passCardStartPeroid-1);
        //所有之前的数据都放入,做冗余处理;不会影响太多性能
        LocalDateTime startLocalDate = endLocalDate.minusMinutes(minutesOfDay);
        Date startDate= DateUtil.localDate2Date(startLocalDate);
        Date endDate=DateUtil.localDate2Date(endLocalDate);
        logger.debug("@query db开始从数据库查询[[[教师旷课数据]]],参数[startDate:{}    ;    endDate:{}    要求的鱼卡status;[{}]]"
                ,DateUtil.Date2String(startDate),DateUtil.Date2String(endDate)
                ,FishCardStatusEnum.TEACHER_ASSIGNED.getCode()+";"+FishCardStatusEnum.TEACHER_CANCEL_PUSH.getCode());
//        Integer[] statuses=new Integer[]{FishCardStatusEnum.STUDENT_ACCEPTED.getCode(),FishCardStatusEnum.TEACHER_CANCEL_PUSH.getCode()};
//        List<WorkOrder> result= jpa.findByStatusInAndStartTimeBetween(statuses, startDate, endDate);
        List<WorkOrder> result= jpa.findByStatusAndStartTimeBetween(FishCardStatusEnum.TEACHER_ASSIGNED.getCode(), startDate, endDate);
        return result;
    }


    //找出所有上课时间大于当前时间,并且状态处于等待学生应答状态超过3分钟的鱼卡,这样的鱼卡;学生旷课的可能性更大
    public List<WorkOrder> getCardsWaitStudentAccepted(){
        Date date=new Date();
        LocalDateTime endLocalDate = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).minusSeconds(60*passCardWaitPeroid-1);
        LocalDateTime startLocalDate = endLocalDate.minusMinutes(minutesOfDay);
        Date startDate= DateUtil.localDate2Date(startLocalDate);
        Date endDate=DateUtil.localDate2Date(endLocalDate);
        logger.debug("@query db开始从数据库查询[[[学生可能旷课]]],参数[startDate:{}    ;    endDate:{}    要求的鱼卡status;[{}]]"
                ,DateUtil.Date2String(startDate),DateUtil.Date2String(endDate),FishCardStatusEnum.WAITFORSTUDENT.getCode());
        List<WorkOrder> result= jpa.findByStatusAndStartTimeBetween(FishCardStatusEnum.WAITFORSTUDENT.getCode(), startDate, endDate);
        return result;
    }

    //找出结束时间大于应该结束时间3分钟,但是没有状态回应的鱼卡,这样的鱼卡出现服务器强制下课的可能性更大
    public List<WorkOrder> getCardsBeyondEndTime(){
        Date date=new Date();
        LocalDateTime endLocalDate = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).minusMinutes(passCardEndTime);
        LocalDateTime startLocalDate = endLocalDate.minusMinutes(minutesOfDay);
        Date startDate= DateUtil.localDate2Date(startLocalDate);
        Date endDate=DateUtil.localDate2Date(endLocalDate);
        logger.debug("@query db开始从数据库查询[[[需要强制下课]]],参数[startDate:{}    ;    endDate:{}    要求的鱼卡status;[{}]]"
                ,DateUtil.Date2String(startDate),DateUtil.Date2String(endDate),FishCardStatusEnum.ONCLASS.getCode());
        Integer[] statuses=new Integer[]{FishCardStatusEnum.STUDENT_ACCEPTED.getCode(),FishCardStatusEnum.READY.getCode()
                ,FishCardStatusEnum.ONCLASS.getCode(),FishCardStatusEnum.TEACHER_CANCEL_PUSH.getCode()
                ,FishCardStatusEnum.CONNECTED.getCode()};
        List<WorkOrder> result= jpa.findByStatusInAndOrderIdLessThanAndEndTimeBetween(statuses,Long.MAX_VALUE, startDate, endDate);
        return result;
    }

    public List<WorkOrder> getCardsTeacherPrepareClass(){
        Date date=new Date();
        LocalDateTime startLocalDate = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).plusSeconds(60*notifyTeacherPrepareLimit-1);
        //多放入一分钟的，以免漏过通知；为了防止重复通知，把最近两分钟通知过的数据放入redis；设置超时时间为两分钟
        LocalDateTime endLocalDate = startLocalDate.plusSeconds(60*notifyTeacherPrepareLimit+1);
        Date startDate= DateUtil.localDate2Date(startLocalDate);
        Date endDate=DateUtil.localDate2Date(endLocalDate);
        logger.debug("@query db开始从数据库查询[[[需要课前通知上课的老师]]],参数[startDate:{}    ;    endDate:{}    要求的鱼卡status;[{}]]"
                ,DateUtil.Date2String(startDate),DateUtil.Date2String(endDate),FishCardStatusEnum.TEACHER_ASSIGNED.getCode());
        List<WorkOrder> result= jpa.findByStatusAndStartTimeBetween(FishCardStatusEnum.TEACHER_ASSIGNED.getCode(), startDate, endDate);
        return result;
    }

    public MessageProperties getMsgProperties(Date date, FishCardDelayMsgType fishCardDelayMsgType){
        MessageProperties messageProperties = new MessageProperties();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        switch (fishCardDelayMsgType){
            case TEACHER_ABSENT: {
                calendar.add(Calendar.SECOND,60*teacherAbsentTimeLimit+passBeyondNormal);
                break;
            }
            case STUDENT_ABSENT: {
                calendar.add(Calendar.SECOND,60*studentAbsentTimeLimit+passBeyondNormal);
                break;
            }
            case FORCE_COMPLETE_SERVER: {
                calendar.add(Calendar.SECOND,60*forceCompleteTimeLimit+4*passBeyondNormal);
                break;
            }
            case NOTIFY_TEACHER_PREPARE_CLASS:{
                calendar.add(Calendar.SECOND,0-60*notifyTeacherPrepareLimit+passBeyondNormal);
                break;
            }
            default:
                break;
        }
        Date deadDate =calendar.getTime();
        Date now=new Date();
        Long diff=deadDate.getTime()-now.getTime();
        if(diff<0l){
            diff=0l;
        }
        messageProperties.setExpiration(diff+"");
        messageProperties.setTimestamp(new Date());
        return messageProperties;
    }
}
