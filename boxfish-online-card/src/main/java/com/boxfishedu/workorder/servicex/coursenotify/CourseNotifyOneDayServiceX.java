package com.boxfishedu.workorder.servicex.coursenotify;

import com.alibaba.fastjson.JSONArray;
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
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.*;

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


    /**
     * 通知明天学生有课
     */
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
        sendShortMessage(studentHasClassMap, "1");
        /**  end    发送短信 **/

        //开始发送通知
        pushTeacherList(studentHasClassMap);


        logger.info("notiFyStudentClass:::::通知完成");

    }

    private Map<Long, List<WorkOrder>> getStudentClassess(Map<Long, List<WorkOrder>> studentHasClassMap, List<WorkOrder> listWorkOrders) {

        listWorkOrders.forEach(workOrder -> {

            List<WorkOrder> listWorkOrder = studentHasClassMap.get(workOrder.getStudentId());
            if (null != listWorkOrder) {
                studentHasClassMap.get(workOrder.getStudentId()).add(workOrder);
            } else {
                listWorkOrder = Lists.newArrayList();
                listWorkOrder.add(workOrder);
                studentHasClassMap.put(workOrder.getStudentId(), listWorkOrder);
            }
        });
        return studentHasClassMap;
    }


    public void pushTeacherList(Map<Long, List<WorkOrder>> map) {
        logger.info("notiFyStudentClass::begin");

        for (Long key : map.keySet()) {
            JSONObject  jsonObject = new JSONObject();
            JSONObject  jsonObjectData = new JSONObject();
            JSONArray jsonArray = new JSONArray();

            String pushTitle = WorkOrderConstant.SEND_STU_CLASS_TOMO_MESSAGE_BEGIN;
            Integer count = (null == map.get(key) ? 0 : map.get(key).size());

            jsonArray.add(key);

            jsonObject.put("user_id", jsonArray);

            if (null == map.get(key)) {
                continue;
            }
            pushTitle = (pushTitle + count + WorkOrderConstant.SEND_STU_CLASS_TOMO_MESSAGE_END);
            jsonObject.put("push_title", pushTitle);

            jsonObjectData.put("type", MessagePushTypeEnum.SEND_STUDENT_CLASS_TOMO_TYPE.toString());
            jsonObjectData.put("count", count);
            jsonObjectData.put("push_title", pushTitle);
            jsonObjectData.put("user_id", key);

            jsonObject.put("data", jsonObjectData);
            teacherStudentRequester.pushTeacherListOnlineMsgnew(jsonObject);
        }

        logger.info("notiFyStudentClass::end");
    }


    /**
     * 给学生发送短信
     *
     * @param classMap
     * @param type     1 给学生发短信
     *                 2 给老师发送短信
     */
    private void sendShortMessage(Map<Long, List<WorkOrder>> classMap, String type) {
        for (Long key : classMap.keySet()) {
            threadPoolManager.execute(new Thread(() -> {
                // 发送短信 向短信队列发送q消息
                List<WorkOrder> list = classMap.get(key);
                if (null != list) {
                    Object o = null;
                    if ("1".equals(type)) {
                        o = getMessageStu(key, list);
                    } else if ("2".equals(type)) {
                        o = getMessageteacher(key, list);
                    }

                    rabbitMqSender.send(o, QueueTypeEnum.SHORT_MESSAGE);
                }
            }));
        }

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
        map.put("template_code", ShortMessageCodeConstant.SMS_STU_NOTITY_TOMO_CODE);

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


    /**
     * 老师消息体
     *
     * @param userId
     * @param list
     * @return
     */
    private Object getMessageteacher(Long userId, List<WorkOrder> list) {
        Map map = Maps.newHashMap();
        map.put("user_id", userId);
        map.put("template_code", ShortMessageCodeConstant.SMS_TEA_NOTITY_CLASS_TODY_CODE);

        JSONObject jo = new JSONObject();
        jo.put("quantity", String.valueOf(list.size()));
        jo.put("startTime", CollectionUtils.isEmpty(list) ? "" : DateUtil.date2ShortString(list.get(0).getStartTime()));
        try {
            logger.info(":::getMessageteacher::fishcardId [{}]::startTime [{}]", list.get(0).getId(), list.get(0).getStartTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        map.put("data", jo.toJSONString());
        return map;

    }


    private void splitByTwoThousandsMessage(List list) {
        int count = list.size() / base_count;
        int yushu = list.size() % base_count;

        logger.info("splitByTwoThousandsMessage::count=[{}] ::yushu[{}]", count, yushu);

        for (int i = 0; i < count; i++) {
            List list1 = list.subList(i * base_count, i * base_count + base_count);
            //System.out.println(list1.get(0)+"-----"+list1.get(list1.size()-1));
            teacherStudentRequester.pushTeacherListOnlineMsg(list.subList(i * base_count, i * base_count + base_count));

            logger.info("splitByTwoThousandsMessage:[{}] :begin:[{}]:end:[{}]",i,i * base_count,i * base_count + base_count);
        }
        List list2 = list.subList(count * base_count, count * base_count + yushu);

        //System.out.println(list2.get(0)+"-----"+list2.get(list2.size()-1));

        teacherStudentRequester.pushTeacherListOnlineMsg(list.subList(count * base_count, count * base_count + yushu ));
        logger.info("splitByTwoThousandsMessage: last :begin:[{}]:end:[{}]",count * base_count,count * base_count + yushu);

    }


    public static void main(String[] args) {
        CourseNotifyOneDayServiceX t = new CourseNotifyOneDayServiceX();

        List list = Lists.newArrayList();

        for (int i = 0; i < 5; i++) {
            list.add(i);
        }

        t.splitByTwoThousandsMessage(list);
    }


    /**
     * 通知今天老师有课
     */
    public void notiFyTeacherClass() {

        logger.info("notiFyTeacherClass--->开始检索老师今天有课的记录");
        List<WorkOrder> listWorkOrders = fishCardStatusService.getCardsTeacherNotifyClass();
        if (null == listWorkOrders || listWorkOrders.isEmpty()) {
            logger.info("notiFyTeacherClass--->没有今天的课");
            return;
        }


        Map<Long, List<WorkOrder>> teacherHasClassMap = Maps.newHashMap();


        teacherHasClassMap = getTeacherClassess(teacherHasClassMap, listWorkOrders);
        try {

            logger.info("notiFyStudentClass:::::::匹配_fishcard_map ,size=[{}]::::::::::::::::::::::::::::::::", teacherHasClassMap == null ? 0 : teacherHasClassMap.size());
            logger.info("notiFyStudentClass:::::::::sendToStudentInfo [{}]::::::::::::::::::::::::::::::::", teacherHasClassMap);

        } catch (Exception e) {
            logger.error("lazyLoadError");
            e.printStackTrace();
        }

        if (null == teacherHasClassMap || teacherHasClassMap.isEmpty()) {
            logger.info("notiFyTeacherClass:::::::::::MapIsNull");
            return;
        }

        /**  begin  发送短信 **/
        sendShortMessage(teacherHasClassMap, "2");
        /**  end    发送短信 **/

        logger.info("notiFyTeacherClass:::::通知完成");

    }


    private Map<Long, List<WorkOrder>> getTeacherClassess(Map<Long, List<WorkOrder>> teacherHasClassMap, List<WorkOrder> listWorkOrders) {

        listWorkOrders.forEach(workOrder -> {

            List<WorkOrder> listWorkOrder = teacherHasClassMap.get(workOrder.getTeacherId());
            if (null != listWorkOrder) {
                teacherHasClassMap.get(workOrder.getTeacherId()).add(workOrder);
            } else {
                listWorkOrder = Lists.newLinkedList();
                final Function<WorkOrder, Date> byName = wo -> wo.getStartTime();
                listWorkOrder.add(workOrder);
                teacherHasClassMap.put(workOrder.getTeacherId(), listWorkOrder);
            }
        });

        // 按照开始时间排序
        if (!teacherHasClassMap.isEmpty()) {
            teacherHasClassMap.forEach((id, wolist) -> {
                if (null != wolist && wolist.size() > 0) {
                    wolist.sort(new Comparator<WorkOrder>() {
                        @Override
                        public int compare(WorkOrder o1, WorkOrder o2) {
                            if (o1.getStartTime().after(o2.getStartTime())) {
                                return 0;
                            }
                            return -1;
                        }
                    });
                }
            });
        }

        return teacherHasClassMap;
    }

}
