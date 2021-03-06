package com.boxfishedu.workorder.servicex.studentrelated.validator;

import com.boxfishedu.workorder.common.exception.ValidationException;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.servicex.studentrelated.selectmode.SelectMode;
import com.boxfishedu.workorder.servicex.studentrelated.selectmode.TemplateSelectMode;
import com.boxfishedu.workorder.servicex.studentrelated.selectmode.UserDefinedSelectMode;
import com.boxfishedu.workorder.web.param.SelectedTime;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by LuoLiBing on 16/9/24.
 * 提交上课次数验证
 */
@Order(1)
@Component
public class SelectedCountValidator implements StudentTimePickerValidator {

    @Override
    public void prepareValidate(TimeSlotParam timeSlotParam, SelectMode selectMode, List<Service> serviceList) {
        List<SelectedTime> selectedTimes = timeSlotParam.getSelectedTimes();

        if(selectMode!= null && selectMode instanceof UserDefinedSelectMode) {
            Integer count = serviceList.stream()
                    .filter(service -> Objects.equals(service.getProductType(), 1001))
                    .collect(Collectors.summingInt(Service::getAmount));
            if(!Objects.equals(count, selectedTimes.size())) {
                throw new ValidationException("选择的上课次数不符合规范");
            }
        } else {
            if((TemplateSelectMode.createSelectTemplateParam(timeSlotParam, serviceList).getNumPerWeek())
                    != selectedTimes.size()) {
                throw new ValidationException("选择的上课次数不符合规范");
            }
        }
    }

    @Override
    public void postValidate(List<Service> serviceList, List<WorkOrder> workOrderList, Set<String> unFinishWorkOrder) {
        // 防止选课次数和提交鱼卡的次数不一致
        Integer count = serviceList.stream().collect(Collectors.summingInt(Service::getAmount));
        if(!Objects.equals(count, workOrderList.size())) {
            throw new ValidationException("选择的上课次数超过了可选次数");
        }
    }
}
