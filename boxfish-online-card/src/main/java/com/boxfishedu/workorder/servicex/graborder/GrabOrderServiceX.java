package com.boxfishedu.workorder.servicex.graborder;

import com.alibaba.fastjson.JSONObject;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.exception.BoxfishException;
import com.boxfishedu.workorder.common.redis.CacheKeyConstant;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.WorkOrderConstant;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.entity.mysql.WorkOrderGrab;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.ServiceSDK;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.graborder.GrabOrderService;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by mk on 16/7/12.
 */
@Component
public class GrabOrderServiceX {

    //本地异常日志记录对象
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ServiceSDK serviceSDK;

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

    @Autowired
    private CourseScheduleService courseScheduleService;

    @Autowired
    private WorkOrderLogService workOrderLogService;


    public JsonResultModel getWorkOrderListByTeacherId(Long teacherId){
        List<WorkOrderGrab> listWorkOrderGrab = this.getFromMySql(teacherId);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msg", WorkOrderConstant.FISHCARD_LIST);
        if(listWorkOrderGrab!=null&&listWorkOrderGrab.size()>0){
            logger.error("::::::::::::::::::获取该老师可以抢的课程列表::::::::::::::::::");
            jsonObject.put("code",0);
            jsonObject.put("workorderlist",listWorkOrderGrab);
        }
        else{
            logger.error("::::::::::::::::::数据库中都没有可抢课程::::::::::::::::::");
            jsonObject.put("code",1);
            jsonObject.put("workorderlist",null);
        }
        return JsonResultModel.newJsonResultModel(jsonObject);
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
        return grabOrderService.findByTeacherId(workOrderGrab);
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

    @Transactional
    public JsonResultModel grabOrderByOneTeacher(GrabOrderView grabOrderView){
        JSONObject jsonObject = new JSONObject();
        if(!checkIfCanGrabOrderByOnlineTeacher(grabOrderView)||!checkIfCanGrabOrderByOnlineFishcard(grabOrderView)){
//     if(!checkIfCanGrabOrderByOnlineFishcard(grabOrderView)){
            grabOrderService.setFlagFailAndTeacherId(grabOrderView);
            jsonObject.put("msg",WorkOrderConstant.GRABORDER_FAIL);
            jsonObject.put("code","1");
        }else{
            WorkOrder workOrder = grabOrderService.findByIdForUpdate(grabOrderView.getWorkOrderId());
            if(workOrder!=null){
                if(compareDate(workOrder.getStartTime())){
                    grabOrderService.setFlagFailAndTeacherId(grabOrderView);
                    jsonObject.put("msg",WorkOrderConstant.GRABORDER_FAIL);
                    jsonObject.put("code","1");
                    logger.info("::::::::::::::::::单子已过期,抢单失败::::::::::::::::::");
                }else{


                    grabOrderView.setState(FishCardStatusEnum.TEACHER_ASSIGNED.getCode());

                    //更新鱼卡(状态  教师id)
                    int updateCount = grabOrderService.updateTestGrab(grabOrderView);

                    if(updateCount!=1){
                        //抢单失败
                        grabOrderService.setFlagFailAndTeacherId(grabOrderView);
                        jsonObject.put("msg",WorkOrderConstant.GRABORDER_FAIL);
                        jsonObject.put("code","1");
                        return JsonResultModel.newJsonResultModel(jsonObject);
                    }

                    // 更新鱼卡
                    grabOrderService.setFlagSuccessAndTeacherId(grabOrderView);

                    workOrder.setStatus( FishCardStatusEnum.TEACHER_ASSIGNED.getCode());
                    workOrder.setTeacherId(grabOrderView.getTeacherId());
                    workOrder.setUpdateTime(new Date());
                    workOrder.setAssignTeacherTime(new Date());

                    logger.info("::::::::::::::::changeFishCardStatusForGrab::::::::[{}]:::",workOrder);
                    courseScheduleService.findByWorkOrderIdForUpdate(grabOrderView);
                    courseScheduleService.setTeacherIdByWorkOrderId(grabOrderView);
                    // 纪录日志



                    workOrderLogService.saveWorkOrderLog(workOrder);
                    jsonObject.put("msg",WorkOrderConstant.GRABORDER_SUCCESS);
                    jsonObject.put("code","0");

                    // 向在线运营发送建组(小马)
                    serviceSDK.createGroup(workOrder);
                    logger.info("::::::::::::::::::成功抢单::::::::::::::::::");
                }
            }
        }
        return JsonResultModel.newJsonResultModel(jsonObject);
    }

    private boolean checkIfCanGrabOrderByOnlineFishcard(GrabOrderView grabOrderView){
        WorkOrder workOrder = workOrderService.findOne(grabOrderView.getWorkOrderId());
        if(workOrder!=null){
            if(workOrder.getTeacherId()!=null&&workOrder.getTeacherId()>0){    //该课程已经被其他老师抢了
                logger.info("::::::::::::::::::OnlineFishcard验证----不能抢(teacherId>0)::::::::::::::::::");
                return false;
            }else{
                logger.info("::::::::::::::::::OnlineFishcard验证----能抢::::::::::::::::::");
                return true;
            }
        }
        logger.info("::::::::::::::::::OnlineFishcard验证----不能抢(在workorder表中根据workOrderId没有查出记录)::::::::::::::::::");
        return false;
    }

    private boolean checkIfCanGrabOrderByOnlineTeacher(GrabOrderView grabOrderView) throws BoxfishException {
        Map<String,Object> mapParams = this.makeParams(grabOrderView);
//      String url = "http://192.168.77.210:8099/order/course/schedule/add/order/time";   //TODO
        String url=String.format("%s/order/course/schedule/add/order/time", urlConf.getTeacher_service());
        JsonResultModel jsonResultModel=restTemplate.postForObject(url,mapParams,JsonResultModel.class);
        if(jsonResultModel.getReturnCode()== HttpStatus.OK.value()){
            logger.info("::::::::::::::::::OnlineTeacher验证----能抢(returnCode==200)::::::::::::::::::");
            return true;
        }else{
            logger.info("::::::::::::::::::OnlineTeacher验证----不能抢::::::::::::::::::");
            return false;
        }
    }

    private Map<String,Object> makeParams (GrabOrderView grabOrderView){
        WorkOrder workOrder = workOrderService.findOne(grabOrderView.getWorkOrderId());
        Map<String,Object> mapParams =  Maps.newHashMap();
        mapParams.put("studentId",workOrder.getStudentId());
        mapParams.put("slotId",workOrder.getSlotId());
        mapParams.put("startTime", DateUtil.Date2String(workOrder.getStartTime()));
        mapParams.put("teacherId",grabOrderView.getTeacherId());
        return mapParams;
    }




}
