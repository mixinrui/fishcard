package com.boxfishedu.workorder.service;

import com.boxfishedu.workorder.common.bean.QueueTypeEnum;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.exception.NetWorkException;
import com.boxfishedu.workorder.common.rabbitmq.RabbitMqSender;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.entity.mongo.ScheduleCourseInfo;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.CourseOnlineRequester;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.servicex.bean.CourseView;
import com.boxfishedu.workorder.servicex.bean.MonthTimeSlots;
import com.boxfishedu.workorder.web.view.form.DateRangeForm;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by LuoLiBing on 16/4/25.
 */
@Service
public class ServiceSDK {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UrlConf urlConf;

    @Autowired
    private RabbitMqSender rabbitMqSender;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private ScheduleCourseInfoService scheduleCourseInfoService;

    @Autowired
    private CourseOnlineRequester courseOnlineRequester;

    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    private final static Logger logger = LoggerFactory.getLogger(ServiceSDK.class);


    /**
     * 获取老师一个月选的时间片
     * @param teacherId
     * @param dateRangeForm
     * @return
     */
    public MonthTimeSlots getMonthTimeSlotsByDateBetween(Long teacherId, DateRangeForm dateRangeForm) {
        logger.info("向师生运营组发起教师[{}]日期请求,url[{}]",teacherId,timeSlotsUrl());
        MonthTimeSlots monthTimeSlots = restTemplate.postForObject(
                timeSlotsUrl(), timeSlotsParam(teacherId, dateRangeForm), MonthTimeSlots.class);
        // 将时间戳转换为日期字符串
        monthTimeSlots.transferTimestampToDate();
        return monthTimeSlots;
    }


    private String timeSlotsUrl() {
        return String.format("%s/timeslot/months", urlConf.getTeacher_service());
    }

    public CourseView getCourseInfo(Long scheduleId){
        ScheduleCourseInfo scheduleCourseInfo=scheduleCourseInfoService.queryByScheduleId(scheduleId);
        return CourseView.courseViewAdapter(scheduleCourseInfo);
    }

    //@Recover
    public void recover(Exception e) {
        e.printStackTrace();
        logger.error("网络异常" + e.getMessage());
        throw new NetWorkException("网络缓慢,请稍后再试");
    }

    /**
     *  {
     "studentId":975443,
     "teacherId":1028080,
     "id":1
     }
     * @param workOrder
     */
    public void createGroup(WorkOrder workOrder) {
        logger.info("@createGroup开始通知在线授课创建组,鱼卡号:[{}]",workOrder.getId());
        Map map = new HashMap<String, Long>(3);
        map.put("studentId", workOrder.getStudentId());
        map.put("teacherId", workOrder.getTeacherId());
        map.put("id", workOrder.getId());

        //使用mq向小马发送创组请求
        rabbitMqSender.send(map, QueueTypeEnum.CREATE_GROUP);
        logger.info("完成通知在线授课创建组,鱼卡号:[{}]",workOrder.getId());
    }

    private Map<String, Object> timeSlotsParam(Long teacherId, DateRangeForm dateRangeForm) {
        HashMap<String, Object> param = Maps.newHashMap();
        param.put("userId", teacherId.toString());
        param.put("type", "TEACHER");
        param.put("begin", dateRangeForm.getBeginLongValue());
        param.put("end", dateRangeForm.getEndLongValue());
        logger.info("获取教师[{}]的参数:{}",teacherId, JacksonUtil.toJSon(param));
        return param;
    }
}
