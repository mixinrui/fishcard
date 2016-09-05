package com.boxfishedu.workorder.servicex.timer;

import com.boxfishedu.workorder.common.bean.CompleteForceComparator;
import com.boxfishedu.workorder.common.bean.FishCardDelayMessage;
import com.boxfishedu.workorder.common.bean.FishCardDelayMsgType;
import com.boxfishedu.workorder.common.rabbitmq.RabbitMqDelaySender;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.FishCardStatusService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by hucl on 2016/6/20.
 */
@Component
public class FishCardStatusFinderServiceX {
    @Autowired
    private FishCardStatusService fishCardStatusService;
    @Autowired
    private RabbitMqDelaySender rabbitMqDelaySender;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 发现器:检测教师旷课生产者
     */
    public void teacherAbsentFinder() {
        logger.info("=================teacherAbsentFinder开始轮询课程开始情况,判断教师是否旷课");
        List<WorkOrder> workOrderList = fishCardStatusService.getCardsToStart();
        logger.info("@teacherAbsentFinder#teacher超过上课时间没有开始上课的数量:{}", workOrderList.size());
        TreeSet<FishCardDelayMessage> treeSet = getFishCardDelayMessages(workOrderList, FishCardDelayMsgType.TEACHER_ABSENT);
        Iterator<FishCardDelayMessage> iterator=treeSet.iterator();
        while (iterator.hasNext()){
            FishCardDelayMessage fishCardDelayMessage=iterator.next();
            MessageProperties messageProperties = fishCardStatusService.getMsgProperties(fishCardDelayMessage.getStartTime()
                    , FishCardDelayMsgType.TEACHER_ABSENT);
            logger.debug("@teacherAbsentFinder[检测教师旷课的教师先后顺序发送消息：{},id:[{}];分发期限[{}]",fishCardDelayMessage.getStartTime(),fishCardDelayMessage.getId(),messageProperties.getExpiration());
            rabbitMqDelaySender.send(fishCardDelayMessage,messageProperties);
        }
    }

    public void studentAbsentFinder() {
        logger.info("=================studentAbsentFinder开始轮询课程开始情况,判断学生是否旷课");
        List<WorkOrder> workOrderList = fishCardStatusService.getCardsWaitStudentAccepted();
        logger.info("超过上课时间没有学生没有接受上课的数量:{}", workOrderList.size());
        TreeSet<FishCardDelayMessage> treeSet = getFishCardDelayMessages(workOrderList,FishCardDelayMsgType.STUDENT_ABSENT);
        Iterator<FishCardDelayMessage> iterator=treeSet.iterator();
        while (iterator.hasNext()){
            FishCardDelayMessage fishCardDelayMessage=iterator.next();
            MessageProperties messageProperties = fishCardStatusService.getMsgProperties(fishCardDelayMessage.getStartTime()
                    , FishCardDelayMsgType.STUDENT_ABSENT);
            logger.debug("@studentAbsentFinder[检测学生旷课先后顺序发送消息：{},id:[{}];分发期限[{}]",fishCardDelayMessage.getStartTime(),fishCardDelayMessage.getId(),messageProperties.getExpiration());
            rabbitMqDelaySender.send(fishCardDelayMessage,messageProperties);
        }
    }

    public void teacherPrepareClassFinder(){
        logger.info("=================teacherPrepareClassFinder开始轮询准备上课的鱼卡，判断是否有需要通知上课的教师");
        List<WorkOrder> workOrderList=fishCardStatusService.getCardsTeacherPrepareClass();
        logger.info("距离上课时间即将上课的鱼卡数量[{}]", workOrderList.size(),workOrderList.size());
        if(CollectionUtils.isEmpty(workOrderList)){
            logger.info("没有检测到上课前需要通知的教师,返回");
            return;
        }
        TreeSet<FishCardDelayMessage> treeSet = getFishCardDelayMessages(workOrderList,FishCardDelayMsgType.NOTIFY_TEACHER_PREPARE_CLASS);
        Iterator<FishCardDelayMessage> iterator=treeSet.iterator();
        while (iterator.hasNext()){
            FishCardDelayMessage fishCardDelayMessage=iterator.next();
            MessageProperties messageProperties = fishCardStatusService.getMsgProperties(fishCardDelayMessage.getStartTime()
                    , FishCardDelayMsgType.NOTIFY_TEACHER_PREPARE_CLASS);
            logger.debug("@teacherPrepareClassFinder[检测通知上课的教师先后顺序发送消息：{},id:[{}];分发期限[{}]",fishCardDelayMessage.getStartTime(),fishCardDelayMessage.getId(),messageProperties.getExpiration());
            rabbitMqDelaySender.send(fishCardDelayMessage,messageProperties);
        }
    }

    /**
     * 1.处于正在上课的鱼卡,到时间没有完成,强制将其设置为服务端强制完成
     * 2.处于学生接收应答,就绪然后没有任何反应的鱼卡,将其设置为系统异常;
     */
    public void forceCompleteFinder() {
        logger.info("=================forceCompleteFinder开始轮询课程开始情况,判断是否应该强制标记课程完成");
        List<WorkOrder> workOrderList = fishCardStatusService.getCardsBeyondEndTime();
        logger.info("超过下课时间没有下课的数量:{}", workOrderList.size());
        TreeSet<FishCardDelayMessage> treeSet = getFishCardDelayMessages(workOrderList,FishCardDelayMsgType.FORCE_COMPLETE_SERVER);
        Iterator<FishCardDelayMessage> iterator=treeSet.iterator();
        while (iterator.hasNext()){
            FishCardDelayMessage fishCardDelayMessage=iterator.next();
            logger.info("@forceCompleteFinder[超时课程状态没更改,强制课程完成]按时间的先后顺序发送消息：[{}],id:[{}]",fishCardDelayMessage.getEndTime(),fishCardDelayMessage.getId());
            MessageProperties messageProperties = fishCardStatusService.getMsgProperties(fishCardDelayMessage.getEndTime()
                    , FishCardDelayMsgType.FORCE_COMPLETE_SERVER);
            logger.info("@forceCompleteFinder到期执行时间[{}ms]",messageProperties.getExpiration());
            rabbitMqDelaySender.send(fishCardDelayMessage,messageProperties);
        }
    }

    /**
     *TODO:检查没有补课的学生情况,检查到后,直接修改数据库
     */
    public void makeUpOutOfValidDay(){

    }

    private TreeSet<FishCardDelayMessage> getFishCardDelayMessages(List<WorkOrder> workOrderList,FishCardDelayMsgType fishCardDelayMsgType) {
        TreeSet<FishCardDelayMessage> treeSet=null;
        if(fishCardDelayMsgType.toString().equals(FishCardDelayMsgType.FORCE_COMPLETE_SERVER.toString())){
            logger.info("@getFishCardDelayMessages->使用endtime的比较器");
            treeSet=new TreeSet<>(new CompleteForceComparator());
        }
        else{
            logger.info("@getFishCardDelayMessages->使用starttime的比较器");
            treeSet=new TreeSet<>(new FishCardDelayMessage.StartTimeComparator());
        }
        for(WorkOrder workOrder:workOrderList) {
            FishCardDelayMessage fishCardDelayMessage = FishCardDelayMessage.newFishCardDelayMessage();
            fishCardDelayMessage.setId(workOrder.getId());
            fishCardDelayMessage.setStatus(workOrder.getStatus());
            fishCardDelayMessage.setType(fishCardDelayMsgType.value());
            fishCardDelayMessage.setStartTime(workOrder.getStartTime());
            fishCardDelayMessage.setEndTime(workOrder.getEndTime());
            treeSet.add(fishCardDelayMessage);
        }
        return treeSet;
    }
}
