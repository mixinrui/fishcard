package com.boxfishedu.workorder.servicex.studentrelated.validator;

import com.boxfishedu.workorder.common.exception.ValidationException;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by LuoLiBing on 16/9/24.
 * 每天最大上课数量限制
 */
@Order(5)
@Component
public class EveryDayCourseLimitValidator implements StudentTimePickerValidator {


    /**
     * 每天最大选课数量
     */
    private final static long everydayMaxCountCourse = 4;


    @Override
    public void postValidate(List<WorkOrder> workOrderList, Set<String> unFinishWorkOrder) {
        Map<String, Long> counter = unFinishWorkOrder
                .stream()
                .collect(Collectors.groupingBy((dateStr) -> dateStr.split(" ")[0], Collectors.counting()));

        // 只对新添加的课程进行验证,兼容历史数据
        workOrderList.forEach((workOrder -> {
            String date = DateUtil.string(workOrder.getStartTime());
            if(Objects.equals(counter.compute(date, (k, v) -> v == null ? 1 : v + 1), everydayMaxCountCourse)) {
                throw new ValidationException(date + "选课数量超过了最大" + everydayMaxCountCourse + "节课");
            }
        }));
    }
}
