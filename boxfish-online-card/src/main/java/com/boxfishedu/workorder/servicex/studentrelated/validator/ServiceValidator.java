package com.boxfishedu.workorder.servicex.studentrelated.validator;

import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.servicex.studentrelated.selectmode.SelectMode;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Created by LuoLiBing on 16/9/24.
 * service是否选课验证,选课次数与service验证
 */
@Order(2)
@Component
public class ServiceValidator implements StudentTimePickerValidator {

    @Override
    public void prepareValidate(TimeSlotParam timeSlotParam, SelectMode selectMode, List<Service> serviceList) {
        for(Service service : serviceList) {
            if (service.getCoursesSelected() == 1) {
                throw new BusinessException("该订单已经完成选课,请勿重复选课");
            }
        }
    }

    @Override
    public void postValidate(List<Service> serviceList, List<WorkOrder> workOrderList, Set<String> unFinishWorkOrder) {
        // 次数验证
        int count = 0;
        for(Service service : serviceList) {
            if(Objects.equals(service.getProductType(), 1001)) {
                count += service.getAmount();
            }
        }
        if(count != workOrderList.size()) {
            throw new BusinessException(String.format(
                    "该订单包含课程次数(%s)与所选课次数(%s)不符", count, workOrderList.size()));
        }
    }
}
