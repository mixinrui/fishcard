package com.boxfishedu.workorder.web.controller;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.redis.CacheKeyConstant;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.ServeService;
import com.boxfishedu.workorder.service.ServiceSDK;
import com.boxfishedu.workorder.service.WorkOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Created by hucl on 16/6/12.
 * 供给演示环境使用
 */
@CrossOrigin
@RestController
@RequestMapping("/test")
public class TestController {
    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private ServeService serveService;

    @Autowired
    private CourseScheduleService courseScheduleService;
    @Autowired
    private ServiceSDK serviceSDK;

    @Autowired
    private CacheManager cacheManager;

    @RequestMapping(value = "/fishcard", method = RequestMethod.PUT)
    public void changeFishCardTime(@RequestBody Map<String,String> param){
       String idStr=param.get("id");
        String beginStr=param.get("begin");
        String endStr=param.get("end");
        String timeSlotIdStr=param.get("timeSlotId");
        String teacherIdStr=param.get("teacherId");
        String studentIdStr=param.get("studentId");
        Long id=null,teacherId=null,studentId=null;
        Integer timeSlotId=null;
        Date begin=null,end=null;
        if(null!=idStr)
            id=Long.parseLong(idStr);
        if(null!=beginStr)
            begin=DateUtil.String2Date(beginStr);
        if(null!=endStr)
            end=DateUtil.String2Date(endStr);
        if(null!=timeSlotIdStr)
            timeSlotId=Integer.parseInt(timeSlotIdStr);
        if(null!=teacherIdStr)
            teacherId=Long.parseLong(teacherIdStr);
        if(null!=studentIdStr)
            studentId=Long.parseLong(studentIdStr);

        WorkOrder workOrder=workOrderService.findOne(id);
        CourseSchedule courseSchedule=courseScheduleService.findByWorkOrderId(id);
        if(null!=end)
            workOrder.setEndTime(end);
        if(null!=begin) {
            courseSchedule.setClassDate(DateUtil.date2SimpleDate(begin));
            workOrder.setStartTime(begin);
        }
        if(null!=timeSlotId) {
            workOrder.setSlotId(timeSlotId);
            courseSchedule.setTimeSlotId(timeSlotId);
        }
        workOrder.setUpdateTime(new Date());
        courseSchedule.setUpdateTime(new Date());
        if(null!=teacherId){
            workOrder.setTeacherId(teacherId);
            courseSchedule.setTeacherId(teacherId);
            workOrder.setStatus(FishCardStatusEnum.TEACHER_ASSIGNED.getCode());
            courseSchedule.setStatus(FishCardStatusEnum.TEACHER_ASSIGNED.getCode());
        }
        if(null!=studentId){
            workOrder.setStudentId(studentId);
            courseSchedule.setStudentId(studentId);
        }
        workOrder.setUpdateTime(new Date());
        courseSchedule.setUpdateTime(new Date());
        workOrderService.save(workOrder);
        courseScheduleService.save(courseSchedule);
        serviceSDK.createGroup(workOrder);
    }

    @RequestMapping(value = "/save/code", method = RequestMethod.POST)
    public void pdateWorkOrder(){
        List<WorkOrder> workOrders=workOrderService.findAll();
        for (WorkOrder workOrder:workOrders){
            workOrder.setOrderCode(workOrder.getService().getOrderCode());
        }
        workOrderService.save(workOrders);

    }

    @RequestMapping(value = "/redis/{value}", method = RequestMethod.POST)
    public void testRedis(@PathVariable("value") String value){
        cacheManager.getCache(CacheKeyConstant.NOTIFY_TEACHER_PREPARE_CLASS_KEY).put("21212121212112", value);
        System.out.println(cacheManager.getCache(CacheKeyConstant.NOTIFY_TEACHER_PREPARE_CLASS_KEY).get("21212121212112").get().toString());
    }
}
