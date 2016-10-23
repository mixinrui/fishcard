package com.boxfishedu.workorder.servicex.studentrelated.validator;

import com.boxfishedu.workorder.common.exception.ValidationException;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.servicex.studentrelated.selectmode.SelectMode;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

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
                throw new ValidationException("该订单已经完成选课,请勿重复选课");
            }
        }
    }
}
