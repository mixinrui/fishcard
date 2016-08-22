package com.boxfishedu.workorder.servicex.fishcardcenter;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.boxfishedu.workorder.common.bean.FishCardChargebackStatusEnum;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.QueueTypeEnum;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.rabbitmq.RabbitMqSender;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.CourseOnlineRequester;
import com.boxfishedu.workorder.service.*;
import com.boxfishedu.workorder.service.fishcardcenter.FishCardMakeUpService;
import com.boxfishedu.workorder.service.studentrelated.TimePickerService;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.web.param.MakeUpCourseParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.math.LongMath;
import com.google.gson.JsonObject;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by hucl on 16/6/14.
 * 补课相关的操作
 */
@Component
public class MakeUpLessionServiceX {

    @Autowired
    private WorkOrderService workOrderService;
    @Autowired
    private CourseScheduleService courseScheduleService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private TimePickerService timePickerService;

    @Autowired
    private CourseOnlineRequester courseOnlineRequester;

    @Autowired
    private FishCardMakeUpService fishCardMakeUpService;

    @Autowired
    private WorkOrderLogService workOrderLogService;

    @Value("${choiceTime.durationAllowMakeUp}")
    private Integer durationAllowMakeUp;

    @Value("${choiceTime.durationFromParentCourse}")
    private Integer durationFromParentCourse;

    @Autowired
    private RabbitMqSender rabbitMqSender;



    /**
     * 补课逻辑
     */
    public void makeUpCourse(MakeUpCourseParam makeUpCourseParam) {
        //此处状态没有锁,可能会稍有问题
        WorkOrder oldWorkOrder = workOrderService.findOne(makeUpCourseParam.getWorkOrderId());
        if (null == oldWorkOrder) {
            throw new BusinessException("所传参数不合法");
        }
        fishCardMakeUpService.processMakeUpParam(oldWorkOrder);
        CourseSchedule oldCourseSchedule = courseScheduleService.findByWorkOrderId(oldWorkOrder.getId());
        if (null == oldCourseSchedule) {
            throw new BusinessException("无对应的排课表");
        }
        WorkOrder newWorkOrder = getMakeUpWorkOrder(oldWorkOrder, makeUpCourseParam);
        CourseSchedule newCourseSchedule = getMakeUpSchedule(oldCourseSchedule, makeUpCourseParam);

        oldWorkOrder.setMakeUpFlag((short)1);
        //将已经安排补课的鱼卡的
        //将的鱼卡和courseschedule状态修改为补课
//        oldWorkOrder.setStatus(FishCardStatusEnum.FISHCARD_ADDITION.getCode());
//        oldCourseSchedule.setStatus(FishCardStatusEnum.FISHCARD_ADDITION.getCode());

        //保存入库
        fishCardMakeUpService.saveBothOldAndNew(oldWorkOrder, newWorkOrder, oldCourseSchedule, newCourseSchedule);
        //保存数据日志到mongodb

        //通知小马释放师生关系
        courseOnlineRequester.releaseGroup(oldWorkOrder);
        //调用教师资源分配
        List<CourseSchedule> newCourseScheduleList = Lists.newArrayList();
        newCourseScheduleList.add(newCourseSchedule);
        timePickerService.getRecommandTeachers(oldWorkOrder.getService(), newCourseScheduleList);
    }

    private WorkOrder getMakeUpWorkOrder(WorkOrder oldWorkOrder, MakeUpCourseParam makeUpCourseParam) {
        WorkOrder newWorkOrder = new WorkOrder();
        BeanUtils.copyProperties(oldWorkOrder, newWorkOrder);
        newWorkOrder.setTeacherId(0l);
        newWorkOrder.setTeacherName(null);
        newWorkOrder.setSlotId(makeUpCourseParam.getTimeSlotId());
        newWorkOrder.setStartTime(DateUtil.String2Date(makeUpCourseParam.getStartTime()));
        newWorkOrder.setEndTime(DateUtil.String2Date(makeUpCourseParam.getEndTime()));
        newWorkOrder.setId(null);
        newWorkOrder.setCreateTime(new Date());
        newWorkOrder.setUpdateTime(null);
        newWorkOrder.setActualStartTime(null);
        newWorkOrder.setActualEndTime(null);
        //已经安排补课
        newWorkOrder.setStatus(FishCardStatusEnum.COURSE_ASSIGNED.getCode());
        if (null == oldWorkOrder.getMakeUpSeq() || 0 == oldWorkOrder.getMakeUpSeq()) {
            newWorkOrder.setMakeUpSeq(1);
            newWorkOrder.setParentId(oldWorkOrder.getId());
            newWorkOrder.setParentRootId(oldWorkOrder.getId());
        } else {
            newWorkOrder.setMakeUpSeq(oldWorkOrder.getMakeUpSeq() + 1);
            newWorkOrder.setParentId(oldWorkOrder.getId());
            newWorkOrder.setParentRootId(oldWorkOrder.getParentRootId());
        }
        return newWorkOrder;
    }

    private CourseSchedule getMakeUpSchedule(CourseSchedule oldCourseSchedule, MakeUpCourseParam makeUpCourseParam) {
        CourseSchedule newCourseSchedule = new CourseSchedule();
        BeanUtils.copyProperties(oldCourseSchedule, newCourseSchedule);
        newCourseSchedule.setTimeSlotId(makeUpCourseParam.getTimeSlotId());
        newCourseSchedule.setTeacherId(0l);
        newCourseSchedule.setClassDate(DateUtil.String2SimpleDate(makeUpCourseParam.getEndTime()));
        newCourseSchedule.setId(null);
        newCourseSchedule.setStatus(FishCardStatusEnum.COURSE_ASSIGNED.getCode());
        newCourseSchedule.setCreateTime(new Date());
        newCourseSchedule.setUpdateTime(null);
        return newCourseSchedule;
    }



    /**
     * 更改鱼卡状态
     * @param makeUpCourseParam
     * @return
     */
    public JsonResultModel fishcardStatusChange(MakeUpCourseParam makeUpCourseParam){
        WorkOrder workOrder = workOrderService.findOne(makeUpCourseParam.getWorkOrderId());
        Map<String,String> resultMap = Maps.newHashMap();

        // 不存在该课程
        if(null == workOrder){
            resultMap.put("1","该课程不存在");
            return JsonResultModel.newJsonResultModel(resultMap);
        }

        // 该课程已经更改过一次
        if(workOrder.getUpdateManulFlag().equals("0")){
            resultMap.put("2","该课程已经更改过");
            return JsonResultModel.newJsonResultModel(resultMap);
        }

        // 该课程状态未进行变更
        if(workOrder.getStatus() == makeUpCourseParam.getFishStatus()){
            resultMap.put("3","该课程状态未进行变更");
            return JsonResultModel.newJsonResultModel(resultMap);
        }
        CourseSchedule courseSchedule = courseScheduleService.findByWorkOrderId(workOrder.getId());

        // 该课程状态未进行变更
        if(null == courseSchedule){
            resultMap.put("4","该课程不存在");
            return JsonResultModel.newJsonResultModel(resultMap);
        }
        // 课程
        courseSchedule.setStatus(makeUpCourseParam.getFishStatus());
        courseSchedule.setUpdateTime(DateTime.now().toDate());

        // 鱼卡
        workOrder.setStatus(makeUpCourseParam.getFishStatus());
        workOrder.setUpdateManulFlag("0");// 已经更新过
        workOrder.setUpdateTime(new Date());
        // 更新课程  鱼卡
        workOrderService.saveWorkOrderAndSchedule(workOrder,courseSchedule);

        // 记录日志
        workOrderLogService.saveWorkOrderLog(workOrder);

        resultMap.put("0","更新成功");
        return JsonResultModel.newJsonResultModel(resultMap);
    }

    /**
     * 批量确认按钮
     * @param makeUpCourseParam
     * @return
     */
    public JsonResultModel fishcardStatusRechargeChange(MakeUpCourseParam makeUpCourseParam){
        Map<String,String> resultMap = Maps.newHashMap();
        if(null == makeUpCourseParam || null ==makeUpCourseParam.getWorkOrderIds()|| makeUpCourseParam.getWorkOrderIds().length<1){
            resultMap.put("1","参数有错误");
            return JsonResultModel.newJsonResultModel(resultMap);
        }

        List<WorkOrder>  workOrders = workOrderService.getAllWorkOrdersByIds(makeUpCourseParam.getWorkOrderIds());

        if(null==workOrders || workOrders.size()<1){
            resultMap.put("1","参数有错误");
            return JsonResultModel.newJsonResultModel(resultMap);
        }

        for(WorkOrder wo:workOrders){
            // 1 正常  0  未开始 或者正在进行
            if("0".equals(wo.getIsCourseOver())){
                resultMap.put("2","请核实鱼卡信息,该课程未开始或者已经正在进行中!");
                return JsonResultModel.newJsonResultModel(resultMap);
            }
            wo.setConfirmFlag("0");
            wo.setStatusRecharge(FishCardChargebackStatusEnum.NEED_RECHARGEBACK.getCode());
            wo.setUpdatetimeRecharge(new Date());
        }

        //更新已经确认
        workOrderService.updateWorkStatusRechargeOrderByIds(workOrders);

        // 记录日志
        workOrderLogService.batchSaveWorkOrderLogs(workOrders);
        resultMap.put("0","更新成功");
        return JsonResultModel.newJsonResultModel(resultMap);
    }


    /**
     * 页面发起退款申请
     * @param makeUpCourseParam
     * @return
     */
    public JsonResultModel fishcardConfirmStatusRecharge(MakeUpCourseParam makeUpCourseParam){
        Map<String,String> resultMap = Maps.newHashMap();
        if(null == makeUpCourseParam || null ==makeUpCourseParam.getWorkOrderIds()|| makeUpCourseParam.getWorkOrderIds().length<1){
            logger.info("::fishcardConfirmStatusRecharge1::");
            resultMap.put("1","参数有错误");
        }

        List<WorkOrder>  workOrders = workOrderService.getAllWorkOrdersByIds(makeUpCourseParam.getWorkOrderIds());

        if(null==workOrders || workOrders.size()<1){
            logger.info("::fishcardConfirmStatusRecharge1::");
            resultMap.put("1","参数有错误");
        }

        Long orderId = workOrders.get(0).getOrderId();
        for(WorkOrder wo:workOrders){
//            if(!orderId.equals(   wo.getOrderId())){
//                resultMap.put("3","退款的鱼卡应该在同一个订单里!");
//                return JsonResultModel.newJsonResultModel(resultMap);
//            }
            // 1 正常  0  未开始 或者正在进行
            if("0".equals(wo.getIsCourseOver())){
                logger.info("::fishcardConfirmStatusRecharge2::");
                resultMap.put("2","请核实鱼卡信息,该课程未开始或者已经正在进行中!");
                return JsonResultModel.newJsonResultModel(resultMap);
            }

            if(FishCardChargebackStatusEnum.NEED_RECHARGEBACK.getCode() != wo.getStatusRecharge()){
                logger.info("::fishcardConfirmStatusRecharge3::");
                resultMap.put("3","请核实鱼卡信息,鱼卡退款状态有不符合标准退款流程,请合适数据!");
                return JsonResultModel.newJsonResultModel(resultMap);
            }

            if(!wo.getStatus().equals(FishCardStatusEnum. TEACHER_ABSENT.getCode()  ) ||  !wo.getStatus().equals(FishCardStatusEnum. TEACHER_LEAVE_EARLY.getCode())  || !wo.getStatus().equals( FishCardStatusEnum. EXCEPTION.getCode()) ){
                logger.info("::fishcardConfirmStatusRecharge4::");
                resultMap.put("4","请核实鱼卡信息,鱼卡状态不符合退款要求!");
                return JsonResultModel.newJsonResultModel(resultMap);
            }

            wo.setStatusRecharge(FishCardChargebackStatusEnum.RECHARGBACKING.getCode());
            wo.setUpdatetimeRecharge(new Date());
        }

        //更新已经确认
        workOrderService.updateWorkStatusRechargeOrderByIds(workOrders);


        // 记录日志
        workOrderLogService.batchSaveWorkOrderLogs(workOrders);
        resultMap.put("0","更新成功");


        /**
         * 向订单发送Q消息
         * 目前针对多个鱼卡  每个鱼卡 单次发送请求确认退款
         */

        workOrders.forEach(workOrderMessage -> {
           JSONObject message =generator(workOrderMessage);logger.info("::::::fishcardConfirmStatusRecharge:::::[{}]",message);
            rabbitMqSender.send(message.toJSONString(), QueueTypeEnum.RECHARGE_ORDER);
        });




        return JsonResultModel.newJsonResultModel(resultMap);
    }

    /**
     * 订单发送退款最终状态确认
     * @param makeUpCourseParam
     * @return
     */
    public JsonResultModel fixedStateFromOrder(MakeUpCourseParam makeUpCourseParam){
        Map<String,String> resultMap = Maps.newHashMap();
        if(null == makeUpCourseParam || null ==makeUpCourseParam.getWorkOrderIds()|| makeUpCourseParam.getWorkOrderIds().length<1 ){
            resultMap.put("1","参数有错误");
            return JsonResultModel.newJsonResultModel(resultMap);
        }

        List<WorkOrder>  workOrders = workOrderService.getAllWorkOrdersByIds(makeUpCourseParam.getWorkOrderIds());

        if(null==workOrders || workOrders.size()<1){
            resultMap.put("2","参数有错误");
            return JsonResultModel.newJsonResultModel(resultMap);
        }

        Long orderId = workOrders.get(0).getOrderId();
        Boolean successFlag = makeUpCourseParam.getSuccessFlag();
        if(null==successFlag){
            resultMap.put("3","无成功失败标示");
            logger.info("fixedStateFromOrder订单id[{}],无成功失败标示",orderId);
            return JsonResultModel.newJsonResultModel(resultMap);
        }
        for(WorkOrder wo:workOrders){

           if(successFlag){
               wo.setStatusRecharge(FishCardChargebackStatusEnum.RECHARGEBACK_SUCCESS.getCode());
           }else{
               wo.setStatusRecharge(FishCardChargebackStatusEnum.RECHARGEBACK_FAILED.getCode());
           }
            wo.setUpdatetimeRecharge(new Date());
        }
        //更新已经确认
        workOrderService.updateWorkStatusRechargeOrderByIds(workOrders);


        // 记录日志
        workOrderLogService.batchSaveWorkOrderLogs(workOrders);
        resultMap.put("0","更新成功");
        return JsonResultModel.newJsonResultModel(resultMap);
    }



    public String generator(List<WorkOrder> workOrders){
        WorkOrder workOrder = workOrders.get(0);
        JSONObject json =new JSONObject();
        json.put("orderId",workOrder.getOrderId());
        json.put("orderCode",workOrder.getOrderCode());

        JSONArray  jsonArray = new JSONArray();
        for(WorkOrder wo:workOrders){
            JSONObject jsinner =new JSONObject();
            jsinner.put("workOrderId",wo.getId());
            jsinner.put("skuId","??????????skuid待定???????");
            jsinner.put("orderType","???????订单类型待定?????????");
            jsinner.put("courseType",wo.getCourseType());
            jsinner.put("reason",FishCardStatusEnum.get(wo.getStatus()).getDesc());
            jsonArray.add(jsinner);
        }

        json.put("fishCardIds",jsonArray);
        return json.toString();

    }
    public JSONObject generator(WorkOrder workOrder){
        JSONObject json =new JSONObject();
        json.put("orderId",workOrder.getOrderId());
        json.put("orderCode",workOrder.getOrderCode()==null?"":workOrder.getOrderCode());
        json.put("skuId",232323L);
        json.put("refundReason",FishCardStatusEnum.get(workOrder.getStatus()).getDesc());
        json.put("courseType",workOrder.getCourseType());
        json.put("fishCardId",workOrder.getId());
        return json;

    }




}
