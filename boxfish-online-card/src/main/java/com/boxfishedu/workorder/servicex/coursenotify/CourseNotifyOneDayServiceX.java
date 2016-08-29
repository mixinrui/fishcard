package com.boxfishedu.workorder.servicex.coursenotify;

import com.alibaba.fastjson.JSONObject;
import com.boxfishedu.card.bean.CourseTypeEnum;
import com.boxfishedu.workorder.common.bean.MessagePushTypeEnum;
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


    public void notiFyStudentClass() {

        logger.info("notiFyStudentClass--->开始检索明天有课的学生准备上课");
        List<WorkOrder> listWorkOrders = fishCardStatusService.getCardsStudentNotifyClass();
        if (null == listWorkOrders || listWorkOrders.isEmpty()) {
            logger.info("notiFyStudentClass--->没有明天要上课的学生");
            return;
        }


        Map<Long, Integer> studentHasClassMap = Maps.newHashMap();


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

        //开始发送通知
        pushTeacherList(studentHasClassMap);


        logger.info("notiFyStudentClass:::::通知完成");

    }

    private Map<Long, Integer> getStudentClassess(Map<Long, Integer> studentHasClassMap, List<WorkOrder> listWorkOrders) {

        listWorkOrders.forEach(workOrder -> {
            Integer account = studentHasClassMap.get(workOrder.getStudentId());
            if (null != account) {
                account += 1;
                studentHasClassMap.put(workOrder.getStudentId(), account);
            } else {
                studentHasClassMap.put(workOrder.getStudentId(), new Integer(1));
            }
        });
        return studentHasClassMap;
    }


    public void pushTeacherList(Map<Long, Integer> map) {
        logger.info("notiFyStudentClass::begin");
        List list = Lists.newArrayList();
        for (Long key : map.keySet()) {
            String pushTitle = WorkOrderConstant.SEND_STU_CLASS_TOMO_MESSAGE_BEGIN;
            Integer count = (null == map.get(key) ? 0 : map.get(key));
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
