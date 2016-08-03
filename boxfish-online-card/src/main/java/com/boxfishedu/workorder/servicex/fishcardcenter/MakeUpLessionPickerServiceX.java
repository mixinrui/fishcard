package com.boxfishedu.workorder.servicex.fishcardcenter;

import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.TimeLimitPolicy;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.fishcardcenter.FishCardMakeUpService;
import com.boxfishedu.workorder.servicex.bean.DayTimeSlots;
import com.boxfishedu.workorder.servicex.bean.TimeSlots;
import com.boxfishedu.workorder.web.param.AvaliableTimeParam;
import com.boxfishedu.workorder.web.view.base.DateRange;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by hucl on 16/6/17.
 * 补课时候获取可以选择的补课时间列表
 */
@Component
public class MakeUpLessionPickerServiceX {
    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    @Autowired
    private TimeLimitPolicy timeLimitPolicy;

    @Value("${choiceTime.durationAllowMakeUp}")
    private Integer durationAllowMakeUp;

    @Value("${choiceTime.durationFromParentCourse}")
    private Integer durationFromParentCourse;

    @Autowired
    private FishCardMakeUpService fishCardMakeUpService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //获取学生可以选择补课的时间,超过补课时间;不再允许补课;但是如果是多次补课呢??????必须要求落下的课程在两周内补完
    public JsonResultModel listAvaliableSlots(Long workOrderId) {
        Integer days = durationAllowMakeUp;
        WorkOrder workOrder = validateAndGetWorkOrder(workOrderId);
        //校验有效性
        fishCardMakeUpService.processMakeUpParam(workOrder);
        // 获取时间区间
        DateRange dateRange = getEnableDateRange(workOrder, days);
        // 获取时间片模板,并且复制
        DayTimeSlots dayTimeSlots = teacherStudentRequester.dayTimeSlotsTemplate(workOrder.getTeacherId());
        List<DayTimeSlots> monthTimeSlots = Lists.newArrayList();
        for (LocalDateTime from = dateRange.getFrom(); from.isBefore(dateRange.getTo()); from = from.plusDays(1)) {
            try {
                DayTimeSlots clone = (DayTimeSlots) dayTimeSlots.clone();
                clone.setDay(DateUtil.localDate2SimpleString(from));
                clone = timeLimitPolicy.limit(clone);
                monthTimeSlots.add(clone);
            } catch (Exception ex) {
                logger.error("鱼卡[{}]获取可用时间片失败", workOrderId, ex);
                throw new BusinessException("鱼卡[" + workOrderId + "]获取可用时间片失败");
            }
        }

        AvaliableTimeParam avaliableTimeParam = new AvaliableTimeParam();
        avaliableTimeParam.setStudentId(workOrder.getStudentId());
        addTagForSlotTemplate(avaliableTimeParam, monthTimeSlots, dateRange);

        return JsonResultModel.newJsonResultModel(monthTimeSlots);
    }

    private AvaliableTimeParam getTimeParam(WorkOrder workOrder) {
        AvaliableTimeParam avaliableTimeParam = new AvaliableTimeParam();
        avaliableTimeParam.setStudentId(workOrder.getStudentId());
        avaliableTimeParam.setWorkOrderId(workOrder.getId());
        return avaliableTimeParam;
    }

    private void addTagForSlotTemplate(AvaliableTimeParam avaliableTimeParam, List<DayTimeSlots> dayTimeSlotsList, DateRange dateRange) {
        Map<String, WorkOrder> timeSlotMapSelected = findByStudentIdAndStartTimeBetween(avaliableTimeParam, dateRange);
        //往后推得所有数据,只要是本周有课的都默认不让选择
        if (0 == timeSlotMapSelected.size()) return;
        for (DayTimeSlots dayTimeSlots : dayTimeSlotsList) {
            Iterator<TimeSlots> slotsIterator = dayTimeSlots.getDailyScheduleTime().iterator();
            while (slotsIterator.hasNext()) {
                TimeSlots timeSlots = slotsIterator.next();
                String key = dayTimeSlots.getDay() + timeSlots.getSlotId().toString();
                if (null != timeSlotMapSelected.get(key)) {
                    slotsIterator.remove();
                }
            }
        }
    }

    //找出所有可能重复的工单
    private Map<String, WorkOrder> findByStudentIdAndStartTimeBetween(AvaliableTimeParam avaliableTimeParam, DateRange dateRange) {
        Map<String, WorkOrder> map = Maps.newHashMap();
        List<WorkOrder> workOrders = workOrderService.findByStudentIdAndStartTimeBetween(avaliableTimeParam.getStudentId(),
                DateUtil.localDate2Date(dateRange.getFrom()), DateUtil.localDate2Date(dateRange.getTo()));
        if (!CollectionUtils.isEmpty(workOrders)) {
            workOrders.forEach(workOrder -> {
                if (workOrder.getStartTime() != null) {
                    map.put(DateUtil.date2SimpleString(workOrder.getStartTime()) + workOrder.getSlotId().toString(), workOrder);
                }
            });
        }
        return map;
    }

    private DateRange getEnableDateRange(WorkOrder workOrder, Integer days) {
        // 如果没有未消费的订单,则取得当前时间;否则换成订单的最后结束时间
        Date oldWorkOrderDate = workOrder.getStartTime();
        LocalDateTime oldWorkOrderLocalDate = LocalDateTime.ofInstant(oldWorkOrderDate.toInstant(), ZoneId.systemDefault());
        LocalDateTime deadLineDate = oldWorkOrderLocalDate.plusDays(durationAllowMakeUp);

        Date now = new Date();
        LocalDateTime startDate = LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault());
        startDate = startDate.plusDays(durationFromParentCourse);

        if (startDate.isAfter(deadLineDate)) {
            logger.info("!!!!!!!!!鱼卡[{}]已超出补课时间,不能进行补课", workOrder.getId());
            throw new BusinessException("当前鱼卡已超过补课的时间限制,不能补课");
        }
        DateRange dateRange = new DateRange();
        dateRange.setFrom(startDate);
        dateRange.setTo(deadLineDate);
        return dateRange;
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
}
