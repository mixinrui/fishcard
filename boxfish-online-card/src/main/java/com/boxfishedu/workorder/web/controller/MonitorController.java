package com.boxfishedu.workorder.web.controller;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.workorder.common.bean.AppPointRecordEventEnum;
import com.boxfishedu.workorder.common.bean.QueueTypeEnum;
import com.boxfishedu.workorder.common.config.PoolConf;
import com.boxfishedu.workorder.common.rabbitmq.RabbitMqDelaySender;
import com.boxfishedu.workorder.common.rabbitmq.RabbitMqSender;
import com.boxfishedu.workorder.common.threadpool.ParameterThread;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.entity.mongo.WorkOrderLog;
import com.boxfishedu.workorder.requester.DataAnalysisRequester;
import com.boxfishedu.workorder.service.ServeService;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.servicex.timer.DailyCourseAssignedServiceX;
import com.boxfishedu.workorder.web.param.requester.DataAnalysisLogParam;
import com.google.common.collect.Maps;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by hucl on 16/4/6.
 */
@CrossOrigin
@RestController
@RequestMapping("/monitor")
public class MonitorController {
    //    @Autowired
//    private ThreadPoolManager threadPoolManager;
    @Autowired
    private PoolConf poolConf;
    @Autowired
    private ParameterThread parameterThread;
    @Autowired
    private WorkOrderLogService workOrderLogService;
    @Autowired
    private RabbitMqSender rabbitMqSender;
    @Autowired
    private ServeService serveService;
    @Autowired
    private RabbitMqDelaySender rabbitMqDelaySender;
    @Autowired
    private DataAnalysisRequester dataAnalysisRequester;

    @Autowired
    private DailyCourseAssignedServiceX dailyCourseAssignedServiceX;

    @RequestMapping(value = "/return/{num}", method = RequestMethod.GET)
    public void returnTest(@PathVariable("num") Integer num) throws Exception {
        parameterThread.test(num);
    }

    @RequestMapping(value = "/send", method = RequestMethod.GET)
    public void send() throws Exception {
        WorkOrderLog workOrderLog = new WorkOrderLog();
        workOrderLog.setWorkOrderId(1L);
        workOrderLog.setContent("测试数据啊");
        workOrderLog.setCreateTime(new Date());
        workOrderLog.setStatus(8);
        rabbitMqSender.send(workOrderLog, QueueTypeEnum.TEACHING_SERVICE);
    }

    @RequestMapping(value = "/send/{order_id}", method = RequestMethod.GET)
    public void send(@PathVariable("order_id") Long orderId) throws Exception {
        HashMap<String, Long> map = Maps.newHashMap();
        map.put("studentId", 18l);
        map.put("teacherId", 20l);
        map.put("id", orderId);

        //使用mq向小马发送创组请求
        rabbitMqSender.send(map, QueueTypeEnum.CREATE_GROUP);
    }

    @RequestMapping(value = "/exception", method = RequestMethod.GET)
    public void exception() throws Exception {
        throw new Exception("hahahhhahhsa");
    }

    @RequestMapping(value = "/delay", method = RequestMethod.GET)
    public void delay() throws Exception {
//        rabbitMqDelaySender.send();
        throw new Exception("hahahhhahhsa");
    }

    @RequestMapping(value = "/order/{order_id}/{status}", method = RequestMethod.POST)
    public void updateOrderStatus(@PathVariable("order_id") Long orderId, @PathVariable("status") Integer status) {
        for (int i = 0; i < 20; i++) {
            serveService.notifyOrderUpdateStatus(orderId, status);
        }
    }

    @RequestMapping(value = "/card/daily/assign", method = RequestMethod.GET)
    public JsonResultModel dailyAssignNotidy() {
        dailyCourseAssignedServiceX.batchNotifyTeacherAssignedCourse();
        return JsonResultModel.newJsonResultModel("ok");
    }

    @RequestMapping(value = "/heartbeat", method = RequestMethod.GET)
    public JsonResultModel heartBeat() {
        Date date = new Date();
        LocalDateTime endLocalDate = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).plusDays(3);
        LocalDateTime startLocalDate = endLocalDate.minusDays(6);
        Date startDate = DateUtil.localDate2Date(startLocalDate);
        Date endDate = DateUtil.localDate2Date(endLocalDate);
        DataAnalysisLogParam dataAnalysisLogParam = new DataAnalysisLogParam();
        dataAnalysisLogParam.setStartTime(startDate.getTime());
        dataAnalysisLogParam.setEndTime(endDate.getTime());
        dataAnalysisLogParam.setEvent(AppPointRecordEventEnum.ONLINE_COURSE_HEARTBEAT.value());
        dataAnalysisLogParam.setUserId(386l);
        return JsonResultModel.newJsonResultModel(dataAnalysisRequester.fetchHeartBeatLog(dataAnalysisLogParam));
    }

    public static void main(String[] args) throws Exception {
        Date now=new Date();
        LocalDateTime endLocalDate = LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault()).plusMinutes(100);
        System.out.println("结束时间:"+DateUtil.localDate2Date(endLocalDate).getTime());
        LocalDateTime startLocalDate = endLocalDate.minusMinutes(105);
        Date startDate = DateUtil.localDate2Date(startLocalDate);
        System.out.println("开始时间:"+startDate.getTime());
    }
}
