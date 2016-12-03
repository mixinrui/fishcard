package com.boxfishedu.workorder.servicex.coursenotify;

import com.alibaba.fastjson.JSONObject;
import com.boxfishedu.workorder.common.bean.QueueTypeEnum;
import com.boxfishedu.workorder.common.rabbitmq.RabbitMqSender;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.ShortMessageCodeConstant;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 关于节假日通知学生 换时间功能
 * Created by jiaozijun on 16/12/2.
 */
@Component
public class CourseChangeTimeNotifySerceX {

    //本地异常日志记录对象
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private ThreadPoolManager threadPoolManager;

    @Autowired
    private RabbitMqSender rabbitMqSender;


    /**
     * 短信通知学生换时间
     */
    public void notiFyStudentChangeTime() {

        logger.info("notiFyStudentChangeTime--->开始检索老师今天有课的记录");
        Map<Long,List<WorkOrder>> studentNotifyMap  = workOrderService.getNotifyMessage();
        if (CollectionUtils.isEmpty(studentNotifyMap)) {
            logger.info("notiFyStudentChangeTime--->没有提醒的课程");
            return;
        }

        try {

            logger.info("notiFyStudentChangeTime:::::::匹配_studentNotifyMap ,size=[{}]::::::::::::::::::::::::::::::::", studentNotifyMap == null ? 0 : studentNotifyMap.size());
            logger.info("notiFyStudentChangeTime:::::::::studentNotifyMap [{}]::::::::::::::::::::::::::::::::", studentNotifyMap);

        } catch (Exception e) {
            logger.error("lazyLoadError");
            e.printStackTrace();
        }


        /**  begin  发送短信 **/

        for(Long studentId:studentNotifyMap.keySet()){
            sendShortMessage(studentNotifyMap.get(studentId));
        }
        /**  end    发送短信 **/

        logger.info("studentNotifyMap:::::通知完成");

    }



    /**
     * 给学生发送短信
     *
     * @param myClass  提醒学生换时间
     */
    private void sendShortMessage( List<WorkOrder> myClass) {

        Object messageStuct = getMessageStu(myClass.get(0).getStudentId(),  myClass);


            threadPoolManager.execute(new Thread(() -> {
                // 发送短信 向短信队列发送q消息
                    rabbitMqSender.send(messageStuct, QueueTypeEnum.SHORT_MESSAGE);
            }));

    }

    /**
     * 学生消息体
     *
     * @param userId
     * @param list
     * @return
     */
    private Object getMessageStu(Long userId, List<WorkOrder> list) {
        Map map = Maps.newHashMap();
        map.put("user_id", userId);
        map.put("template_code", ShortMessageCodeConstant.SMS_STU_NOTITY_CHANGE_CODE);

        JSONObject jo = new JSONObject();
        jo.put("quantity", String.valueOf(list.size()));

        StringBuffer startTime = new StringBuffer("");
        list.forEach(workOrder -> {
            startTime.append(DateUtil.date2ShortString(workOrder.getStartTime())).append(",");
        });

        jo.put("startTime", startTime.substring(0, startTime.length() - 1));




        map.put("data", jo.toJSONString());
        return map;

    }


    public static void main(String[] args) {
        String [] array = {"32","你好","特殊"};
        String s = StringUtils.arrayToDelimitedString(array,",");

        System.out.println(s);
    }

}
