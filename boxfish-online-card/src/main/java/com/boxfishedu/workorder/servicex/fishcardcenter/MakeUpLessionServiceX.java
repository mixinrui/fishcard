package com.boxfishedu.workorder.servicex.fishcardcenter;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.boxfishedu.workorder.common.bean.FishCardChargebackStatusEnum;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.MessagePushTypeEnum;
import com.boxfishedu.workorder.common.bean.QueueTypeEnum;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.rabbitmq.RabbitMqSender;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.WorkOrderConstant;
import com.boxfishedu.workorder.entity.mongo.ScheduleCourseInfo;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.CourseOnlineRequester;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.*;
import com.boxfishedu.workorder.service.fishcardcenter.FishCardMakeUpService;
import com.boxfishedu.workorder.service.graborder.GrabOrderService;
import com.boxfishedu.workorder.service.studentrelated.TimePickerService;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.web.param.MakeUpCourseParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.web.view.fishcard.GrabOrderView;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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


    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    @Autowired
    private ScheduleCourseInfoService scheduleCourseInfoService;


    /**
     * 补课逻辑
     */
    public void makeUpCourse(MakeUpCourseParam makeUpCourseParam) {
        //此处状态没有锁,可能会稍有问题
        WorkOrder oldWorkOrder = workOrderService.findOne(makeUpCourseParam.getWorkOrderId());
        if (null == oldWorkOrder) {
            throw new BusinessException("所传参数不合法");
        }
        CourseSchedule oldCourseSchedule = courseScheduleService.findByWorkOrderId(oldWorkOrder.getId());
        if (null == oldCourseSchedule) {
            throw new BusinessException("无对应的排课表");
        }
        fishCardMakeUpService.processMakeUpParam(oldWorkOrder);

        WorkOrder newWorkOrder = getMakeUpWorkOrder(oldWorkOrder, makeUpCourseParam);
        CourseSchedule newCourseSchedule = getMakeUpSchedule(oldCourseSchedule, makeUpCourseParam);

        oldWorkOrder.setMakeUpFlag((short) 1);

        //保存入库
        fishCardMakeUpService.saveBothOldAndNew(oldWorkOrder, newWorkOrder, oldCourseSchedule, newCourseSchedule);

        ScheduleCourseInfo scheduleCourseInfo=scheduleCourseInfoService.queryByWorkId(oldWorkOrder.getId());
        ScheduleCourseInfo clone=new ScheduleCourseInfo();
        BeanUtils.copyProperties(scheduleCourseInfo,clone);
        clone.setId(null);
        clone.setWorkOrderId(newWorkOrder.getId());
        clone.setScheduleId(newCourseSchedule.getId());
        scheduleCourseInfoService.save(clone);

        workOrderLogService.saveWorkOrderLog(newWorkOrder,"补课生成鱼卡");

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
        newWorkOrder.setIsCourseOver((short) 0);
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
     *
     * @param makeUpCourseParam
     * @return
     */
    public JsonResultModel fishcardStatusChange(MakeUpCourseParam makeUpCourseParam) {
        WorkOrder workOrder = workOrderService.findOne(makeUpCourseParam.getWorkOrderId());
        Map<String, String> resultMap = Maps.newHashMap();

        // 不存在该课程
        if (null == workOrder) {
            resultMap.put("1", "该课程不存在");
            return JsonResultModel.newJsonResultModel(resultMap);
        }

        // 该课程已经更改过一次
        if ("0" .equals( workOrder.getUpdateManulFlag() )) {
            resultMap.put("2", "该课程已经更改过");
            return JsonResultModel.newJsonResultModel(resultMap);
        }

        // 该课程状态未进行变更
        if (workOrder.getStatus() == makeUpCourseParam.getFishStatus()) {
            resultMap.put("3", "该课程状态未进行变更");
            return JsonResultModel.newJsonResultModel(resultMap);
        }
        CourseSchedule courseSchedule = courseScheduleService.findByWorkOrderId(workOrder.getId());

        // 该课程状态未进行变更
        if (null == courseSchedule) {
            resultMap.put("4", "该课程不存在");
            return JsonResultModel.newJsonResultModel(resultMap);
        }

        // 已经确认过状态的鱼卡不可以进行状态更正
        if("0".equals(workOrder.getConfirmFlag())){
            resultMap.put("5", "该课程已经进行过状态确认,可能进行状态更正");
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
        workOrderService.saveWorkOrderAndSchedule(workOrder, courseSchedule);

        // 记录日志
        workOrderLogService.saveWorkOrderLog(workOrder);

        resultMap.put("0", "更新成功");
        return JsonResultModel.newJsonResultModel(resultMap);
    }

    /**
     * 批量确认按钮
     *
     * @param makeUpCourseParam
     * @return
     */
    public JsonResultModel fishcardStatusRechargeChange(MakeUpCourseParam makeUpCourseParam) {
        Map<String, String> resultMap = Maps.newHashMap();
        if (null == makeUpCourseParam || null == makeUpCourseParam.getWorkOrderIds() || makeUpCourseParam.getWorkOrderIds().length < 1) {
            resultMap.put("1", "参数有错误");
            return JsonResultModel.newJsonResultModel(resultMap);
        }

        List<WorkOrder> workOrders = workOrderService.getAllWorkOrdersByIds(makeUpCourseParam.getWorkOrderIds());

        if (null == workOrders || workOrders.size() < 1) {
            resultMap.put("1", "参数有错误");
            return JsonResultModel.newJsonResultModel(resultMap);
        }

        for (WorkOrder wo : workOrders) {
            // 1 正常  0  未开始 或者正在进行
            if ("0".equals(wo.getIsCourseOver())) {
                resultMap.put("2", "请核实鱼卡信息,该课程未开始或者已经正在进行中!");
                return JsonResultModel.newJsonResultModel(resultMap);
            }
            if("0".equals(wo.getConfirmFlag())){
                resultMap.put("2", "该鱼卡已经确认过状态!");
                return JsonResultModel.newJsonResultModel(resultMap);
            }
        }


        this.fishcardStatusRechargeChangeLast(workOrders);

        resultMap.put("0", "更新成功");
        return JsonResultModel.newJsonResultModel(resultMap);
    }

    // db操作 确认状态
    public void fishcardStatusRechargeChangeLast(List<WorkOrder> workOrders){
        for (WorkOrder wo : workOrders) {
            wo.setConfirmFlag("0");
            // 50 52 60 三种状态为退款状态
            if(     wo.getStatus() == FishCardStatusEnum.TEACHER_ABSENT.getCode() ||
                    wo.getStatus() == FishCardStatusEnum.TEACHER_LEAVE_EARLY.getCode() ||
                    wo.getStatus() == FishCardStatusEnum.EXCEPTION.getCode()
                    ){
                wo.setStatusRecharge(FishCardChargebackStatusEnum.NEED_RECHARGEBACK.getCode());
            }

            wo.setUpdatetimeRecharge(new Date());
        }

        //更新已经确认
        workOrderService.updateWorkStatusRechargeOrderByIds(workOrders);

        // 记录日志
        workOrderLogService.batchSaveWorkOrderLogs(workOrders);
    }


    @Autowired
    private GrabOrderService grabOrderService;

    /**
     * 页面发起退款申请
     *
     * @param makeUpCourseParam
     * @return
     */
    @Transactional
    public JsonResultModel fishcardConfirmStatusRecharge(MakeUpCourseParam makeUpCourseParam) {
        String msg = "";
        logger.info("begin:fishcardConfirmStatusRecharge:::WorkOrderIds()=>[{}]",makeUpCourseParam.getWorkOrderIds());
        Map<String, String> resultMap = Maps.newHashMap();
        if (null == makeUpCourseParam || null == makeUpCourseParam.getWorkOrderIds() || makeUpCourseParam.getWorkOrderIds().length < 1) {
            logger.info("::fishcardConfirmStatusRecharge1::");
            resultMap.put("1", "参数有错误");
            return JsonResultModel.newJsonResultModel(resultMap);
        }

        List<WorkOrder> workOrders = workOrderService.getAllWorkOrdersByIds(makeUpCourseParam.getWorkOrderIds());

        if (null == workOrders || workOrders.size() < 1) {
            logger.info("::fishcardConfirmStatusRecharge1::");
            resultMap.put("1", "参数有错误");
            return JsonResultModel.newJsonResultModel(resultMap);
        }

        Long orderId = workOrders.get(0).getOrderId();
        for (WorkOrder wo : workOrders) {
            // 1 正常  0  未开始 或者正在进行
            if ("0".equals(wo.getIsCourseOver())) {
                logger.info("::fishcardConfirmStatusRecharge2::");
                resultMap.put("2", "请核实鱼卡信息,该课程未开始或者已经正在进行中!");
                return JsonResultModel.newJsonResultModel(resultMap);
            }

            if (FishCardChargebackStatusEnum.NEED_RECHARGEBACK.getCode() != wo.getStatusRecharge().intValue()) {
                logger.info("::fishcardConfirmStatusRecharge3:0[{}]:1[{}]::2[{}]", wo.getId(), FishCardChargebackStatusEnum.RECHARGBACKING.getCode(), wo.getStatusRecharge());
                resultMap.put("3", "请核实鱼卡信息,鱼卡退款状态有不符合标准退款流程,请合适数据!");
                return JsonResultModel.newJsonResultModel(resultMap);
            }

            if (wo.getStatus().equals(FishCardStatusEnum.TEACHER_ABSENT.getCode()) || wo.getStatus().equals(FishCardStatusEnum.TEACHER_LEAVE_EARLY.getCode()) || wo.getStatus().equals(FishCardStatusEnum.EXCEPTION.getCode())) {

            } else {
                logger.info("::fishcardConfirmStatusRecharge4:[{}]:", wo.getStatus());
                resultMap.put("4", "请核实鱼卡信息,鱼卡状态不符合退款要求!");
                return JsonResultModel.newJsonResultModel(resultMap);
            }

            int count = workOrderService.updateWorkFishRechargeOne(FishCardChargebackStatusEnum.RECHARGBACKING.getCode(), wo.getId());
            logger.info("fishcardConfirmStatusRecharge7:count[{}]", count);
            if (count > 0) {
                logger.info("fishcardConfirmStatusRecharge6 鱼卡id[{}]  用户账号 [{}]发送退款请求成功 ", wo.getId(),makeUpCourseParam.getUsername());
                msg += (wo.getId() + "success");
                JSONObject message = generator(wo);
                logger.info("::::::fishcardConfirmStatusRecharge:::::[{}]", message);
                rabbitMqSender.send(message.toJSONString(), QueueTypeEnum.RECHARGE_ORDER);

            } else {
                msg += (wo.getId() + "failed");
            }
        }

        // 记录日志
        workOrderLogService.batchSaveWorkOrderLogs(workOrders);
        resultMap.put("0", msg);
        logger.info("fishcardConfirmStatusRecharge ::msg [{}]",msg);
        return JsonResultModel.newJsonResultModel(resultMap);
    }

    /**
     * 订单发送退款最终状态确认
     *
     * @param makeUpCourseParam
     * @return
     */
    public JsonResultModel fixedStateFromOrder(MakeUpCourseParam makeUpCourseParam) {
        Map<String, String> resultMap = Maps.newHashMap();
        if (null == makeUpCourseParam || null == makeUpCourseParam.getWorkOrderIds() || makeUpCourseParam.getWorkOrderIds().length < 1) {
            logger.info("fixedStateFromOrder1 参数有误");
            resultMap.put("1", "参数有错误");
            return JsonResultModel.newJsonResultModel(resultMap);
        }

        logger.info("fixedStateFromOrder::makeUpCourseParam.getWorkOrderIds():::[{}]", makeUpCourseParam.getWorkOrderIds());

        List<WorkOrder> workOrders = workOrderService.getAllWorkOrdersByIds(makeUpCourseParam.getWorkOrderIds());

        if (null == workOrders || workOrders.size() < 1) {
            logger.info("fixedStateFromOrder2 参数有误");
            resultMap.put("2", "参数有错误");
            return JsonResultModel.newJsonResultModel(resultMap);
        }

        Long orderId = workOrders.get(0).getOrderId();
        Boolean successFlag = makeUpCourseParam.getSuccessFlag();
        if (null == successFlag) {
            resultMap.put("3", "无成功失败标示");
            logger.info("fixedStateFromOrder订单id[{}],无成功失败标示", orderId);
            return JsonResultModel.newJsonResultModel(resultMap);
        }
        for (WorkOrder wo : workOrders) {

            if (successFlag) {
                // 推送成功消息
                sendMessageRefund(wo);
                wo.setStatusRecharge(FishCardChargebackStatusEnum.RECHARGEBACK_SUCCESS.getCode());
            } else {
                wo.setStatusRecharge(FishCardChargebackStatusEnum.RECHARGEBACK_FAILED.getCode());
            }
            wo.setUpdatetimeRecharge(new Date());
        }
        //更新已经确认
        workOrderService.updateWorkStatusRechargeOrderByIds(workOrders);


        // 记录日志
        workOrderLogService.batchSaveWorkOrderLogs(workOrders);
        resultMap.put("0", "更新成功");
        return JsonResultModel.newJsonResultModel(resultMap);
    }


    public String generator(List<WorkOrder> workOrders) {
        WorkOrder workOrder = workOrders.get(0);
        JSONObject json = new JSONObject();
        json.put("orderId", workOrder.getOrderId());
        json.put("orderCode", workOrder.getOrderCode());

        JSONArray jsonArray = new JSONArray();
        for (WorkOrder wo : workOrders) {
            JSONObject jsinner = new JSONObject();
            jsinner.put("workOrderId", wo.getId());
            jsinner.put("skuId", wo.getSkuId());
            jsinner.put("orderType", wo.getOrderChannel());
            jsinner.put("courseType", wo.getCourseType());
            jsinner.put("reason", FishCardStatusEnum.get(wo.getStatus()).getDesc());
            jsonArray.add(jsinner);
        }

        json.put("fishCardIds", jsonArray);
        return json.toString();

    }

    public JSONObject generator(WorkOrder workOrder) {
        JSONObject json = new JSONObject();
        json.put("orderId", workOrder.getOrderId());
        json.put("orderCode", workOrder.getOrderCode() == null ? "" : workOrder.getOrderCode());
        json.put("skuId", 232323L);
        json.put("refundReason", FishCardStatusEnum.get(workOrder.getStatus()).getDesc());
        json.put("courseType", workOrder.getCourseType());
        json.put("fishCardId", workOrder.getId());
        return json;

    }


    /**
     * 向学生推送退款消息
     *
     * @param wo
     */
    public void sendMessageRefund(WorkOrder wo) {
        logger.info("sendMessageRefund 退款消息 id [{}]", wo.getId());

        logger.info("sendMessageRefund::begin");
        List list = Lists.newArrayList();
        String reason = wo.getStatus() == FishCardStatusEnum.EXCEPTION.getCode() ? "不可抗力因素" : FishCardStatusEnum.get(wo.getStatus()).getDesc();// 描述原因
        String pushTitle = WorkOrderConstant.SEND_STU_CLASS_REFUND_ONE
                + DateUtil.Date2StringChinese(wo.getStartTime()) +  // 开始时间
                WorkOrderConstant.SEND_STU_CLASS_REFUND_TWO
                + trimTitle(wo.getCourseName()) +                            // 课程名
                WorkOrderConstant.SEND_STU_CLASS_REFUND_THREE
                + reason +                                        // 原因
                WorkOrderConstant.SEND_STU_CLASS_REFUND_FOUR;
        logger.info("sendMessageRefund title [{}] ,reason [{}]", pushTitle, reason);
        Map map1 = Maps.newHashMap();
        map1.put("user_id", wo.getStudentId());
        map1.put("push_title", pushTitle);
        JSONObject jo = new JSONObject();
        jo.put("type", MessagePushTypeEnum.SEND_STUDENT_CLASS_REFUND_TYPE.toString());
        jo.put("push_title", pushTitle);

        map1.put("data", jo);
        list.add(map1);

        teacherStudentRequester.pushTeacherListOnlineMsg(list);


        logger.info("sendMessageRefund::end");


    }


    /**
     * 对课程名进行截取
     *
     * @param title
     * @return
     */
    private String trimTitle(String title) {
        if (StringUtils.isEmpty(title)) {
            return "";
        }
        int len = title.length();
        if (len > 60 || (len > 50 && len < 60)) {
            return (title.substring(0, 50) + "...");
        }
        return title;
    }


}
