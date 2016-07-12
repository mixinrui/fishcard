package com.boxfishedu.workorder.servicex.graborder;

import com.alibaba.fastjson.JSONObject;
import com.boxfishedu.workorder.common.redis.CacheKeyConstant;
import com.boxfishedu.workorder.common.util.WorkOrderConstant;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.entity.mysql.WorkOrderGrab;
import com.boxfishedu.workorder.service.graborder.GrabOrderService;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;

import java.util.Iterator;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;

/**
 * Created by mk on 16/7/12.
 */
public class GrabOrderServiceX {

    //本地异常日志记录对象
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private GrabOrderService grabOrderService;

    public JsonResultModel getWorkOrderListByTeacherId(Long teacherId){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msg", WorkOrderConstant.FISHCARD_LIST);
        jsonObject.put("code",0);
        List<WorkOrder> listWorkOrder = Lists.newArrayList();
        List<WorkOrderGrab> listWorkOrderGrab = Lists.newArrayList();
        listWorkOrder = cacheManager.getCache(CacheKeyConstant.FISHCARD_WORKORDER_GRAB_KEY).get(teacherId, List.class);
        if(listWorkOrder!=null&&listWorkOrder.size()>0){
            logger.info("::::::::::::::::::根据teacherId从Redis中获取可抢课程列表::::::::::::::::::");
            listWorkOrder = filterListByStartTime(listWorkOrder);
            jsonObject.put("workorderlist",listWorkOrder);
        }else{
            WorkOrderGrab workOrderGrab = new WorkOrderGrab();
            workOrderGrab.setTeacherId(teacherId);
            workOrderGrab.setFlag("0");
            listWorkOrderGrab = grabOrderService.findByTeacherIdAndFlagAndStartTimeGreaterThan(workOrderGrab);
            if(listWorkOrderGrab!=null&&listWorkOrderGrab.size()>0){
                logger.info("::::::::::::::::::根据teacherId从MySql中获取可抢课程列表::::::::::::::::::");
                jsonObject.put("workorderlist",listWorkOrderGrab);

            }else{
                logger.info("::::::::::::::::::没有需要补老师的课程-缓存与数据库中都没有可抢课程::::::::::::::::::");
            }
        }
        return JsonResultModel.newJsonResultModel(jsonObject);
    }

    public List<WorkOrder> filterListByStartTime(List<WorkOrder> list){
        Iterator<WorkOrder> iter = list.iterator();
        while(iter.hasNext()){
            WorkOrder workOrder = iter.next();
            if(compareDate(workOrder.getStartTime())){ //开始时间小于当前时间
                iter.remove();
            }
        }
        return list;
    }

    public boolean compareDate (Date date){
        long startTime = date.getTime();
        Date currDate = new Date();
        long currTime = currDate.getTime();
        if(currTime-startTime > 0){  //开始时间小于当前时间
            return true;
        }else{
            return false;
        }
    }


}
