package com.boxfishedu.workorder.servicex.coursenotify;

import com.alibaba.fastjson.JSONObject;
import com.boxfishedu.card.bean.CourseTypeEnum;
import com.boxfishedu.workorder.common.bean.MessagePushTypeEnum;
import com.boxfishedu.workorder.common.bean.QueueTypeEnum;
import com.boxfishedu.workorder.common.rabbitmq.RabbitMqSender;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.ShortMessageCodeConstant;
import com.boxfishedu.workorder.common.util.WorkOrderConstant;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.FishCardStatusService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 提前一天 通知明天学生上课消息
 * Created by jiaozijun on 16/8/23.
 */
@Component
public class CourseNotifyOneDayServiceX {

    //本地异常日志记录对象
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final int base_count = 2000;

    @Autowired
    private FishCardStatusService fishCardStatusService;


    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    @Autowired
    private ThreadPoolManager threadPoolManager;

    @Autowired
    private RabbitMqSender rabbitMqSender;

    public void notiFyStudentClass() {

        logger.info("notiFyStudentClass--->开始检索明天有课的学生准备上课");
        List<WorkOrder> listWorkOrders = fishCardStatusService.getCardsStudentNotifyClass();
        if (null == listWorkOrders || listWorkOrders.isEmpty()) {
            logger.info("notiFyStudentClass--->没有明天要上课的学生");
            return;
        }


        Map<Long, List<WorkOrder>> studentHasClassMap = Maps.newHashMap();


        studentHasClassMap = getStudentClassess(studentHasClassMap, listWorkOrders);
        try {

            logger.info("notiFyStudentClass:::::::匹配_fishcard_map ,size=[{}]::::::::::::::::::::::::::::::::", studentHasClassMap == null ? 0 : studentHasClassMap.size());
            logger.info("notiFyStudentClass:::::::::sendToStudentInfo [{}]::::::::::::::::::::::::::::::::", studentHasClassMap);

        } catch (Exception e) {
            logger.error("lazyLoadError");
            e.printStackTrace();
        }

        if (null == studentHasClassMap || studentHasClassMap.isEmpty()) {
            logger.info("notiFyStudentClass:::::::::::MapIsNull");
            return;
        }

        /**  begin  发送短信 **/
        sendShortMessage(studentHasClassMap);
        /**  end    发送短信 **/

        //开始发送通知
        pushTeacherList(studentHasClassMap);


        logger.info("notiFyStudentClass:::::通知完成");

    }

    private Map<Long, List<WorkOrder>> getStudentClassess(Map<Long, List<WorkOrder>> studentHasClassMap, List<WorkOrder> listWorkOrders) {

        listWorkOrders.forEach(workOrder -> {

            List<WorkOrder>  listWorkOrder= studentHasClassMap.get(workOrder.getStudentId());
            if (null != listWorkOrder) {
                studentHasClassMap.get(workOrder.getStudentId()).add(workOrder);
            } else {
                listWorkOrder = Lists.newArrayList();
                listWorkOrder.add(workOrder);
                studentHasClassMap.put(workOrder.getStudentId(),listWorkOrder );
            }
        });
        return studentHasClassMap;
    }


    public void pushTeacherList(Map<Long, List<WorkOrder>> map) {
        logger.info("notiFyStudentClass::begin");
        List list = Lists.newArrayList();
        for (Long key : map.keySet()) {
            String pushTitle = WorkOrderConstant.SEND_STU_CLASS_TOMO_MESSAGE_BEGIN;
            Integer count = (null == map.get(key) ? 0 : map.get(key).size());
            Map map1 = Maps.newHashMap();
            map1.put("user_id", key);

            if (null == map.get(key)) {
                continue;
            }
            pushTitle = (pushTitle + count + WorkOrderConstant.SEND_STU_CLASS_TOMO_MESSAGE_END);
            map1.put("push_title", pushTitle);

            JSONObject jo = new JSONObject();
            jo.put("type", MessagePushTypeEnum.SEND_STUDENT_CLASS_TOMO_TYPE.toString());
            jo.put("count", count);
            jo.put("push_title", pushTitle);

//            try{
//                logger.info(":::::::sendToStudentContent::::pushTitle:[{}]:size[{}]",pushTitle,map.get(key));
//            }catch (Exception e){
//                logger.error("::::::::dataError::::::::");
//            }

            map1.put("data", jo);

            list.add(map1);
        }
        if (!list.isEmpty()) {

            // 2000 分组
            if (list.size() > 2000) {
                teacherStudentRequester.pushTeacherListOnlineMsg(list);
            } else {
                teacherStudentRequester.pushTeacherListOnlineMsg(list);
            }
        }

        logger.info("notiFyStudentClass::end");
    }


    /**
     * 发送短信
     * @param studentHasClassMap
     */
    private void sendShortMessage(Map<Long,List<WorkOrder>> studentHasClassMap){
        for(Long key :studentHasClassMap.keySet()){
            threadPoolManager.execute(new Thread(() -> {
                // 发送短信 向短信队列发送q消息
                List<WorkOrder> list = studentHasClassMap.get(key);
                if(null!=list){
                    Object o = getMessage(key,list);
                    rabbitMqSender.send(o, QueueTypeEnum.SHORT_MESSAGE);
                }
            }));
        }

    }

   private Object getMessage(Long userId,List<WorkOrder> list){
       Map map = Maps.newHashMap();
       map.put("user_id",userId);
       map.put("template_code", ShortMessageCodeConstant.SMS_STU_NOTITY_TOMO_CODE);

       JSONObject jo = new JSONObject();
       jo.put("quantity", list.size());

       StringBuffer startTime = new StringBuffer("");
       list.forEach(workOrder -> {
           startTime.append(DateUtil.date2ShortString(workOrder.getStartTime())).append(",");
       });

       jo.put("startTime", startTime.substring(0,startTime.length()-1));

       map.put("data",jo);
       return map;

   }


    private void splitByTwoThousandsMessage(List list) {
        int count = list.size() / base_count;
        int yushu = list.size() % base_count;

        logger.info("splitByTwoThousandsMessage::count=[{}] ::yushu[{}]", count, yushu);

        for (int i = 0; i < count; i++) {
            List list1 = list.subList(i * base_count, i * base_count + base_count);
            //System.out.println(list1.get(0)+"-----"+list1.get(list1.size()-1));
            teacherStudentRequester.pushTeacherListOnlineMsg(list.subList(i * base_count, i * base_count + base_count - 1));
            logger.info("splitByTwoThousandsMessage:[{}] :begin:[{}]:end:[{}]",i,i * base_count,i * base_count + base_count - 1);
        }
        List list2 = list.subList(count * base_count, count * base_count + yushu);

        //System.out.println(list2.get(0)+"-----"+list2.get(list2.size()-1));
        teacherStudentRequester.pushTeacherListOnlineMsg(list.subList(count * base_count, count * base_count + yushu - 1));
        logger.info("splitByTwoThousandsMessage: last :begin:[{}]:end:[{}]",count * base_count,count * base_count + yushu - 1);

    }


    public static void main(String[] args) {
        CourseNotifyOneDayServiceX t = new CourseNotifyOneDayServiceX();

        List list = Lists.newArrayList();

        for (int i = 0; i < 5; i++) {
            list.add(i);
        }

        t.splitByTwoThousandsMessage(list);
    }

}
