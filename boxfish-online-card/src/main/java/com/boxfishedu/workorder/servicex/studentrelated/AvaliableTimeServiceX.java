package com.boxfishedu.workorder.servicex.studentrelated;

import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.studentrelated.RandomSlotFilterService;
import com.boxfishedu.workorder.servicex.bean.DayTimeSlots;
import com.boxfishedu.workorder.servicex.bean.MonthTimeSlots;
import com.boxfishedu.workorder.web.param.AvaliableTimeParam;
import com.boxfishedu.workorder.web.view.base.DateRange;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by hucl on 16/5/17.
 * 以前的老版本
 */
@Component
public class AvaliableTimeServiceX {

    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    @Autowired
    private CourseScheduleService courseScheduleService;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private RandomSlotFilterService randomSlotFilterService;

    /**
     * 免费体验的天数
     */
    @Value("${choiceTime.freeExperienceDay:7}")
    private Integer freeExperienceDay;
    /**
     * 选时间生效天数,默认为第二天生效
     */
    @Value("${choiceTime.consumerStartDay:2}")
    private Integer consumerStartDay;
    private final static Integer daysOfWeek = 7;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     *获取可以修改鱼卡的时间片列表
     */
    public JsonResultModel getTimeAvailable(AvaliableTimeParam avaliableTimeParam) throws CloneNotSupportedException {
        // 判断是免费还是正常购买
        Integer days = avaliableTimeParam.getIsFree() ? freeExperienceDay : daysOfWeek;
        // 获取时间区间
        DateRange dateRange = getEnableDateRange(avaliableTimeParam, days);

        // TODO
        Set<String> classDateTimeSlotsSet = courseScheduleService.findByStudentIdAndAfterDate(avaliableTimeParam.getStudentId());
        // 获取时间片模板,并且复制
        DayTimeSlots dayTimeSlots = teacherStudentRequester.dayTimeSlotsTemplate((long) avaliableTimeParam.getComboTypeEnum().getValue());
        List<DayTimeSlots> dayTimeSlotsList = dateRange.forEach(dayTimeSlots, (localDateTime, d) -> {
            DayTimeSlots clone = (DayTimeSlots) d.clone();
            clone.setDay(DateUtil.formatLocalDate(localDateTime));
//            DayTimeSlots result = timeLimitPolicy.limit(clone);
            //获取时间片范围内的数据
            DayTimeSlots result = randomSlotFilterService.removeSlotsNotInRange(clone,avaliableTimeParam);
            if(Objects.isNull(result) || CollectionUtils.isEmpty(result.getDailyScheduleTime())) {
                return null;
            }
            //随机显示热点时间片
            result=randomSlotFilterService.removeExculdeSlot(result,avaliableTimeParam);
            result.setDailyScheduleTime(result.getDailyScheduleTime().stream()
                    .filter(t -> !classDateTimeSlotsSet.contains(String.join(" ", clone.getDay(), t.getSlotId().toString())))
                    .collect(Collectors.toList()));
            return result;
        });
        return JsonResultModel.newJsonResultModel(new MonthTimeSlots(dayTimeSlotsList).getData());
    }


    public JsonResultModel getTimeAvailableChangeTime(Long workOrderId,String date) throws CloneNotSupportedException {
        WorkOrder workOrder = validateAndGetWorkOrder(workOrderId);

        AvaliableTimeParam avaliableTimeParam  = new AvaliableTimeParam();
        avaliableTimeParam.setStudentId(workOrder.getStudentId());
        avaliableTimeParam.setComboType(workOrder.getService().getComboType());
        avaliableTimeParam.setTutorType(workOrder.getService().getTutorType());
        avaliableTimeParam.setDate(date);


        Integer days = new Integer(1);
        // 获取时间区间
        DateRange dateRange = getEnableDateRangeCurrentDay(avaliableTimeParam, days);
       logger.info("getTimeAvailableChangeTimes [{}]",DateUtil.date2SimpleDate(DateUtil.String2Date(date)));
        // TODO  获取当天的时间
        Set<String> classDateTimeSlotsSet = courseScheduleService.findByStudentIdAndCurrentDate(avaliableTimeParam.getStudentId(),DateUtil.date2SimpleDate(DateUtil.String2Date(date))  );
        // 获取时间片模板,并且复制
        DayTimeSlots dayTimeSlots = teacherStudentRequester.dayTimeSlotsTemplate((long) avaliableTimeParam.getComboTypeEnum().getValue());
        List<DayTimeSlots> dayTimeSlotsList = dateRange.forEach(dayTimeSlots, (localDateTime, d) -> {
            DayTimeSlots clone = (DayTimeSlots) d.clone();
            clone.setDay(DateUtil.formatLocalDate(localDateTime));
//            DayTimeSlots result = timeLimitPolicy.limit(clone);
            //获取时间片范围内的数据
            DayTimeSlots result = randomSlotFilterService.removeSlotsNotInRange(clone,avaliableTimeParam);
            result.setDailyScheduleTime(result.getDailyScheduleTime().stream()
                    .filter(t -> !classDateTimeSlotsSet.contains(String.join(" ", clone.getDay(), t.getSlotId().toString())))
                    .collect(Collectors.toList()));
            return result;
        });
        return JsonResultModel.newJsonResultModel(new MonthTimeSlots(dayTimeSlotsList).getData());
    }



    private WorkOrder validateAndGetWorkOrder(Long workOrderId) {
        WorkOrder workOrder;
        try {
            workOrder = workOrderService.findOne(workOrderId);
            if (null == workOrder) {
                throw new BusinessException("鱼卡在服务端无对应的服务");
            }
        } catch (Exception ex) {
            throw new BusinessException("传入鱼卡的id在服务端无对应服务");
        }
        return workOrder;
    }


    /**
     * 获取可选的时间区间
     *
     * @return
     */
    private DateRange getEnableDateRange(AvaliableTimeParam avaliableTimeParam, Integer days) {
        // 如果没有未消费的订单,则取得当前时间;否则换成订单的最后结束时间
        WorkOrder workOrder = null;
        try {
            workOrder = workOrderService.getLatestWorkOrderByStudentIdAndComboType(
                    avaliableTimeParam.getStudentId(), avaliableTimeParam.getComboTypeEnum().name());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("获取可用时间片时获取鱼卡失败,此次选课为该学生的首单选课");
        }
        Date date = new Date();
        int afterDays = consumerStartDay;
        // 同类型工单的最后一个工单
        if (null != workOrder && workOrder.getEndTime().after(date)) {
            date = workOrder.getEndTime();
            afterDays = 1;
        }
        LocalDateTime startDate = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        if(afterDays > 0) {
            startDate = startDate.plusDays(afterDays);
        }
        return new DateRange(startDate, days);
    }

    private DateRange getEnableDateRangeCurrentDay(AvaliableTimeParam avaliableTimeParam, Integer days) {
        Date date = DateUtil.date2SimpleDate(DateUtil.String2Date(avaliableTimeParam.getDate()))  ;
        LocalDateTime startDate = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return new DateRange(startDate, days);
    }

}
