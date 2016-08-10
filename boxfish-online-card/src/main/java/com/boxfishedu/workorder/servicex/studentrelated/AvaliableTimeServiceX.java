package com.boxfishedu.workorder.servicex.studentrelated;

import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.TimeLimitPolicy;
import com.boxfishedu.workorder.service.WorkOrderService;
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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by hucl on 16/5/17.
 */
@Component
public class AvaliableTimeServiceX {

    @Autowired
    private TimeLimitPolicy timeLimitPolicy;

    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    @Autowired
    private CourseScheduleService courseScheduleService;

    @Autowired
    private WorkOrderService workOrderService;

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
        DayTimeSlots dayTimeSlots = teacherStudentRequester.dayTimeSlotsTemplate(avaliableTimeParam.getComboType().getValue());
        List<DayTimeSlots> dayTimeSlotsList = dateRange.forEach(dayTimeSlots, (localDateTime, d) -> {
            DayTimeSlots clone = (DayTimeSlots) d.clone();
            clone.setDay(DateUtil.formatLocalDate(localDateTime));
            DayTimeSlots result = timeLimitPolicy.limit(clone);
            result.setDailyScheduleTime(result.getDailyScheduleTime().stream()
                    .filter(t -> !classDateTimeSlotsSet.contains(String.join(" ", clone.getDay(), t.getSlotId().toString())))
                    .collect(Collectors.toList()));
            return result;
        });
        return JsonResultModel.newJsonResultModel(new MonthTimeSlots(dayTimeSlotsList).getData());
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
                    avaliableTimeParam.getStudentId(), avaliableTimeParam.getComboType().name());
        } catch (Exception ex) {
            logger.error("获取可用时间片时获取鱼卡失败,此次选课为该学生的首单选课");
        }
        Date date = new Date();
        if (null != workOrder && workOrder.getEndTime().after(date)) {
            date = workOrder.getEndTime();
        }
        LocalDateTime startDate = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        startDate = startDate.plusDays(consumerStartDay);
        return new DateRange(startDate, days);
    }

}
