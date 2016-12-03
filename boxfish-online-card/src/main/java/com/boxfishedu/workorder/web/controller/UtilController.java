package com.boxfishedu.workorder.web.controller;

import com.boxfishedu.workorder.common.bean.ComboTypeEnum;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.QueueTypeEnum;
import com.boxfishedu.workorder.common.rabbitmq.RabbitMqSender;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.common.util.ConstantUtil;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.ServiceJpaRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.dao.mongo.ContinousAbsenceMorphiaRepository;
import com.boxfishedu.workorder.dao.mongo.InstantClassTimeRulesMorphiaRepository;
import com.boxfishedu.workorder.entity.mongo.ContinousAbsenceRecord;
import com.boxfishedu.workorder.entity.mongo.InstantClassTimeRules;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.ServeService;
import com.boxfishedu.workorder.service.ServiceSDK;
import com.boxfishedu.workorder.service.absencendeal.AbsenceDealService;
import com.boxfishedu.workorder.service.accountcardinfo.AccountCardInfoService;
import com.boxfishedu.workorder.service.accountcardinfo.DataCollectorService;
import com.boxfishedu.workorder.service.accountcardinfo.OnlineAccountService;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.google.common.collect.Maps;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by hucl on 16/9/19.
 * 用于初始化程序相关的数据
 */
@CrossOrigin
@RestController
@RequestMapping("/util")
@SuppressWarnings("ALL")
public class UtilController {

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

    @Autowired
    private OnlineAccountService onlineAccountService;

    @Autowired
    private InstantClassTimeRulesMorphiaRepository instantClassTimeRulesMorphiaRepository;

    @Autowired
    private CourseScheduleService courseScheduleService;

    @Autowired
    private ServiceSDK serviceSDK;

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());


    @RequestMapping(value = "/group/card/{fishcard_id}", method = RequestMethod.POST)
    public JsonResultModel asyncNotifyCustomer(@PathVariable("fishcard_id") Long fishcardId) {
        WorkOrder workOrder = workOrderJpaRepository.findOne(fishcardId);
        serviceSDK.createGroup(workOrder);
        return JsonResultModel.newJsonResultModel("ok");
    }
}
