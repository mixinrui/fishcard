package com.boxfishedu.workorder.servicex.graborder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.boxfishedu.card.bean.CourseTypeEnum;
import com.boxfishedu.online.order.entity.TeacherForm;
import com.boxfishedu.workorder.common.bean.MessagePushTypeEnum;
import com.boxfishedu.workorder.common.bean.TeachingOnlineListMsg;
import com.boxfishedu.workorder.common.bean.TeachingOnlineMsg;
import com.boxfishedu.workorder.common.bean.TeachingType;
import com.boxfishedu.workorder.common.redis.CacheKeyConstant;
import com.boxfishedu.workorder.common.util.WorkOrderConstant;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.entity.mysql.WorkOrderGrab;
import com.boxfishedu.workorder.entity.mysql.WorkOrderGrabHistory;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.base.BaseService;
import com.boxfishedu.workorder.service.graborder.MakeWorkOrderService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 生成可以抢单的鱼卡
 * Created by jiaozijun on 16/7/11.
 */
@Component
public class MakeWorkOrderServiceX {

    //本地异常日志记录对象
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MakeWorkOrderService makeWorkOrderService;

    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    @Autowired
    private CacheManager cacheManager;


    // send to redis
    //cacheManager.getCache(CacheKeyConstant.FISHCARD_BACK_ORDER_USERINFO).put(userName.trim(), json.toJSONString());


    /**
     * 组合发送的鱼卡
     */
    public void makeSendWorkOrder() {
        logger.info("::::::::::::::::::::::::::::::::begin::::::::::::makeSendWorkOrder::::::::::::::::::::::::::::::::");
        logger.info("开始定时轮训 生成需要匹配教师的鱼卡");


        // 1 获取未来两天内未匹配老师的鱼卡
        List<WorkOrder> workOrderNOteacher = makeWorkOrderService.findByTeacherIdAndStartTimeBetweenOrderByStartTime();
        if (null == workOrderNOteacher || workOrderNOteacher.size() < 1) {
            logger.info("1111111111111111111111:::::::::::::没有需要补老师的课程::::::::::::::::::::::::::::::::");
            return;
        }


        // 2 向师生运营获取教师信息列表
        List<TeacherForm> teacherForms = getTeacherList(foreighAndChinesTeahcer(workOrderNOteacher));

        if (null == teacherForms || teacherForms.size() < 0) {
            logger.info("222222222222222:::::::::::::thereisNOteacherList:没有查询到符合条件的教师列表:::::::::::::::::::::::::::::::");
            return;
        }

        // 3 获取未来两天内已经匹配老师的鱼卡
        List<WorkOrder> workOrderYESteacher = makeWorkOrderService.findWorkOrderContainTeachers();

        // 4 进行 老师鱼卡匹配
        Map<Long, List<WorkOrder>> map = Maps.newConcurrentMap();
        map = getTeacherWorkOrderList(map, workOrderNOteacher, workOrderYESteacher, teacherForms);

        logger.info("3333333333:::::::::::::::::::匹配_fishcard_map ,size=[{}]::::::::::::::::::::::::::::::::", map == null ? 0 : map.size());

        // 5 把缓存数据放入redis  把能够分配的鱼卡放在缓存中

        for (WorkOrder wo : workOrderNOteacher) {
            cacheManager.getCache(CacheKeyConstant.FISHCARD_WORKORDER_GRAB_KEY).put(wo.getId(), JSON.toJSONString(wo));
        }


        // 6 把缓存数据放入db中
        logger.info("4444444444:::::::::::::::向db中放入缓存数据");
        makeWorkOrderService.saveCurrentworkOrderMap(map);


        // 7 向在线运营发送老师数据(能够获取抢单的老师名单)
        logger.info("5555555555:::::::::::::向在线运营发送数据");
        pushTeacherList(map);

        logger.info("::::::::::::::::::::::::::::::::end-------makeSendWorkOrder::::::::::::::::::::::::::::::::");
        logger.info("结束定时轮训 生成需要匹配教师的鱼卡");
    }


    /**
     * 获取 获取查询教师列表 的条件
     *
     * @param workOrderNOteacher
     * @return
     */
    public String foreighAndChinesTeahcer(List<WorkOrder> workOrderNOteacher) {
        boolean chineseTeacherflag = false;
        boolean foreignTeahcerflag = false;
        for (WorkOrder wo : workOrderNOteacher) {
            if (CourseTypeEnum.TALK.equals(wo.getCourseType())) {
                foreignTeahcerflag = true;
            } else {
                chineseTeacherflag = true;
            }
            if (foreignTeahcerflag && chineseTeacherflag)
                break;
        }

        return chineseTeacherflag + "/" + foreignTeahcerflag;
    }


    /**
     * 过滤 获取最终发送老师鱼卡信息
     * 未来两天是否含有匹配课程的老师
     * 该老师在该时间片上 不能安排课程
     *
     * @param map
     * @param workOrderNOteacher
     * @param workOrderYESteacher
     * @param teacherForms
     * @return
     */
    public Map getTeacherWorkOrderList(Map map, List<WorkOrder> workOrderNOteacher, List<WorkOrder> workOrderYESteacher, List<TeacherForm> teacherForms) {

        boolean workOrderYESteacherFlag = true;
        if (null == workOrderYESteacher || workOrderYESteacher.size() < 1) {
            workOrderYESteacherFlag = false;
        }
        for (TeacherForm teacher : teacherForms) {

            if (workOrderYESteacherFlag) {
                map.put(teacher.getTeacherId(), getTeacherListByType(workOrderNOteacher, teacher.getTeacherType()));
            } else {
                List workOrder = Lists.newArrayList();
                for (WorkOrder wono : workOrderNOteacher) {
                    for (WorkOrder woyes : workOrderYESteacher) {
                        if (teacher.getTeacherId() != woyes.getTeacherId() && !wono.getStartTime().equals(woyes.getStartTime())) {
                            workOrder.add(wono);
                        }
                    }
                }

                if (workOrder.size() > 0) {
                    map.put(teacher.getTeacherId(), getTeacherListByType(workOrder, teacher.getTeacherType()));
                }
            }

        }

        return map;
    }

    /**
     * 根据教师类型  返回相应的 鱼卡列表
     *
     * @param workOrderNOteacher
     * @param type
     * @return
     */
    public List<WorkOrder> getTeacherListByType(List<WorkOrder> workOrderNOteacher, int type) {
        List<WorkOrder> list = Lists.newArrayList();
        for (WorkOrder wo : workOrderNOteacher) {
            if (TeachingType.WAIJIAO.getCode() == type && CourseTypeEnum.TALK.equals(wo.getCourseType())) {
                list.add(wo);
            }

            if (TeachingType.ZHONGJIAO.getCode() == type && !CourseTypeEnum.TALK.equals(wo.getCourseType())) {
                list.add(wo);
            }
        }

        return list;
    }

    /**
     * 获取教师信息列表
     * TODO:reload()
     *
     * @return
     */
    public List getTeacherList(String parameter) {
        List<TeacherForm> teacherListFromTeach = Lists.newArrayList();

        TeacherForm tf1 = new TeacherForm();//IOS
        tf1.setTeacherId(1298937L);
        tf1.setTeacherType(1);

        TeacherForm tf2 = new TeacherForm();//安卓
        tf2.setTeacherId(1298904L);
        tf2.setTeacherType(1);

        teacherListFromTeach.add(tf1);

        teacherListFromTeach.add(tf2);

        return teacherListFromTeach;

//
//        // 获取教师列表  TeacherForm
//        logger.info("=================>开始调用师生运营获取教师列表");
//        List teacherList = teacherStudentRequester.pullTeacherListMsg(parameter);
//        logger.info("=================>开始调用师生运营获取教师列表");
//        if(null == teacherList || teacherList.size() <1){
//            return null;
//        }
//
//        for(Object o : teacherList){
//            TeacherForm tf = new TeacherForm();
//            Map m = (Map)o;
//            tf.setTeacherId(Long.parseLong( String.valueOf( m.get("teacherId"))));
//            tf.setTeacherType((Integer) m.get("teachingType"));
//            teacherListFromTeach.add(tf);
//        }
//        return teacherListFromTeach;
    }


    /**
     * 向在线教学发送能够抢单的教师列表
     *
     * @param map
     */
    public void pushTeacherList(Map<Long, List<WorkOrder>> map) {
        List list = Lists.newArrayList();
        for (Long key : map.keySet()) {
            Map map1 = Maps.newHashMap();
            map1.put("user_id", key);
            map1.put("push_title", WorkOrderConstant.SEND_GRAB_ORDER_MESSAGE);

            JSONObject jo = new JSONObject();
            jo.put("type", MessagePushTypeEnum.SEND_GRAB_ORDER_TYPE.toString());
            jo.put("count", null == map.get(key) ? "0" : map.get(key).size());
            map1.put("data", jo);

            list.add(map1);
        }
        teacherStudentRequester.pushTeacherListOnlineMsg(list);
    }


    /**
     * 每天 17:40 清理前一天的数据进历史表
     */
    @Transactional
    public void clearGrabData() {

        List<WorkOrderGrab> workOrderGrabList = makeWorkOrderService.getGrabDataBeforeDay();
        if (null != workOrderGrabList && workOrderGrabList.size() > 0) {
            // 1 组装 抢单鱼卡数据 到 历史数据中
            List<WorkOrderGrabHistory> workOrderGrabHistoryList = Lists.newArrayList();
            for (WorkOrderGrab wg : workOrderGrabList) {
                WorkOrderGrabHistory wgh = new WorkOrderGrabHistory();
                wgh.setWorkorderId(wg.getWorkorderId());
                wgh.setTeacherId(wg.getTeacherId());
                wgh.setUpdateTime(wg.getUpdateTime());
                wgh.setCreateTime(wg.getCreateTime());
                wgh.setStartTime(wg.getStartTime());
                wgh.setRealCreateTime(new Date());
                workOrderGrabHistoryList.add(wgh);
            }

            // 2 删除数据
            makeWorkOrderService.deleteGrabData(workOrderGrabList);
            // 3 新增数据
            makeWorkOrderService.initGrabOrderHistory(workOrderGrabHistoryList);
        }
    }

}
