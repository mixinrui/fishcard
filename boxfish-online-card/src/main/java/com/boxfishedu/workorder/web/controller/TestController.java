package com.boxfishedu.workorder.web.controller;

import com.alibaba.fastjson.JSON;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.redis.CacheKeyConstant;
import com.boxfishedu.workorder.common.util.Collections3;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.BaseTimeSlotJpaSmallClassRepository;
import com.boxfishedu.workorder.dao.jpa.SmallClassJpaRepository;
import com.boxfishedu.workorder.dao.jpa.StStudentApplyRecordsJpaRepository;
import com.boxfishedu.workorder.dao.mongo.InstantCardLogMorphiaRepository;
import com.boxfishedu.workorder.entity.mysql.*;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.ServeService;
import com.boxfishedu.workorder.service.ServiceSDK;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.accountcardinfo.DataCollectorService;
import com.boxfishedu.workorder.service.accountcardinfo.OnlineAccountService;
import com.boxfishedu.workorder.service.baseTime.BaseTimeSlotService;
import com.boxfishedu.workorder.service.baseTime.BaseTimeSlotSmallClassService;
import com.boxfishedu.workorder.service.instantclass.InstantClassService;
import com.boxfishedu.workorder.service.smallclass.SmallClassService;
import com.boxfishedu.workorder.servicex.assignTeacher.AssignTeacherServiceX;
import com.boxfishedu.workorder.servicex.instantclass.classdatagenerator.ScheduleEntranceDataGenerator;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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
    private InstantClassService instantClassService;

    @Autowired
    private ServeService serveService;

    @Autowired
    private CourseScheduleService courseScheduleService;
    @Autowired
    private ServiceSDK serviceSDK;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private OnlineAccountService onlineAccountService;

    @Autowired
    private ScheduleEntranceDataGenerator scheduleEntranceDataGenerator;

    @Autowired
    private InstantCardLogMorphiaRepository instantCardLogMorphiaRepository;

    @Autowired
    private BaseTimeSlotService baseTimeSlotService;
    @Autowired
    private AssignTeacherServiceX assignTeacherServiceX;

    @Autowired
    private SmallClassJpaRepository smallClassJpaRepository;

    @Autowired
    private StStudentApplyRecordsJpaRepository stStudentApplyRecordsJpaRepository;

    @Autowired
    private DataCollectorService dataCollectorService;

    @Autowired
    private SmallClassService smallClassService;

    @Autowired
    private BaseTimeSlotSmallClassService baseTimeSlotSmallClassService;

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/fishcard", method = RequestMethod.PUT)
    public void changeFishCardTime(@RequestBody Map<String, String> param) {
        String idStr = param.get("id");
        String beginStr = param.get("begin");
        String endStr = param.get("end");
        String timeSlotIdStr = param.get("timeSlotId");
        String teacherIdStr = param.get("teacherId");
        String studentIdStr = param.get("studentId");
        Long id = null, teacherId = null, studentId = null;
        Integer timeSlotId = null;
        Date begin = null, end = null;
        if (null != idStr)
            id = Long.parseLong(idStr);
        if (null != beginStr)
            begin = DateUtil.String2Date(beginStr);
        if (null != endStr)
            end = DateUtil.String2Date(endStr);
        if (null != timeSlotIdStr)
            timeSlotId = Integer.parseInt(timeSlotIdStr);
        if (null != teacherIdStr)
            teacherId = Long.parseLong(teacherIdStr);
        if (null != studentIdStr)
            studentId = Long.parseLong(studentIdStr);

        WorkOrder workOrder = workOrderService.findOne(id);
        CourseSchedule courseSchedule = courseScheduleService.findByWorkOrderId(id);
        if (null != end)
            workOrder.setEndTime(end);
        if (null != begin) {
            courseSchedule.setClassDate(DateUtil.date2SimpleDate(begin));
            courseSchedule.setStartTime(begin);
            workOrder.setStartTime(begin);
        }
        if (null != timeSlotId) {
            workOrder.setSlotId(timeSlotId);
            courseSchedule.setTimeSlotId(timeSlotId);
        }
        workOrder.setUpdateTime(new Date());
        courseSchedule.setUpdateTime(new Date());
        if (null != teacherId) {
            workOrder.setTeacherId(teacherId);
            courseSchedule.setTeacherId(teacherId);
            workOrder.setStatus(FishCardStatusEnum.TEACHER_ASSIGNED.getCode());
            courseSchedule.setStatus(FishCardStatusEnum.TEACHER_ASSIGNED.getCode());
        }
        if (null != studentId) {
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
    public void pdateWorkOrder() {
        List<WorkOrder> workOrders = workOrderService.findAll();
        for (WorkOrder workOrder : workOrders) {
            workOrder.setOrderCode(workOrder.getService().getOrderCode());
        }
        workOrderService.save(workOrders);

    }

    @RequestMapping(value = "/redis/{value}", method = RequestMethod.POST)
    public void testRedis(@PathVariable("value") String value) {
        cacheManager.getCache(CacheKeyConstant.NOTIFY_TEACHER_PREPARE_CLASS_KEY).put("21212121212112", value);
        System.out.println(cacheManager.getCache(CacheKeyConstant.NOTIFY_TEACHER_PREPARE_CLASS_KEY).get("21212121212112").get().toString());
    }

    @RequestMapping(value = "/change_teacher/{workOrderId}", method = RequestMethod.POST)
    public void testTeacher(@PathVariable("workOrderId") Long workOrderId) {
        WorkOrder workOrder = workOrderService.findOne(workOrderId);
        workOrderService.changeTeacherForTypeChanged(workOrder);
    }

    @RequestMapping(value = "/add/redis/user/{user_id}", method = RequestMethod.POST)
    public void addUser(@PathVariable("user_id") Long userId) {
        onlineAccountService.add(userId);
    }

    @RequestMapping(value = "/redis/user/{user_id}", method = RequestMethod.GET)
    public JsonResultModel getUser(@PathVariable("user_id") Long userId) {
        return JsonResultModel.newJsonResultModel(onlineAccountService.isMember(userId));
    }

    @RequestMapping(value = "/add/redis/sync", method = RequestMethod.POST)
    public void syncMongo2Redis() {
        onlineAccountService.syncMongo2Redis();
    }

    @RequestMapping(value = "/slot/latest", method = RequestMethod.GET)
    public void slot() {
        System.out.println(instantClassService.getMostSimilarSlot(2l));
    }

    @RequestMapping(value = "/queue", method = RequestMethod.GET)
    public void queueTest() {
        InstantClassCard instantClassCard = new InstantClassCard();
        instantClassCard.setWorkorderId(58805l);
        instantClassCard.setSlotId(29l);
        scheduleEntranceDataGenerator.initCardAndSchedule(instantClassCard);
    }

    @RequestMapping(value = "/instantcard/{instant_id}", method = RequestMethod.GET)
    public JsonResultModel notiFyTeachers(@PathVariable("instant_id") Long instantId) {
        return JsonResultModel.newJsonResultModel(instantCardLogMorphiaRepository.findByInstantCardId(instantId));
    }

    @RequestMapping(value = "/instantlog/student/{student_id}", method = RequestMethod.GET)
    public JsonResultModel notiFyTeachersByStudent(@PathVariable("student_id") Long studentId) {
        return JsonResultModel.newJsonResultModel(instantCardLogMorphiaRepository.findByInstantStudentId(studentId));
    }

    // 增加时间片   增加目前已有的时间片
    @RequestMapping(value= "/baseTime/addSlot/{slots}")
    public Object addOne2OneTimeSlots(@PathVariable Integer slots) {
        baseTimeSlotService.addTimeSlots(slots);
        return ResponseEntity.ok().build();
    }


    /**
     * 初始化 新的时间片(1对1)
     *
     * @param days
     * @return
     */
    @RequestMapping(value = "/baseTime/init/{days}")
    public Object initBaseTimeSlots(@PathVariable Integer days) {
        baseTimeSlotService.initBaseTimeSlots(days);
        return ResponseEntity.ok().build();
    }




    /**
     * 初始化 新的时间片(小班课)
     *
     * @param days
     * @return
     */
    @RequestMapping(value = "/baseTime/init/smallclass/{days}")
    public Object initBaseTimeSlotsForSmallClass(@PathVariable Integer days) {
        baseTimeSlotSmallClassService.initBaseTimeSlotsSmallClass(days);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/assign")

    public void testAssign(Long studentId, Long teacherId, Integer skuid) {
        assignTeacherServiceX.maualAssign(teacherId, studentId, skuid);
    }

    @RequestMapping(value = "/teacherAccept")
    public void teacherAccept(Long studentId, Long teacherId) {
        List<StStudentApplyRecords> stStudentApplyRecordsList = stStudentApplyRecordsJpaRepository.findByStudentIdAndTeacherIdAndValid(studentId, teacherId, StStudentApplyRecords.VALID.yes);
        List<Long> courseScheleIds = Collections3.extractToList(stStudentApplyRecordsList, "courseScheleId");
        List<Long> workOrderIds = Collections3.extractToList(stStudentApplyRecordsList, "workOrderId");
        assignTeacherServiceX.teacherAccept(teacherId, studentId, workOrderIds);
    }

    @RequestMapping(value = "/homeupdate", method = RequestMethod.GET)
    public JsonResultModel homeupdate(Long studentId) {
        dataCollectorService.updateBothChnAndFnItem(studentId);
        return JsonResultModel.newJsonResultModel("ok");
    }

    @RequestMapping(value = "/proxy", method = RequestMethod.POST)
    public JsonResultModel proxy(String flag) {
        //代理
        if (Objects.equals("true", flag)) {
            smallClassService.testProxy();
        }
        //非代理
        else {
            this.testNotProxy();
        }
        return JsonResultModel.newJsonResultModel();
    }

    @Transactional
    private void testNotProxy() {
        SmallClass smallClass=new SmallClass();
        smallClass.setStartTime(new Date());
        smallClass.setStatus(10);
        smallClass.setEndTime(new Date());
        smallClass.setClassDate(new Date());
        smallClass.setClassNum(1l);
        smallClass.setCreateTime(new Date());

        logger.debug("&&&&&&&&&&&&&&&&&&&&&& not proxy[{}]",smallClass.getId());

        smallClassJpaRepository.save(smallClass);
        smallClass.setClassNum(2l);
    }

}
