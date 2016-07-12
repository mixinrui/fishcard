package com.boxfishedu.workorder.servicex.graborder;

import com.alibaba.fastjson.JSONObject;
import com.boxfishedu.card.bean.CourseTypeEnum;
import com.boxfishedu.online.order.entity.TeacherForm;
import com.boxfishedu.workorder.common.bean.TeachingOnlineListMsg;
import com.boxfishedu.workorder.common.bean.TeachingOnlineMsg;
import com.boxfishedu.workorder.common.redis.CacheKeyConstant;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.base.BaseService;
import com.boxfishedu.workorder.service.graborder.MakeWorkOrderService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 生成可以抢单的鱼卡
 * Created by jiaozijun on 16/7/11.
 */
@Component
public class MakeWorkOrderServiceX{

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
    public void makeSendWorkOrder(){
        logger.info("开始定时轮训 生成需要匹配教师的鱼卡");


        // 1 获取未来两天内未匹配老师的鱼卡
            List<WorkOrder> workOrderNOteacher = makeWorkOrderService.findByTeacherIdAndStartTimeBetweenOrderByStartTime();
            if(null == workOrderNOteacher || workOrderNOteacher.size() < 1){
                logger.info("没有需要补老师的课程");
                return;
            }


        // 2 向师生运营获取教师信息列表
            List<TeacherForm> teacherForms  =  getTeacherList(foreighAndChinesTeahcer(workOrderNOteacher));

            if(null == teacherForms || teacherForms.size() <0){
                logger.info("没有查询到符合条件的教师列表");
                return;
            }
        // 3 获取未来两天内已经匹配老师的鱼卡
            List<WorkOrder> workOrderYESteacher = makeWorkOrderService.findWorkOrderContainTeachers();

        // 4 进行 老师鱼卡匹配
            Map<Long ,List<WorkOrder>> map = Maps.newConcurrentMap();
            map = getTeacherWorkOrderList(map,workOrderNOteacher,workOrderYESteacher,teacherForms);


        // 5 把缓存数据放入redis

           for(Long key:map.keySet()){
               cacheManager.getCache(CacheKeyConstant.FISHCARD_WORKORDER_GRAB_KEY).put(key, map.get(key));
           }


        // 6 把缓存数据放入db中
        makeWorkOrderService.saveCurrentworkOrderMap(map);


        // 7 向在线运营发送老师数据(能够获取抢单的老师名单)
        pushTeacherList(map);



        logger.info("结束定时轮训 生成需要匹配教师的鱼卡");
    }

    /**
     * 获取 获取查询教师列表 的条件
     * @param workOrderNOteacher
     * @return
     */
    public String foreighAndChinesTeahcer( List<WorkOrder> workOrderNOteacher){
        int chineseTeacherNum = 0;
        int foreignTeahcerNum = 0;
        for (WorkOrder wo: workOrderNOteacher){
            if(CourseTypeEnum.TALK.equals(wo.getCourseType())){
                foreignTeahcerNum+=1;
            }else {
                chineseTeacherNum+=1;
            }
            if(chineseTeacherNum>0 && foreignTeahcerNum >0)
                break;
        }
        JSONObject json =new JSONObject();
        json.put("chineseTeacherNum",chineseTeacherNum);
        json.put("foreignTeahcerNum",foreignTeahcerNum);
        return json.toJSONString();
    }


    /**
     * 过滤 获取最终发送老师鱼卡信息
     * @param map
     * @param workOrderNOteacher
     * @param workOrderYESteacher
     * @param teacherForms
     * @return
     */
    public Map getTeacherWorkOrderList (Map map,List<WorkOrder> workOrderNOteacher,List<WorkOrder> workOrderYESteacher,List<TeacherForm> teacherForms){
        boolean workOrderYESteacherFlag = true;
        if(null ==workOrderYESteacher || workOrderYESteacher.size() <1){
            workOrderYESteacherFlag = false;
        }
        for(TeacherForm teacher :teacherForms){

            if(workOrderYESteacherFlag){
                map.put(teacher.getTeacherId(),workOrderNOteacher);
            }else {
                List workOrder = Lists.newArrayList();
                for(WorkOrder wono:workOrderNOteacher){
                    for(WorkOrder woyes:workOrderYESteacher){
                        if(teacher.getTeacherId() != woyes.getTeacherId()  && !wono.getStartTime().equals(woyes.getStartTime())){
                            workOrder.add(wono) ;
                        }
                    }
                }

                if(workOrder.size()>0){
                    map.put(teacher.getTeacherId(),workOrder);
                }
            }

        }

        return map;
    }

    /**
     * 获取教师信息列表
     * @return
     */
    public List getTeacherList(String parameter){
        // 获取教师列表
        teacherStudentRequester.pullTeacherListMsg(parameter);
        return null;
    }


    /**
     * 向在线教学发送能够抢单的教师列表
     * @param map
     */
    public void pushTeacherList(Map<Long ,List<WorkOrder>>  map){
        TeachingOnlineListMsg  teacherMsg = new TeachingOnlineListMsg();
        for(Long key :map.keySet()){
            TeachingOnlineMsg tMsg = new TeachingOnlineMsg();
            tMsg.setUser_id(key);
            tMsg.setPush_title("有学生等待上课,打开app抢先上课");
            teacherMsg.getTeachingOnlineMsg().add(tMsg);
        }
        teacherStudentRequester.pushTeacherListOnlineMsg(teacherMsg);
    }

}
