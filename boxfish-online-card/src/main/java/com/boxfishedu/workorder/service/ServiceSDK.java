package com.boxfishedu.workorder.service;

import com.boxfishedu.workorder.common.bean.PublicClassMessageEnum;
import com.boxfishedu.workorder.common.bean.QueueTypeEnum;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.exception.NetWorkException;
import com.boxfishedu.workorder.common.exception.PublicClassException;
import com.boxfishedu.workorder.common.rabbitmq.RabbitMqSender;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.entity.mongo.ScheduleCourseInfo;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.servicex.bean.CourseView;
import com.boxfishedu.workorder.servicex.bean.MonthTimeSlots;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.web.view.form.DateRangeForm;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

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
    private ScheduleCourseInfoService scheduleCourseInfoService;

    @Autowired
    private WorkOrderLogService workOrderLogService;

    private final static Logger logger = LoggerFactory.getLogger(ServiceSDK.class);

    /**
     * 获取老师一个月选的时间片
     *
     * @param teacherId
     * @param dateRangeForm
     * @return
     */
    public MonthTimeSlots getMonthTimeSlotsByDateBetween(Long teacherId, DateRangeForm dateRangeForm) {
        logger.info("向师生运营组发起教师[{}]日期请求,url[{}]", teacherId, timeSlotsUrl());
        MonthTimeSlots monthTimeSlots = restTemplate.postForObject(
                timeSlotsUrl(), timeSlotsParam(teacherId, dateRangeForm), MonthTimeSlots.class);
        // 将时间戳转换为日期字符串
        monthTimeSlots.transferTimestampToDate();
        return monthTimeSlots;
    }


    private String timeSlotsUrl() {
        return String.format("%s/timeslot/months", urlConf.getTeacher_service());
    }


    public CourseView getCourseInfoByScheduleId(Long scheduleId, Locale locale) {
        ScheduleCourseInfo scheduleCourseInfo = scheduleCourseInfoService.queryByScheduleId(scheduleId);
        return getCourseInfo(scheduleCourseInfo, locale);
    }


    public CourseView getCourseInfoByWorkOrderId(Long workOrderId, Locale locale) {
        ScheduleCourseInfo scheduleCourseInfo = scheduleCourseInfoService.queryByWorkId(workOrderId);
        return getCourseInfo(scheduleCourseInfo, locale);
    }


    /**
     * 判断是否是会员
     *
     * @param accessToken
     * @return
     */
    public JsonResultModel getMemberInfo(String accessToken) {
        JsonResultModel resultModel = null;
        URI uri = createMemberInfo(accessToken);
        try {
            resultModel = restTemplate.getForObject(uri, JsonResultModel.class);
            if (Objects.isNull(resultModel) || Objects.isNull(resultModel.getData())) {
                throw new PublicClassException(PublicClassMessageEnum.NETWORK_ERROR);
            }
            logger.debug("@getMemberInfo#获取会员信息成功,uri[{}],结果[{}]"
                    , uri.toString(), JacksonUtil.toJSon(resultModel));
            return resultModel;
        } catch (Exception e) {
            logger.error("@getMemberInfo#获取会员信息失败,URI[{}],结果[{}]"
                    , uri.toString(), JacksonUtil.toJSon(resultModel), e);
            // 网络错误, 请重试
            throw new PublicClassException(PublicClassMessageEnum.NETWORK_ERROR);
//            return JsonResultModel.EMPTY;
        }
    }

    public CourseView getCourseInfo(ScheduleCourseInfo scheduleCourseInfo, Locale locale) {
        CourseView courseView = CourseView.courseViewAdapter(scheduleCourseInfo);
        if (Objects.isNull(courseView)) {
            return null;
        }
        courseView.setLocale(locale);
        return courseView;
    }

    //@Recover
    public void recover(Exception e) {
        e.printStackTrace();
        logger.error("网络异常" + e.getMessage());
        throw new NetWorkException("网络缓慢,请稍后再试");
    }

    /**
     * {
     * "studentId":975443,
     * "teacherId":1028080,
     * "id":1
     * }
     *
     * @param workOrder
     */
    public void createGroup(WorkOrder workOrder) {
        logger.info("@createGroup开始通知在线授课创建组,鱼卡号:[{}]", workOrder.getId());
        Map map = new HashMap<String, Long>(3);
        map.put("studentId", workOrder.getStudentId());
        map.put("teacherId", workOrder.getTeacherId());
        map.put("id", workOrder.getId());

        workOrderLogService.saveWorkOrderLog(workOrder, "创建群组关系");
        //使用mq向小马发送创组请求
        rabbitMqSender.send(map, QueueTypeEnum.CREATE_GROUP);
        logger.info("完成通知在线授课创建组,鱼卡号:[{}]", workOrder.getId());
    }

    private Map<String, Object> timeSlotsParam(Long teacherId, DateRangeForm dateRangeForm) {
        HashMap<String, Object> param = Maps.newHashMap();
        param.put("userId", teacherId.toString());
        param.put("type", "TEACHER");
        param.put("begin", dateRangeForm.getBeginLongValue());
        param.put("end", dateRangeForm.getEndLongValue());
        logger.info("获取教师[{}]的参数:{}", teacherId, JacksonUtil.toJSon(param));
        return param;
    }

    private URI createMemberInfo(String accessToken) {
        return UriComponentsBuilder.fromUriString(urlConf.getMemberUrl())
                                   .path("/info")
                                   .queryParam("access_token", accessToken)
                                   .build()
                                   .toUri();
    }
}
