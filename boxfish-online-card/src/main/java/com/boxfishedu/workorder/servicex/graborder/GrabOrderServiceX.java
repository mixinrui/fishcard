package com.boxfishedu.workorder.servicex.graborder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.exception.BoxfishException;
import com.boxfishedu.workorder.common.redis.CacheKeyConstant;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.common.util.WorkOrderConstant;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.entity.mysql.WorkOrderGrab;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.graborder.GrabOrderService;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.web.view.fishcard.GrabOrderView;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Created by mk on 16/7/12.
 */
@Component
public class GrabOrderServiceX {

    //本地异常日志记录对象
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private GrabOrderService grabOrderService;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private UrlConf urlConf;

    @Autowired
    private RestTemplate restTemplate;


    public JsonResultModel getWorkOrderListByTeacherId(Long teacherId){
        List<WorkOrderGrab> listWorkOrderGrab = this.getFromMySql(teacherId);
        if(listWorkOrderGrab!=null&&listWorkOrderGrab.size()>0){
            logger.info("::::::::::::::::::根据teacherId从Redis中获取可抢课程列表::::::::::::::::::");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("msg", WorkOrderConstant.FISHCARD_LIST);
            jsonObject.put("code",0);
            jsonObject.put("workorderlist",listWorkOrderGrab);
            return JsonResultModel.newJsonResultModel(jsonObject);
        }
        else{
            logger.error("::::::::::::::::::没有需要补老师的课程-缓存与数据库中都没有可抢课程::::::::::::::::::");
            return JsonResultModel.newJsonResultModel(null);
        }
    }

    //TODO 等待被召唤的一天
    private List<WorkOrder> getFromRedis(Long teacherId){
        List<WorkOrder> listWorkOrder = Lists.newArrayList();
        listWorkOrder = cacheManager.getCache(CacheKeyConstant.FISHCARD_WORKORDER_GRAB_KEY).get(teacherId, List.class);
        if(listWorkOrder!=null&&listWorkOrder.size()>0){
            listWorkOrder = filterListByStartTime(listWorkOrder);
        }
        return listWorkOrder;
    }

    private List<WorkOrderGrab> getFromMySql(Long teacherId){
        WorkOrderGrab workOrderGrab = new WorkOrderGrab();
        workOrderGrab.setTeacherId(teacherId);
        workOrderGrab.setFlag("0");
        return grabOrderService.findByTeacherIdAndFlagAndStartTimeGreaterThan(workOrderGrab);
    }

    private List<WorkOrder> filterListByStartTime(List<WorkOrder> list){
        Iterator<WorkOrder> iter = list.iterator();
        while(iter.hasNext()){
            WorkOrder workOrder = iter.next();
            if(compareDate(workOrder.getStartTime())){ //开始时间小于当前时间
                iter.remove();
            }
        }
        return list;
    }

    private boolean compareDate (Date date){
        long startTime = date.getTime();
        Date currDate = new Date();
        long currTime = currDate.getTime();
        if(currTime-startTime > 0){  //开始时间小于当前时间
            return true;
        }else{
            return false;
        }
    }

    public JsonResultModel grabOrderByOneTeacher(GrabOrderView grabOrderView){
        JSONObject jsonObject = new JSONObject();
        if(checkIfCanGrabOrder(grabOrderView)){
            jsonObject.put("msg",WorkOrderConstant.GRABORDER_FAIL);
            jsonObject.put("code","0");
        }else{
            grabOrderService.setFlagAndTeacherId(grabOrderView);
            grabOrderService.setTeacherIdByWorkOrderId(grabOrderView);
            jsonObject.put("msg",WorkOrderConstant.GRABORDER_SUCCESS);
            jsonObject.put("code","1");
        }
        return JsonResultModel.newJsonResultModel(jsonObject);
    }

    private boolean checkIfCanGrabOrder(GrabOrderView grabOrderView) throws BoxfishException {
        Map<String,Object> mapParams = this.makeParams(grabOrderView);
        String url = "192.168.77.210:8099/order/course/schedule/add/order/time";   //TODO
        JsonResultModel jsonResultModel=restTemplate.postForObject(url,mapParams,JsonResultModel.class);
        if(jsonResultModel.getReturnCode()== HttpStatus.OK.value()){
            logger.error("::::::::::::::::::teacherId:"+grabOrderView.getTeacherId()+"可以抢workorderId:"+grabOrderView.getWorkOrderId()+"的课程::::::::::::::::::");
            return true;
        }else{
            return false;
        }
    }

    private Map<String,Object> makeParams (GrabOrderView grabOrderView){
        WorkOrder workOrder = workOrderService.findOne(grabOrderView.getWorkOrderId());
        Map<String,Object> mapParams =  Maps.newHashMap();
        mapParams.put("studentId",workOrder.getStudentId());
        mapParams.put("slotId",workOrder.getSlotId());
        mapParams.put("startTime",workOrder.getStartTime());
        mapParams.put("teacherId",grabOrderView.getTeacherId());
        return mapParams;
    }




}
