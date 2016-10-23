package com.boxfishedu.workorder.servicex.studentrelated.validator;

import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.servicex.studentrelated.selectmode.SelectMode;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Created by LuoLiBing on 16/9/24.
 * 选时间验证support类
 */
@Component
public class StudentTimePickerValidatorSupport {


    /**
     * 0权限验证->1选择次数验证->2service验证->3参数重复选择时间验证->4unique验证->5每天上课总次数验证
     */
    private List<StudentTimePickerValidator> validatorList;


    @Autowired
    public StudentTimePickerValidatorSupport(List<StudentTimePickerValidator> validatorList) {
        this.validatorList = validatorList;
    }


    public void prepareValidate(TimeSlotParam timeSlotParam, SelectMode selectMode, List<Service> serviceList) {
        validatorList.forEach(validator -> validator.prepareValidate(timeSlotParam, selectMode, serviceList));
    }


    public void postValidate(List<Service> serviceList, List<WorkOrder> workOrderList, Set<String> unFinishWorkOrder) {
        validatorList.forEach(validator -> validator.postValidate(serviceList, workOrderList, unFinishWorkOrder));
    }
}
