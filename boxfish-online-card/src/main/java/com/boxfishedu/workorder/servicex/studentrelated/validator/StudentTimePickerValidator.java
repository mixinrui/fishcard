package com.boxfishedu.workorder.servicex.studentrelated.validator;

import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.servicex.studentrelated.selectmode.SelectMode;
import com.boxfishedu.workorder.web.param.TimeSlotParam;

import java.util.List;
import java.util.Set;

/**
 * Created by LuoLiBing on 16/9/24.
 */
public interface StudentTimePickerValidator {

    default void prepareValidate(TimeSlotParam timeSlotParam, SelectMode selectMode, List<Service> serviceList) {}

    default void postValidate(List<WorkOrder> workOrderList, Set<String> unFinishWorkOrder) {}

}
