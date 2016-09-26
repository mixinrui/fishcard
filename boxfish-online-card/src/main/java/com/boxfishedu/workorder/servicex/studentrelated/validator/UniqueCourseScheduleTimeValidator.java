package com.boxfishedu.workorder.servicex.studentrelated.validator;

import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Created by LuoLiBing on 16/9/24.
 * 上课时间互斥验证
 */
@Order(4)
@Component
public class UniqueCourseScheduleTimeValidator implements StudentTimePickerValidator {

    @Override
    public void postValidate(List<WorkOrder> workOrderList, Set<String> unFinishWorkOrder) {
        for(WorkOrder workOrder : workOrderList) {
            checkUniqueCourseSchedule(
                    unFinishWorkOrder,
                    workOrder,
                    () -> String.join(" ", DateUtil.Date2String(workOrder.getStartTime()))
                            + "已经安排了课程,请重新选择!");
        }
    }

    private void checkUniqueCourseSchedule(
            Set<String> classDateTimeSlotsSet, WorkOrder workOrder, Supplier<String> exceptionProducer) {
        if(classDateTimeSlotsSet.contains(
                String.join(" ", DateUtil.simpleDate2String(workOrder.getStartTime()),
                        workOrder.getSlotId().toString()))) {
            throw new BusinessException(exceptionProducer.get());
        }
    }
}
