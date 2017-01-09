package com.boxfishedu.workorder.servicex.studentrelated.validator;

import com.boxfishedu.workorder.common.exception.ValidationException;
import com.boxfishedu.workorder.common.log.ServiceLog;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.servicex.studentrelated.selectmode.SelectMode;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.boxfishedu.workorder.common.util.MailSupport;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.servicex.studentrelated.selectmode.SelectMode;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Created by LuoLiBing on 16/9/24.
 * 权限验证,防止非法操作其他人的service
 */
@Order(0)
@Component
public class AuthorityValidator implements StudentTimePickerValidator {

    private final static Logger logger = LoggerFactory.getLogger(AuthorityValidator.class);

    @Autowired
    private MailSupport mailSupport;

    @Override
    public void prepareValidate(TimeSlotParam timeSlotParam, SelectMode selectMode, List<Service> serviceList) {
        if (CollectionUtils.isEmpty(serviceList)) {
            mailSupport.reportError("无对应的服务", timeSlotParam + "/n" + ExceptionUtils.getStackTrace(e));
            logger.error(
                    new ServiceLog(timeSlotParam.getStudentId())
                            .data(timeSlotParam)
                            .operation("上课选时间")
                            .errorLevel()
                            .toString());
            throw new ValidationException("无对应的服务");
        }
        // 操作权限认证,防止非法的访问
        serviceList.forEach(s -> s.authentication(timeSlotParam.getStudentId()));
    }
}
