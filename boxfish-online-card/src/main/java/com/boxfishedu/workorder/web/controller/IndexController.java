package com.boxfishedu.workorder.web.controller;

import com.boxfishedu.online.order.entity.OrderForm;
import com.boxfishedu.workorder.common.bean.RedisTypeEnum;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.CourseOnlineRequester;
import com.boxfishedu.workorder.service.RedisService;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Created by hucl on 16/4/11.
 */
@CrossOrigin
@RestController
@RequestMapping("/index")
public class IndexController {
    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private CourseOnlineRequester courseOnlineRequester;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index() {
        return "欢迎来到教学中心api";
    }

    @RequestMapping(value = "/date/{id}/{str}", method = RequestMethod.GET)
    public String index(@PathVariable("id") Long id, @PathVariable("str") String date) throws Exception {
        WorkOrder workOrder = workOrderService.findOne(id);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        System.out.println("==========:::::::::::::" + workOrder.getStartTime());
        System.out.println("workorderdate:" + df.parse(DateUtil.Date2String(workOrder.getStartTime())).getTime());
        System.out.println("date:::::::" + df.parse((date)).getTime());
        return "欢迎来到教学中心api";
    }


    public static void main(String[] args) throws Exception {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        long time = df.parse(("2016-05-09")).getTime();

        DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
        long time2 = df2.parse(df2.format(new Date())).getTime();
        System.out.println(time + "::::::" + time2);
    }

    @RequestMapping(value = "/redis/{content}", method = RequestMethod.POST)
    public void redis(@PathVariable("content") String content) {
        OrderForm order = new OrderForm();
        order.setUserName(content + 1111);
        order.setId(new Random().nextLong());
        redisService.set(order, RedisTypeEnum.ORDER2SERVICE, order.getId());
    }

    @RequestMapping(value = "/redis/clear/DayTimeSlots", method = RequestMethod.DELETE)
    @CacheEvict(value = "DayTimeSlots", allEntries = true)
    public Object clearDayTimeSlotsCache() {
        return JsonResultModel.newJsonResultModel();
    }

    @RequestMapping(value = "/redis/clear/timeSlots", method = RequestMethod.DELETE)
    @CacheEvict(value = "timeSlots", allEntries = true)
    public Object clearTimeSlotsCache() {
        return JsonResultModel.newJsonResultModel();
    }

    @RequestMapping(value = "release", method = RequestMethod.GET)
    public void release() {
        WorkOrder workOrder = new WorkOrder();
        workOrder.setId(123123123l);
        courseOnlineRequester.releaseGroup(workOrder);
    }
}
