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
    private RepeatedSubmissionChecker checker;


    @Autowired
    public StudentTimePickerValidatorSupport(List<StudentTimePickerValidator> validatorList) {
        this.validatorList = validatorList;
    }


    public void prepareValidate(TimeSlotParam timeSlotParam, SelectMode selectMode, List<Service> serviceList) {
        try {
            validatorList.forEach(validator -> validator.prepareValidate(timeSlotParam, selectMode, serviceList));
        } catch (Exception e) {
            evictRepeatedSubmission(e, serviceList);
            throw e;
        }
    }


    public void postValidate(List<Service> serviceList, List<WorkOrder> workOrderList, Set<String> unFinishWorkOrder) {
        try {
            validatorList.forEach(validator -> validator.postValidate(serviceList, workOrderList, unFinishWorkOrder));
        } catch (Exception e) {
            evictRepeatedSubmission(e, serviceList);
            throw e;
        }
    }

    /**
     * 删除缓存
     * @param e
     * @param serviceList
     */
    public void evictRepeatedSubmission(Exception e, List<Service> serviceList) {
        if(!(e instanceof RepeatedSubmissionException)) {
            serviceList.forEach(service -> checker.evictRepeatedSubmission(service.getId()));
        }
    }
}
