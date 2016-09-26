package com.boxfishedu.workorder.servicex.studentrelated.validator;

import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.servicex.studentrelated.selectmode.SelectMode;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by LuoLiBing on 16/9/24.
 * 权限验证,防止非法操作其他人的service
 */
@Order(0)
@Component
public class AuthorityValidator implements StudentTimePickerValidator {

    @Override
    public void prepareValidate(TimeSlotParam timeSlotParam, SelectMode selectMode, List<Service> serviceList) {
        BoxfishAsserts.notEmpty(serviceList, "无对应的服务");
        // 操作权限认证,防止非法的访问
        serviceList.forEach(s -> s.authentication(timeSlotParam.getStudentId()));
    }
}
