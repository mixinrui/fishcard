package com.boxfishedu.workorder.web.controller;

import com.boxfishedu.workorder.common.bean.ComboTypeEnum;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.QueueTypeEnum;
import com.boxfishedu.workorder.common.bean.TutorTypeEnum;
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
import com.boxfishedu.workorder.service.absencendeal.AbsenceDealService;
import com.boxfishedu.workorder.service.accountcardinfo.AccountCardInfoService;
import com.boxfishedu.workorder.service.accountcardinfo.DataCollectorService;
import com.boxfishedu.workorder.service.accountcardinfo.OnlineAccountService;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.servicex.dataanalysis.FetchHeartBeatServiceX;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sun.javafx.collections.MappingChange;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by hucl on 16/9/19.
 * 用于初始化程序相关的数据
 */
@CrossOrigin
@RestController
@RequestMapping("/init")
@SuppressWarnings("ALL")
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

    @Autowired
    private OnlineAccountService onlineAccountService;

    @Autowired
    private InstantClassTimeRulesMorphiaRepository instantClassTimeRulesMorphiaRepository;

    @Autowired
    private CourseScheduleService courseScheduleService;

    @Autowired
    private FetchHeartBeatServiceX fetchHeartBeatServiceX;

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
        Map<Long, Integer> selectedAndCompletedMap = services.stream().filter(service -> service.getProductType() == 1001 && service.getCoursesSelected() == 1).collect(Collectors.groupingBy(Service::getOrderId, Collectors.summingInt(Service::getAmount)));
        Set<Long> selectedSet = selectedAndCompletedMap.entrySet().stream().filter(entry -> entry.getValue() > 0).map(entry -> entry.getKey()).collect(Collectors.toSet());
        Set<Long> completedSet = selectedAndCompletedMap.entrySet().stream().filter(entry -> entry.getValue() == 0).map(entry -> entry.getKey()).collect(Collectors.toSet());

        completedSet.forEach(orderId -> {
            Map param = Maps.newHashMap();
            param.put("id", orderId);
            param.put("status", ConstantUtil.WORKORDER_COMPLETED);
            rabbitMqSender.send(param, QueueTypeEnum.NOTIFY_ORDER);
        });
        System.out.println(completedSet);
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

    //将在线用户的数据初始化到mongo和redis中去
    @RequestMapping(value = "/async/online/account", method = RequestMethod.POST)
    public JsonResultModel asyncInitOnlineUser() {
        List<Service> services = serveService.findAll();
        Set<Long> useIdSet = services.stream().map(service -> service.getStudentId()).collect(Collectors.toSet());
        threadPoolManager.execute(new Thread(() -> useIdSet.forEach(userId -> onlineAccountService.add(userId))));
        return JsonResultModel.newJsonResultModel("OK");
    }

    @RequestMapping(value = "/async/online/account/trial", method = RequestMethod.POST)
    public JsonResultModel asyncInitTrialOnlineUser() {
        List<WorkOrder> workOrders = workOrderJpaRepository.findByOrderId(9223372036854775807l);
        Set<Long> useIdSet = workOrders.stream().map(service -> service.getStudentId()).collect(Collectors.toSet());
        threadPoolManager.execute(new Thread(() -> useIdSet.forEach(userId -> onlineAccountService.add(userId))));
        return JsonResultModel.newJsonResultModel("OK");
    }


    //即时上课时间片限制生成
    @RequestMapping(value = "/instanttimes", method = RequestMethod.POST)
    public JsonResultModel instantClassTimes(@RequestBody Map<String, String> dateInfo) {
        Date beginDate = DateUtil.String2Date(dateInfo.get("begin"));
        Date endDate = DateUtil.String2Date(dateInfo.get("end"));
        String tutorType = dateInfo.get("tutorType");
        LocalDateTime beginLocal = LocalDateTime.ofInstant(beginDate.toInstant(), ZoneId.systemDefault());
        LocalDateTime endLocal = LocalDateTime.ofInstant(endDate.toInstant(), ZoneId.systemDefault());
        for (LocalDateTime localDateTime = beginLocal; localDateTime.isBefore(endLocal); localDateTime = localDateTime.plusDays(1)) {
            logger.debug("正在初始化数据:[" + DateUtil.localDate2SimpleString(localDateTime) + "]");
            switch (localDateTime.getDayOfWeek()) {
                case SATURDAY:
                case SUNDAY: {
                    if (tutorType.equals(TutorTypeEnum.FRN.toString())) {
                        InstantClassTimeRules instantClassTimeRules1 = new InstantClassTimeRules();
                        instantClassTimeRules1.setDate(DateUtil.localDate2SimpleString(localDateTime));
                        instantClassTimeRules1.setDay(localDateTime.getDayOfWeek().toString());
                        instantClassTimeRules1.setBegin("09:00:00");
                        instantClassTimeRules1.setEnd("12:00:00");
                        instantClassTimeRules1.setTutorType(TutorTypeEnum.FRN.toString());
                        instantClassTimeRulesMorphiaRepository.save(instantClassTimeRules1);

                        InstantClassTimeRules instantClassTimeRules2 = new InstantClassTimeRules();
                        instantClassTimeRules2.setDate(DateUtil.localDate2SimpleString(localDateTime));
                        instantClassTimeRules2.setDay(localDateTime.getDayOfWeek().toString());
                        instantClassTimeRules2.setBegin("19:00:00");
                        instantClassTimeRules2.setEnd("23:00:00");
                        instantClassTimeRules2.setTutorType(TutorTypeEnum.FRN.toString());
                        instantClassTimeRulesMorphiaRepository.save(instantClassTimeRules2);
                    } else {
                        InstantClassTimeRules instantClassTimeRules1 = new InstantClassTimeRules();
                        instantClassTimeRules1.setDate(DateUtil.localDate2SimpleString(localDateTime));
                        instantClassTimeRules1.setDay(localDateTime.getDayOfWeek().toString());
                        instantClassTimeRules1.setBegin("09:00:00");
                        instantClassTimeRules1.setEnd("23:00:00");
                        instantClassTimeRules1.setTutorType(TutorTypeEnum.CN.toString());
                        instantClassTimeRulesMorphiaRepository.save(instantClassTimeRules1);
                    }
                    break;
                }
                default:
                    InstantClassTimeRules instantClassTimeRules = new InstantClassTimeRules();
                    instantClassTimeRules.setDate(DateUtil.localDate2SimpleString(localDateTime));
                    instantClassTimeRules.setDay(localDateTime.getDayOfWeek().toString());
                    instantClassTimeRules.setBegin("19:00:00");
                    instantClassTimeRules.setEnd("23:00:00");
                    instantClassTimeRules.setTutorType(tutorType);
                    instantClassTimeRulesMorphiaRepository.save(instantClassTimeRules);
                    break;
            }
        }
        return JsonResultModel.newJsonResultModel("OK");
    }


    @RequestMapping(value = "/schedule/starttime", method = RequestMethod.POST)
    public JsonResultModel initScheduleStartTime() throws InterruptedException {
        List<Long> cardIds = workOrderJpaRepository.findAllWorkOrderId();
        StopWatch stopWatch = new StopWatch();
        int totalNum = cardIds.size();
        logger.debug("开始处理.....总数{}", totalNum);
        stopWatch.start();
        AtomicInteger successNum = new AtomicInteger();
        AtomicInteger failNum = new AtomicInteger();
        List<Long> failList = new Vector<>();
        cardIds.forEach(cardId ->
                        {
                            threadPoolManager.execute(new Thread(() -> {
                                try {
                                    WorkOrder workOrder = workOrderJpaRepository.findOne(cardId);
                                    CourseSchedule courseSchedule = courseScheduleService.findByWorkOrderId(cardId);
                                    courseSchedule.setStartTime(workOrder.getStartTime());
                                    courseSchedule.setClassDate(workOrder.getStartTime());
                                    courseSchedule.setTimeSlotId(workOrder.getSlotId());
                                    courseScheduleService.save(courseSchedule);
                                    int dealNum = successNum.addAndGet(1);
                                    logger.debug("->" + cardId + ";已经处理{}条,剩余{}条", dealNum, (totalNum - dealNum));
                                } catch (Exception ex) {
                                    failNum.addAndGet(1);
                                    failList.add(cardId);
                                    logger.error("失败[{}]", cardId, ex);
                                }
                            }));
                        }
        );
        while (true) {
            if (failNum.get() + successNum.get() == totalNum) {
                stopWatch.stop();
                logger.debug("处理完成....,总数{};处理成功数{};失败数{};花费{}秒", totalNum, successNum, failNum, stopWatch.getTotalTimeSeconds());
                if (!CollectionUtils.isEmpty(failList)) {
                    logger.error("失败的鱼卡列表{}", failList);
                }
                return JsonResultModel.newJsonResultModel("ok");
            }
            Thread.sleep(100);
        }
    }

    @RequestMapping(value = "/net", method = RequestMethod.POST)
    public JsonResultModel initNet() {
        List<Long> cardIds = workOrderJpaRepository.findAllFinishedId(new Date());
        logger.debug("初始化网络,总数[{}]", cardIds.size());
        cardIds.forEach(cardId -> {
            logger.debug("正在初始化网络数据>>>>[{}]", cardId);
            try {
                fetchHeartBeatServiceX.persistAnalysisAsync(workOrderJpaRepository.findOne(cardId));
            } catch (Exception ex) {
                logger.error(cardId.toString(), ex);
            }

        });
        return JsonResultModel.newJsonResultModel("OK");
    }
}
