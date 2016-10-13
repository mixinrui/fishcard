package com.boxfishedu.workorder.web.controller;

import com.boxfishedu.workorder.common.bean.ComboTypeEnum;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.QueueTypeEnum;
import com.boxfishedu.workorder.common.rabbitmq.RabbitMqSender;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.common.util.ConstantUtil;
import com.boxfishedu.workorder.dao.jpa.ServiceJpaRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.dao.mongo.ContinousAbsenceMorphiaRepository;
import com.boxfishedu.workorder.entity.mongo.ContinousAbsenceRecord;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.ServeService;
import com.boxfishedu.workorder.service.absencendeal.AbsenceDealService;
import com.boxfishedu.workorder.service.accountcardinfo.AccountCardInfoService;
import com.boxfishedu.workorder.service.accountcardinfo.DataCollectorService;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sun.javafx.collections.MappingChange;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by hucl on 16/9/19.
 * 用于初始化程序相关的数据
 */
@CrossOrigin
@RestController
@RequestMapping("/init")
public class InitDataController {

    @Autowired
    private ContinousAbsenceMorphiaRepository continousAbsenceMorphiaRepository;

    @Autowired
    private ServiceJpaRepository serviceJpaRepository;

    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;

    @Autowired
    private AbsenceDealService absenceDealService;

    @Autowired
    private DataCollectorService dataCollectorService;

    @Autowired
    private WorkOrderLogService workOrderLogService;

    @Autowired
    private AccountCardInfoService accountCardInfoService;

    @Autowired
    private ServeService serveService;

    @Autowired
    private RabbitMqSender rabbitMqSender;

    @Autowired
    private ThreadPoolManager threadPoolManager;

    private org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/absencenum", method = RequestMethod.POST)
    public JsonResultModel init() {
        List<Long> userIds = workOrderJpaRepository.findDistinctUsersFromWorkOrder();
        for (Long userId : userIds) {
            logger.info("###############################################正在处理用户============[{}]", userId);
            List<WorkOrder> workOrders = workOrderJpaRepository.findByStudentIdAndEndTimeLessThanOrderByStartTimeDesc(userId, new Date());
            for (WorkOrder workOrder : workOrders) {
                if (workOrder.getStatus() != FishCardStatusEnum.STUDENT_ABSENT.getCode()) {
                    break;
                }
                if (!workOrder.getService().getComboType().equals(ComboTypeEnum.EXCHANGE.toString())) {
                    break;
                }
                ContinousAbsenceRecord continousAbsenceRecord = continousAbsenceMorphiaRepository.queryByStudentIdAndComboType(userId, ComboTypeEnum.EXCHANGE.toString());
                if (null == continousAbsenceRecord) {
                    continousAbsenceRecord = new ContinousAbsenceRecord();
                    continousAbsenceRecord.setCreateTime(new Date());
                    continousAbsenceRecord.setContinusAbsenceNum(1);
                    continousAbsenceRecord.setComboType(ComboTypeEnum.EXCHANGE.toString());
                    continousAbsenceRecord.setStudentId(userId);
                    continousAbsenceMorphiaRepository.save(continousAbsenceRecord);
                } else {
                    continousAbsenceRecord.setContinusAbsenceNum(continousAbsenceRecord.getContinusAbsenceNum() + 1);
                    absenceDealService.updateCourseAbsenceNum(continousAbsenceRecord);
                }
            }
        }
        return JsonResultModel.newJsonResultModel("ok");
    }

    @RequestMapping(value = "/home", method = RequestMethod.POST)
    public JsonResultModel initHomePage() {
        List<Long> studentIds = serviceJpaRepository.findDistinctUsersFromService();
        studentIds.forEach(studentId -> dataCollectorService.updateBothChnAndFnItemAsync(studentId));
        return JsonResultModel.newJsonResultModel("ok");
    }

    @RequestMapping(value = "/home/student/{student_id}", method = RequestMethod.POST)
    public JsonResultModel initSpecialHomePage(@PathVariable("student_id") Long studentId) {
        dataCollectorService.updateBothChnAndFnItemAsync(studentId);
        return JsonResultModel.newJsonResultModel("ok");
    }

    @RequestMapping(value = "/async/all", method = RequestMethod.POST)
    public JsonResultModel asyncNotifyCustomer() {
        workOrderJpaRepository.findAll().forEach(workOrder -> {
            workOrderLogService.asyncNotifyCustomer(workOrder);
        });
        return JsonResultModel.newJsonResultModel("ok");
    }

    @RequestMapping(value = "/async/card/{fishcard_id}", method = RequestMethod.POST)
    public JsonResultModel asyncNotifyCustomer(@PathVariable("fishcard_id") Long fishcardId) {
        WorkOrder workOrder = workOrderJpaRepository.findOne(fishcardId);
        workOrderLogService.asyncNotifyCustomer(workOrder);
        return JsonResultModel.newJsonResultModel("ok");
    }

    //将已完成的鱼卡都重新通知到订单中心去
    @RequestMapping(value = "/async/order/complete", method = RequestMethod.POST)
    public JsonResultModel asyncNotifyOrder() {
        List<Service> services = serveService.findAll();
        services.stream().filter(service -> service.getProductType()==1001)
                .map(service1 -> service1.getOrderId()).collect(Collectors.toSet()).forEach(orderId -> {
            threadPoolManager.execute(new Thread(() -> {
                Map param = Maps.newHashMap();
                param.put("id", orderId);
                param.put("status", ConstantUtil.WORKORDER_COMPLETED);
                rabbitMqSender.send(param, QueueTypeEnum.NOTIFY_ORDER);
            }));

        });
        return JsonResultModel.newJsonResultModel("ok");
    }

    //将已完成的鱼卡都重新通知到订单中心去
    @RequestMapping(value = "/async/order/{order_id}/complete", method = RequestMethod.POST)
    public JsonResultModel asyncNotifyOrder(@PathVariable("order_id") Long orderId) {
        Map param = Maps.newHashMap();
        param.put("id", orderId);
        param.put("status", ConstantUtil.WORKORDER_COMPLETED);
        rabbitMqSender.send(param, QueueTypeEnum.NOTIFY_ORDER);
        return JsonResultModel.newJsonResultModel("ok");
    }
}
