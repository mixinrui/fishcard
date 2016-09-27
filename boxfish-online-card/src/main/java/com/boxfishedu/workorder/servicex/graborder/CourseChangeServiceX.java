package com.boxfishedu.workorder.servicex.graborder;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.LongArraySerializer;
import com.boxfishedu.card.bean.CourseTypeEnum;
import com.boxfishedu.workorder.common.bean.MessagePushTypeEnum;
import com.boxfishedu.workorder.common.bean.TeachingType;
import com.boxfishedu.workorder.common.util.WorkOrderConstant;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.FishCardStatusService;
import com.boxfishedu.workorder.service.graborder.MakeWorkOrderService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 用于师生互评发送消息(换课消息推送)
 * Created by jiaozijun on 16/8/3.
 */
@Component
public class CourseChangeServiceX {

    //本地异常日志记录对象
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    @Autowired
    private FishCardStatusService fishCardStatusService;


    @Autowired
    private MakeWorkOrderService makeWorkOrderService;

    /**
     * 发送变更课程的鱼卡信息(通知教师端)
     */
    public void sendCourseChangeWorkOrders(){

        logger.info("sendCourseChangeWorkOrders:");
        // 获取 每天18点  到 后天 24点 (startTime) 的鱼卡信息
        List<WorkOrder> workOrders = makeWorkOrderService.findByTeacherIdGreaterThanAndStatusAndUpdateTimeChangeCourseBetween();
        if(null==workOrders  ||  workOrders.size()<1){
            logger.info("sendCourseChangeWorkOrders:::11111:::没有变更课程的鱼卡信息");
            return;
        }


        //每个老师变更的课程
        Map<Long, List<WorkOrder>> map = Maps.newHashMap();

        //组装map
        makeOrderMap(map,workOrders);

        if(map==null || map.size()<1){
            logger.info("sendCourseChangeWorkOrders::::没有组装map成功");
            return;
        }




        // 推送消息
        logger.info("sendCourseChangeWorkOrders:::sendMessage推送消息");
        pushTeacherList(map);

        logger.info("sendCourseChangeWorkOrders::::changeSendStatus变更状态为已经发送状态");
        changeWorkOrderUpdatetime(workOrders);
    }


    private void makeOrderMap(Map map,List<WorkOrder> workOrders){
        workOrders.forEach(workOrder -> {
            if(null!=map.get(workOrder.getTeacherId())){
                ((List<WorkOrder>) map.get(workOrder.getTeacherId() )).add(workOrder);
            }else {
                List<WorkOrder> list = Lists.newArrayList();
                list.add(workOrder);
                map.put(workOrder.getTeacherId(),list);
            }
        });
    }





    @Transactional
    private void changeWorkOrderUpdatetime(List<WorkOrder> workOrders) {
        workOrders.forEach(
                workOrder -> {
                    workOrder.setUpdateTime(new Date());
                    workOrder.setSendflagcc("0");//发送成功
                }

        );
        fishCardStatusService.save(workOrders);

    }


    /**
     * 向在线教学发送能够抢单的教师列表
     *
     * @param map
     */
    private void pushTeacherList(Map<Long, List<WorkOrder>> map) {
        List list = Lists.newArrayList();
        for (Long key : map.keySet()) {
            String pushTitle = "";
            String pushTitle_bein = WorkOrderConstant.SEND_CHANGE_COURSE_MESSAGE_BEGIN;
            String pushTitle_end = WorkOrderConstant.SEND_CHANGE_COURSE_MESSAGE_END;
            Map map1 = Maps.newHashMap();
            map1.put("user_id", key);

            if (null == map.get(key)) {
                continue;
            }

            WorkOrder workOrder = map.get(key).get(0);
            if (null == workOrder) {
                continue;
            }

            if (TeachingType.WAIJIAO.getCode() == workOrder.getSkuId() ) {
                pushTitle_bein = WorkOrderConstant.SEND_CHANGE_COURSE_MESSAGE_FOREIGN_BEGIN;
                pushTitle_end = WorkOrderConstant.SEND_CHANGE_COURSE_MESSAGE_FOREIGN_END;
            }
            pushTitle = pushTitle_bein + map.get(key).size() + pushTitle_end;

            map1.put("push_title", pushTitle);//您有N节变更的课

            JSONObject jo = new JSONObject();
            jo.put("type", MessagePushTypeEnum.SEND_TEASTU_ASSESS_TYPE.toString());
            jo.put("count", null == map.get(key) ? "0" : map.get(key).size());
            logger.info(":::::::sendToTecherContent::::pushTitle:[{}]:size[{}]", pushTitle, map.get(key).size());
            map1.put("data", jo);

            list.add(map1);
        }
        teacherStudentRequester.pushTeacherListOnlineMsg(list);
    }
}
